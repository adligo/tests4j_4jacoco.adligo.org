package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.adligo.tests4j.models.shared.asserts.AssertionProcessor;
import org.adligo.tests4j.models.shared.common.ClassMethods;
import org.adligo.tests4j.models.shared.dependency.ClassAlias;
import org.adligo.tests4j.models.shared.dependency.ClassAliasLocal;
import org.adligo.tests4j.models.shared.dependency.ClassReferencesLocal;
import org.adligo.tests4j.models.shared.dependency.ClassReferencesLocalMutant;
import org.adligo.tests4j.models.shared.dependency.DependencyMutant;
import org.adligo.tests4j.models.shared.dependency.I_ClassAliasLocal;
import org.adligo.tests4j.models.shared.dependency.I_ClassFilter;
import org.adligo.tests4j.models.shared.dependency.I_ClassParentsLocal;
import org.adligo.tests4j.models.shared.dependency.I_ClassReferencesLocal;
import org.adligo.tests4j.models.shared.dependency.I_Dependency;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;
import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.map.MapInstrConstants;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

/**
 * a model like (non thread safe) class that loads classes into
 * the class loader, discovers 
 * references.
 * 
 * references and dependencies are very similar concepts in this package as follows;
 * references illuminate that one class references another.
 * dependencies illuminate that one class depends on another, 
 *     and indicate 
 *    The I_CachedClassBytesClassLoader is shared memory between threads.
 * Also this model keeps a cache of the references for classes
 * it reads, so it doesn't need to re-ASM byte code read them.
 * 
 * 
 * @author scott
 *
 */
public class ClassReferencesDiscovery {
	private I_CachedClassBytesClassLoader classLoader;
	private I_Tests4J_Log log;
	private ReferenceTrackingClassVisitor cv;
	private I_DiscoveryMemory discoveryMemory;
	private I_ClassFilter basicClassFilter;
	private List<I_ClassAliasLocal> initalRefsToIdentify = new ArrayList<I_ClassAliasLocal>();
	private Map<I_ClassAliasLocal, I_ClassReferencesLocal> refMap = new HashMap<I_ClassAliasLocal,I_ClassReferencesLocal>();
	private ClassParentsDiscovery cpd;
	private ClassInitialReferencesDiscovery cird;
	
	public ClassReferencesDiscovery(I_CachedClassBytesClassLoader pClassLoader,
			I_Tests4J_Log pLog,  I_DiscoveryMemory dc) {
		classLoader = pClassLoader;
		log = pLog;
		discoveryMemory = dc;
		basicClassFilter = dc.getBasicClassFilter();
		cv = new ReferenceTrackingClassVisitor(Opcodes.ASM5, log);
		cpd = new ClassParentsDiscovery(pClassLoader, pLog, dc);
		cird = new ClassInitialReferencesDiscovery(pClassLoader, pLog, dc);
	}
	
	public List<String> findOrLoad(Class<?> c) throws IOException, ClassNotFoundException {
		if (log.isLogEnabled(ClassReferencesDiscovery.class)) {
			log.log("ClassReferencesDiscovery.discoverAndLoad " + c.getName());
		}
		String className = c.getName();
		refMap.clear();
		I_ClassReferencesLocal crefs =  discoveryMemory.getReferences(className);
		if (crefs != null) {
			refMap.put(new ClassAliasLocal(crefs), crefs);
			Set<I_ClassParentsLocal> refs = crefs.getReferencesLocal();
			for (I_ClassAliasLocal alias: refs) {
				I_ClassReferencesLocal crefRef = discoveryMemory.getReferences(alias.getName());
				if (crefRef != null) {
					refMap.put(alias, crefRef);
				} else {
					Map<I_ClassAliasLocal, I_ClassReferencesLocal> newRefs = loadInitalReferences(alias.getTarget(), c);
					refMap.putAll(newRefs);
				}
			}
		} else {
			Map<I_ClassAliasLocal, I_ClassReferencesLocal> newRefs = loadInitalReferences(c, c);
			refMap.putAll(newRefs);
			crefs = refMap.get(new ClassAlias(c));
		}
		Set<Entry<I_ClassAliasLocal, I_ClassReferencesLocal>> entries = refMap.entrySet();
		for (Entry<I_ClassAliasLocal, I_ClassReferencesLocal> e: entries) {
			I_ClassReferencesLocal val = e.getValue();
			Set<I_ClassParentsLocal> refs = val.getReferencesLocal();
			for (I_ClassAliasLocal alias: refs) {
				String ref = alias.getName();
				if (!className.equals(ref)) {
					if ( !discoveryMemory.isFiltered(ref)) {
						initalRefsToIdentify.add(alias);
					}
				}
			}
		}
		
		initalRefsToIdentify.removeAll(refMap.keySet());
		while (initalRefsToIdentify.size() >= 1) {
			I_ClassAliasLocal next = initalRefsToIdentify.get(0);
			Class<?> nextClass = next.getTarget();
			Map<I_ClassAliasLocal, I_ClassReferencesLocal> newRefs = loadInitalReferences(nextClass, c);
			refMap.putAll(newRefs);
			Set<Entry<I_ClassAliasLocal, I_ClassReferencesLocal>> newEntries = newRefs.entrySet();
			for (Entry<I_ClassAliasLocal, I_ClassReferencesLocal> e: newEntries) {
				I_ClassReferencesLocal val = e.getValue();
				Set<I_ClassParentsLocal> delRs = val.getReferencesLocal();
				for (I_ClassAliasLocal alias: delRs) {
					String ref = alias.getName();
					//check local caches first
					if (!initalRefsToIdentify.contains(ref)) {
						if ( !discoveryMemory.isFiltered(ref)) {
							initalRefsToIdentify.add(alias);
						}
					}
				}
			}
			initalRefsToIdentify.removeAll(refMap.keySet());
		}
		//ok all of the initial references should be loaded in the refMap
		rebuildRefMapWithAllReferences();
		//ok all references are loaded
		calcCircles();
		addToRefsCache();
		List<String> refOrder = calcRefOrder(c);
		return refOrder;
	}

	/**
	 * This loads the initial one tier references
	 * into the refMap.
	 * 
	 * @param c
	 * @param referencingClass only for the log, the referencingClass
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private Map<I_ClassAliasLocal, I_ClassReferencesLocal> loadInitalReferences(Class<?> c, Class<?> referencingClass) 
		throws IOException, ClassNotFoundException {
		
		
		Map<I_ClassAliasLocal, I_ClassReferencesLocal> newRefs = new HashMap<I_ClassAliasLocal, I_ClassReferencesLocal>();
		I_ClassReferencesLocal refs = discoveryMemory.getReferences(c.getName());
		if (refs != null) {
			newRefs.put(refs, refs);
			return newRefs;
		}
		if (discoveryMemory.isFiltered(c)) {
			return newRefs;
		}
		
		I_ClassParentsLocal cps = cpd.findOrLoad(c);
		List<I_ClassParentsLocal> parents = cps.getParentsLocal();
		for (I_ClassParentsLocal p: parents) {
			Class<?> target = p.getTarget();
			if ( !discoveryMemory.isFiltered(target)) {
				I_ClassReferencesLocal crl = cird.findOrLoad(target);
				newRefs.put(new ClassAliasLocal(crl), crl);
			}
		}
		I_ClassReferencesLocal crl = cird.findOrLoad(c);
		newRefs.put(new ClassAliasLocal(crl), crl);
		return newRefs;
	}
	

	/**
	 * this should recurse up the references tree
	 * @param c
	 * @param classNames
	 * @param recursionStack
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	protected void addRefAndRecurse(ClassReferencesLocalMutant cplm, I_ClassAliasLocal alias,  Set<I_ClassParentsLocal> done) 
			throws IOException, ClassNotFoundException {
		
		I_ClassParentsLocal cps = cpd.findOrLoad(alias.getTarget());
		cplm.addReference(cps);
		
		I_ClassReferencesLocal initalRef =  refMap.get(alias);
		if (initalRef == null) {
			if (discoveryMemory.isFiltered(alias.getTarget())) {
				//its a filtered class
				return;
			} else {
				throw new IllegalStateException("hmm no refMap entry for " + alias);
			}
		}
		done.add(initalRef);
		List<I_ClassParentsLocal> currentNames = new ArrayList<I_ClassParentsLocal>(initalRef.getReferencesLocal());

		currentNames.removeAll(done);
		
		for (I_ClassParentsLocal cpl: currentNames) {
			addRefAndRecurse(cplm,cpl, done);
		}
	}


	
	/**
	 * ok at this point either we have 
	 * just references, or a mix of references and 
	 * cached dependencies.  
	 * Calculate the most referenced in the group and order
	 * 
	 * This calculates a rough order of dependencies
	 * @return
	 */
	private List<String> calcRefOrder(Class<?> c) {
		String topName = c.getName();
		Set<I_Dependency> deps = toDependencies(topName);
		List<String> toRet = new ArrayList<String>();
		
		Iterator<I_Dependency> it = deps.iterator();
		while (deps.size() >= 1) {
			while (it.hasNext()) {
				I_Dependency dep = it.next();
				I_ClassParentsLocal alias = (I_ClassParentsLocal) dep.getAlias();
				String depName = alias.getName();
				
				List<String> parentNames = alias.getParentNames();
				if (parentNames.size() == 0 || toRet.containsAll(parentNames)) {
					Class<?> dc = alias.getTarget();
					if (discoveryMemory.isFiltered(dc)) {
						if (!toRet.contains(depName)) {
							toRet.add(depName);
						}
						it.remove();
					} else {
						I_ClassReferencesLocal local = refMap.get(alias);
						if (local == null) {
							throw new NullPointerException("problem finding refs for " + depName 
									+ " on " + c);
						}
						Set<String> refNames =  local.getReferenceNames();
						refNames = new HashSet<String>(refNames);
						if (local.hasCircularReferences()) {
							refNames.removeAll(local.getCircularReferenceNames());
						} 
						refNames.remove(depName);
						if (refNames.size() == 0 || toRet.containsAll(refNames)) {
							 if (!toRet.contains(depName)) {
								 toRet.add(depName);
							 }
							 it.remove();
						}
					}
				} else {
					//add the parents
					//add all of the jse stuff
					List<I_ClassParentsLocal> parents =  alias.getParentsLocal();
					for (I_ClassParentsLocal parent: parents) {
						String parentName = parent.getName();
						Class<?> pc = parent.getTarget();
						if (discoveryMemory.isFiltered(pc)) {
							if (!toRet.contains(parentName)) {
								 toRet.add(parentName);
							 }
						} else {
							I_ClassReferencesLocal local = refMap.get(alias);
							Set<String> refNames =  local.getReferenceNames();
							refNames = new HashSet<String>(refNames);
							if (local.hasCircularReferences()) {
								refNames.removeAll(local.getCircularReferenceNames());
							}
							refNames.remove(depName);
							if (refNames.size() == 0 || toRet.containsAll(refNames)) {
								 if (!toRet.contains(depName)) {
									 toRet.add(depName);
								 }
								 it.remove();
							}
						}
					}
				}
			}
			it = deps.iterator();
		}
		
		boolean adding = true;
		int count = 1;
		while (adding) {
			String inName = topName + "$" + count;
			if (refMap.containsKey( new ClassAlias(inName))) {
				if (!toRet.contains(inName)) {
					toRet.add(inName);
				}
			} else {
				adding = false;
			}
			count++;
		}
		if (!toRet.contains(topName)) {
			toRet.add(topName);
		}
		return toRet;
	}

	public Set<I_Dependency> toDependencies(String topName) {
		Map<String,DependencyMutant> refCounts = new HashMap<String,DependencyMutant>();
		
		Set<Entry<I_ClassAliasLocal, I_ClassReferencesLocal>> refs =  refMap.entrySet();
		for (Entry<I_ClassAliasLocal,I_ClassReferencesLocal> e: refs) {
			I_ClassAliasLocal key = e.getKey();
			String className = key.getName();
			I_ClassReferencesLocal crs = e.getValue();
			Set<I_ClassParentsLocal> classes = crs.getReferencesLocal();
			
			DependencyMutant count = null;
			if (isNotClassOrInnerClass(crs, topName)) {
				
				
				for (I_ClassParentsLocal ref: classes) {
					if (isNotClassOrInnerClass(ref, className)) {
						count = refCounts.get(ref.getName());
						if (count == null) {
							count = new DependencyMutant();
							count.setAlias(ref);
							count.addReference();
						} else {
							count.addReference();
						}
						refCounts.put(ref.getName(), count);
					}
				}
			} else {
				for (I_ClassParentsLocal ref: classes) {
					if (isNotClassOrInnerClass(ref, className)) {
						count = refCounts.get(ref.getName());
						if (count == null) {
							count = new DependencyMutant();
							count.setAlias(ref);
							count.addReference();
						} else {
							count.addReference();
						}
						refCounts.put(ref.getName(), count);
					}
				}
			}
		}
		
		Set<I_Dependency> deps = new TreeSet<I_Dependency>(refCounts.values());
		return deps;
	}
	
	private boolean isNotClassOrInnerClass(I_ClassParentsLocal ref, String topName) {
		String className = ref.getName();
		if (className.equals(topName)) {
			return false;
		} else if (className.indexOf(topName + "$") == 0) {
			return false;
		}
		return true;
	}
	private void rebuildRefMapWithAllReferences() throws IOException, ClassNotFoundException {
		Map<I_ClassAliasLocal, I_ClassReferencesLocal> newRefMap = new HashMap<I_ClassAliasLocal, I_ClassReferencesLocal>();
		Set<Entry<I_ClassAliasLocal, I_ClassReferencesLocal>> entries = refMap.entrySet();
		for (Entry<I_ClassAliasLocal, I_ClassReferencesLocal> e: entries) {
			I_ClassAliasLocal key = e.getKey();
			I_ClassReferencesLocal val = e.getValue();
			ClassReferencesLocalMutant crm = new ClassReferencesLocalMutant(val);
			
			Set<I_ClassParentsLocal> refs = val.getReferencesLocal();
			Set<I_ClassParentsLocal> done = new HashSet<I_ClassParentsLocal>();
			for (I_ClassParentsLocal ref: refs) {
				addRefAndRecurse(crm, ref, done);
			}
			newRefMap.put(new ClassAliasLocal(key), new ClassReferencesLocal(crm));
		}
		refMap = newRefMap;
	}
	private void calcCircles() {
		Map<I_ClassAliasLocal, I_ClassReferencesLocal> newRefMap = new HashMap<I_ClassAliasLocal, I_ClassReferencesLocal>();
		Set<Entry<I_ClassAliasLocal, I_ClassReferencesLocal>> entries = refMap.entrySet();
		for (Entry<I_ClassAliasLocal, I_ClassReferencesLocal> e: entries) {
			I_ClassAliasLocal key = e.getKey();
			String name = key.getName();
			I_ClassReferencesLocal cr =  e.getValue();
			ClassReferencesLocalMutant crm = new ClassReferencesLocalMutant(cr);
			Set<I_ClassParentsLocal> refs =  cr.getReferencesLocal();
			for (I_ClassParentsLocal ref: refs) {
				if (!name.equals(ref.getName())) {
					I_ClassReferencesLocal refRef = refMap.get(ref);
					if (refRef != null) {
						Set<I_ClassParentsLocal> refRefRef = refRef.getReferencesLocal();
						if (refRefRef.contains(cr)) {
							crm.addCircularReferences(refRef);
						}
					}
				}
			}
			
			newRefMap.put(new ClassAliasLocal(key), new ClassReferencesLocal(crm));
		}
		refMap = newRefMap;
	}
	
	/**
	 * @diagram_sync with Discovery_ClassReferenceDiscovery.seq on 8/1/2014
	 * @diagram_sync with Discovery_ClassInstrumenter.seq on 8/1/2014
	 * 
	 * @param className
	 * @return
	 */
	public I_ClassReferencesLocal getReferences(I_ClassAliasLocal alias) {
		return refMap.get(alias);
	}

	/**
	 * note this is separated out so that
	 * the circular dependencies are calculated
	 * which require all down stream references to be 
	 * calculated first
	 */
	private void addToRefsCache() {
		Set<Entry<I_ClassAliasLocal, I_ClassReferencesLocal>> entries = refMap.entrySet();
		for (Entry<I_ClassAliasLocal, I_ClassReferencesLocal> e: entries) {
			discoveryMemory.putReferencesIfAbsent(e.getValue());
		}
	}
}
