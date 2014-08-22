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
import org.adligo.tests4j.models.shared.dependency.DependencyMutant;
import org.adligo.tests4j.models.shared.dependency.I_ClassAliasLocal;
import org.adligo.tests4j.models.shared.dependency.I_ClassDependenciesCache;
import org.adligo.tests4j.models.shared.dependency.I_ClassDependenciesLocal;
import org.adligo.tests4j.models.shared.dependency.I_ClassFilter;
import org.adligo.tests4j.models.shared.dependency.I_ClassParentsLocal;
import org.adligo.tests4j.models.shared.dependency.I_Dependency;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;
import org.adligo.tests4j_4jacoco.plugin.common.I_OrderedClassDependencies;
import org.adligo.tests4j_4jacoco.plugin.common.I_OrderedClassDiscovery;

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
public class OrderedClassDiscovery implements I_OrderedClassDiscovery {
	private I_Tests4J_Log log;
	private I_ClassFilter classFilter;
	private I_ClassDependenciesCache cache;
	private Map<I_ClassAliasLocal, I_ClassDependenciesLocal> refMap = new HashMap<I_ClassAliasLocal,I_ClassDependenciesLocal>();
	private I_ClassDependenciesDiscovery fullDependenciesDiscovery;
	private I_ClassDependenciesDiscovery circularDependenciesDiscovery;
	
	public OrderedClassDiscovery() {}
	
	/**
	 * @diagram_sync with InstrumentationOverview.seq on 8/20/2014
	 * @diagram_sync with DiscoveryOverview.seq on 8/20/2014
	 * 
	 * @see org.adligo.tests4j_4jacoco.plugin.discovery.I_OrderedClassDependenciesDiscovery#findOrLoad(java.lang.Class)
	 */
	@Override
	public I_OrderedClassDependencies findOrLoad(Class<?> c) throws IOException, ClassNotFoundException {
		if (log.isLogEnabled(OrderedClassDiscovery.class)) {
			log.log("ClassReferencesDiscovery.discoverAndLoad " + c.getName());
		}
		String className = c.getName();
		refMap.clear();
		I_ClassDependenciesLocal crefs =  cache.getDependencies(className);
		if (crefs != null) {
			refMap.put(new ClassAliasLocal(crefs), crefs);
			fillRefMapFromFullRef(crefs);
		} else {
			crefs = fillRefMapFromClass(c);
		}
		//@diagram_sync with DiscoveryOverview.seq on 8/17/2014
		List<String> refOrder = calculateRefOrder(c);
		return new OrderedClassDependencies(crefs, refOrder);
	}

	private void fillRefMapFromFullRef(I_ClassDependenciesLocal full) throws ClassNotFoundException, IOException {
		Set<I_ClassParentsLocal> refs = full.getDependenciesLocal();
		for (I_ClassParentsLocal ref: refs) {
			I_ClassDependenciesLocal refLoc = circularDependenciesDiscovery.findOrLoad(ref.getTarget());
			refMap.put(refLoc, refLoc);
		}
	}

	/**
	 * @diagram_sync with DiscoveryOverview.seq on 8/17/2014
	 * @param c
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private I_ClassDependenciesLocal fillRefMapFromClass(Class<?> c) throws ClassNotFoundException, IOException {
		//@diagram_sync with DiscoveryOverview.seq on 8/17/2014
		I_ClassDependenciesLocal initial = fullDependenciesDiscovery.findOrLoad(c);
		
		List<I_ClassParentsLocal> parents =  initial.getParentsLocal();
		for (I_ClassParentsLocal cpl : parents) {
			//@diagram_sync with DiscoveryOverview.seq on 8/17/2014
			I_ClassDependenciesLocal parentFull = circularDependenciesDiscovery.findOrLoad(cpl.getTarget());
			refMap.put(parentFull, parentFull);
		}
		
		Set<I_ClassParentsLocal> refs = initial.getDependenciesLocal();
		Set<I_ClassParentsLocal> refsCopy = new HashSet<I_ClassParentsLocal>(refs);
		refsCopy.removeAll(parents);
		for (I_ClassParentsLocal ref : refsCopy) {
			//@diagram_sync with DiscoveryOverview.seq on 8/17/2014
			I_ClassDependenciesLocal refFull = circularDependenciesDiscovery.findOrLoad(ref.getTarget());
			refMap.put(refFull, refFull);
		}
		
		//@diagram_sync with DiscoveryOverview.seq on 8/17/2014
		I_ClassDependenciesLocal full = circularDependenciesDiscovery.findOrLoad(c);
		refMap.put(full, full);
		Set<I_ClassParentsLocal> fullRefs = full.getDependenciesLocal();
		Set<I_ClassParentsLocal> fullRefsCopy = new HashSet<I_ClassParentsLocal>(fullRefs);
		fullRefsCopy.removeAll(parents);
		fullRefsCopy.removeAll(refsCopy);
		for (I_ClassParentsLocal ref : fullRefsCopy) {
			//@diagram_sync with DiscoveryOverview.seq on 8/17/2014
			I_ClassDependenciesLocal refFull = circularDependenciesDiscovery.findOrLoad(ref.getTarget());
			refMap.put(refFull, refFull);
		}
		return full;
	}
	
	/**
	 * @diagram_sync with DiscoveryOverview.seq on 8/17/2014
	 * 
	 * ok at this point either we have 
	 * just references, or a mix of references and 
	 * cached dependencies.  
	 * Calculate the most referenced in the group and order
	 * 
	 * This calculates a rough order of dependencies
	 * @return
	 */
	private List<String> calculateRefOrder(Class<?> c) {
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
					if (classFilter.isFiltered(dc)) {
						if (!toRet.contains(depName)) {
							toRet.add(depName);
						}
						it.remove();
					} else {
						I_ClassDependenciesLocal local = refMap.get(alias);
						if (local == null) {
							throw new NullPointerException("problem finding refs for " + depName 
									+ " on " + c);
						}
						Set<String> refNames =  local.getDependencyNames();
						refNames = new HashSet<String>(refNames);
						if (local.hasCircularDependencies()) {
							refNames.removeAll(local.getCircularDependenciesNames());
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
						if (classFilter.isFiltered(pc)) {
							if (!toRet.contains(parentName)) {
								 toRet.add(parentName);
							 }
						} else {
							I_ClassDependenciesLocal local = refMap.get(alias);
							Set<String> refNames =  local.getDependencyNames();
							refNames = new HashSet<String>(refNames);
							if (local.hasCircularDependencies()) {
								refNames.removeAll(local.getCircularDependenciesNames());
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
		
		Set<Entry<I_ClassAliasLocal, I_ClassDependenciesLocal>> refs =  refMap.entrySet();
		for (Entry<I_ClassAliasLocal,I_ClassDependenciesLocal> e: refs) {
			I_ClassAliasLocal key = e.getKey();
			String className = key.getName();
			I_ClassDependenciesLocal crs = e.getValue();
			Set<I_ClassParentsLocal> classes = crs.getDependenciesLocal();
			
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
	public I_ClassDependenciesLocal getReferences(I_ClassAliasLocal alias) {
		return refMap.get(alias);
	}

	public I_Tests4J_Log getLog() {
		return log;
	}

	public I_ClassFilter getClassFilter() {
		return classFilter;
	}

	public I_ClassDependenciesCache getCache() {
		return cache;
	}

	public I_ClassDependenciesDiscovery getFullDependenciesDiscovery() {
		return fullDependenciesDiscovery;
	}

	public I_ClassDependenciesDiscovery getCircularDependenciesDiscovery() {
		return circularDependenciesDiscovery;
	}

	public void setLog(I_Tests4J_Log log) {
		this.log = log;
	}

	public void setClassFilter(I_ClassFilter classFilter) {
		this.classFilter = classFilter;
	}

	public void setCache(I_ClassDependenciesCache cache) {
		this.cache = cache;
	}

	public void setFullDependenciesDiscovery(
			I_ClassDependenciesDiscovery classDependenciesDiscovery) {
		this.fullDependenciesDiscovery = classDependenciesDiscovery;
	}

	public void setCircularDependenciesDiscovery(
			I_ClassDependenciesDiscovery circularDependenciesDiscovery) {
		this.circularDependenciesDiscovery = circularDependenciesDiscovery;
	}

}
