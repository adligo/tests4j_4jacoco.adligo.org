package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.adligo.tests4j.models.shared.dependency.ClassAlias;
import org.adligo.tests4j.models.shared.dependency.ClassAliasLocal;
import org.adligo.tests4j.models.shared.dependency.ClassReferencesLocal;
import org.adligo.tests4j.models.shared.dependency.DependencyMutant;
import org.adligo.tests4j.models.shared.dependency.I_ClassAliasLocal;
import org.adligo.tests4j.models.shared.dependency.I_ClassParentsLocal;
import org.adligo.tests4j.models.shared.dependency.I_ClassReferencesLocal;
import org.adligo.tests4j.models.shared.dependency.I_Dependency;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;
import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;

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
	private I_Tests4J_Log log;
	private I_DiscoveryMemory discoveryMemory;
	private Map<I_ClassAliasLocal, I_ClassReferencesLocal> refMap = new HashMap<I_ClassAliasLocal,I_ClassReferencesLocal>();
	private ClassInitialReferencesDiscovery cird;
	private ClassFullReferencesDiscovery cfrdFull;
	
	public ClassReferencesDiscovery(I_CachedClassBytesClassLoader pClassLoader,
			I_Tests4J_Log pLog,  I_DiscoveryMemory dc) {
		log = pLog;
		discoveryMemory = dc;
		cird = new ClassInitialReferencesDiscovery(pClassLoader, pLog, dc);
		cfrdFull = new ClassFullReferencesDiscovery(pClassLoader, pLog, dc);
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
			fillRefMapFromFullRef(crefs);
		} else {
			fillRefMapFromClass(c);
		}
		
		List<String> refOrder = calcRefOrder(c);
		return refOrder;
	}

	private void fillRefMapFromFullRef(I_ClassReferencesLocal full) throws ClassNotFoundException, IOException {
		Set<I_ClassParentsLocal> refs = full.getReferencesLocal();
		for (I_ClassParentsLocal ref: refs) {
			I_ClassReferencesLocal refLoc = cfrdFull.findOrLoad(ref.getTarget());
			refMap.put(refLoc, refLoc);
		}
	}

	private void fillRefMapFromClass(Class<?> c) throws ClassNotFoundException, IOException {
		I_ClassReferencesLocal initial = cird.findOrLoad(c);
		
		List<I_ClassParentsLocal> parents =  initial.getParentsLocal();
		for (I_ClassParentsLocal cpl : parents) {
			I_ClassReferencesLocal parentFull = cfrdFull.findOrLoad(cpl.getTarget());
			refMap.put(parentFull, parentFull);
		}
		
		Set<I_ClassParentsLocal> refs = initial.getReferencesLocal();
		Set<I_ClassParentsLocal> refsCopy = new HashSet<I_ClassParentsLocal>(refs);
		refsCopy.removeAll(parents);
		for (I_ClassParentsLocal ref : refsCopy) {
			I_ClassReferencesLocal refFull = cfrdFull.findOrLoad(ref.getTarget());
			refMap.put(refFull, refFull);
		}
		
		I_ClassReferencesLocal full = cfrdFull.findOrLoad(c);
		refMap.put(full, full);
		Set<I_ClassParentsLocal> fullRefs = full.getReferencesLocal();
		Set<I_ClassParentsLocal> fullRefsCopy = new HashSet<I_ClassParentsLocal>(fullRefs);
		fullRefsCopy.removeAll(parents);
		fullRefsCopy.removeAll(refsCopy);
		for (I_ClassParentsLocal ref : fullRefsCopy) {
			I_ClassReferencesLocal refFull = cfrdFull.findOrLoad(ref.getTarget());
			refMap.put(refFull, refFull);
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

}
