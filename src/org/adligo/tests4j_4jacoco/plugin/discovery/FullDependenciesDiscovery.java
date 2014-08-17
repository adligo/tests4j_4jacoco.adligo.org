package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.adligo.tests4j.models.shared.dependency.ClassAliasLocal;
import org.adligo.tests4j.models.shared.dependency.ClassDependenciesLocal;
import org.adligo.tests4j.models.shared.dependency.ClassDependenciesLocalMutant;
import org.adligo.tests4j.models.shared.dependency.I_ClassAliasLocal;
import org.adligo.tests4j.models.shared.dependency.I_ClassFilter;
import org.adligo.tests4j.models.shared.dependency.I_ClassParentsLocal;
import org.adligo.tests4j.models.shared.dependency.I_ClassDependenciesCache;
import org.adligo.tests4j.models.shared.dependency.I_ClassDependenciesLocal;
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
public class FullDependenciesDiscovery implements I_ClassDependenciesDiscovery {
	private I_Tests4J_Log log;
	private I_ClassFilter classFilter;
	private I_ClassDependenciesCache cache;
	/**
	 * this contains the initial references
	 */
	private Map<I_ClassAliasLocal, I_ClassDependenciesLocal> refMap = new HashMap<I_ClassAliasLocal,I_ClassDependenciesLocal>();
	private Set<I_ClassParentsLocal> initalRefsToIdentify = new HashSet<I_ClassParentsLocal>();
	private Set<I_ClassParentsLocal> fullRefsFound = new HashSet<I_ClassParentsLocal>();
	private I_ClassDependenciesDiscovery initialDependenciesDiscovery;
	
	public FullDependenciesDiscovery() {}
	
	/**
	 * @diagram_sync with DiscoveryOverview.seq on 8/17/2014
	 * @see I_ClassDependenciesDiscovery#findOrLoad(Class)
	 */
	public I_ClassDependenciesLocal findOrLoad(Class<?> c) throws IOException, ClassNotFoundException {
		if (log.isLogEnabled(FullDependenciesDiscovery.class)) {
			log.log(".discoverAndLoad " + c.getName());
		}
		
		String className = c.getName();
		refMap.clear();
		initalRefsToIdentify.clear();
		fullRefsFound.clear();
		
		I_ClassDependenciesLocal crefs =  cache.getDependencies(className);
		if (crefs != null) {
			return crefs;
		}
		if (classFilter.isFiltered(c)) {
			I_ClassDependenciesLocal toRet = new ClassDependenciesLocal(initialDependenciesDiscovery.findOrLoad(c));
			cache.putDependenciesIfAbsent(toRet);
			return toRet;
		}
		I_ClassDependenciesLocal initalRefs = initialDependenciesDiscovery.findOrLoad(c);
		refMap.put(new ClassAliasLocal(initalRefs), initalRefs);
		
		fillRefMapWithParents(className, initalRefs);
		ClassDependenciesLocal toRet = fillRefMap(initalRefs);
		cache.putDependenciesIfAbsent(toRet);
		return toRet;
	}

	/**
	 * @diagram_sync with DiscoveryOverview.seq on 8/17/2014
	 * @param className
	 * @param initalRefs
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	protected void fillRefMapWithParents(String className,
			I_ClassDependenciesLocal initalRefs) throws IOException,
			ClassNotFoundException {
		//ok get the parent inital references
		List<I_ClassParentsLocal> parents = initalRefs.getParentsLocal();
		for (I_ClassParentsLocal parent: parents) {
			Class<?> pc = parent.getTarget();
			I_ClassDependenciesLocal parentRefs = initialDependenciesDiscovery.findOrLoad(pc);
			refMap.put(new ClassAliasLocal(parent), parentRefs);
			
			I_ClassDependenciesLocal prefs =  cache.getDependencies(className);
			if (prefs != null) {
				fullRefsFound.add(prefs);
				refMap.put(prefs, prefs);
			} else if (classFilter.isFiltered(pc)) {
				prefs = new ClassDependenciesLocal(initialDependenciesDiscovery.findOrLoad(pc));
				cache.putDependenciesIfAbsent(prefs);
				fullRefsFound.add(prefs);
				refMap.put(prefs, prefs);
			} else {
				fillRefMap(parentRefs);
			}
		}
	}

	/**
	 * this method finds all delegate references for the initalRefs
	 * and populates the fullRefMap and discoveryMemory reference cache,
	 * with a completed I_ClassReferencesLocal instance, which includes
	 * everything;
	 * parents
	 * circularReferences
	 * delegateReferences
	 * it also may add initial references to the refMap
	 * 
	 * @diagram_sync with DiscoveryOverview.seq on 8/17/2014
	 * @param initalRefs
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	private ClassDependenciesLocal fillRefMap(I_ClassDependenciesLocal initalRefs) throws ClassNotFoundException, IOException {
	
		
		//should include the parents at this point in the references
		Set<I_ClassParentsLocal> refs =   initalRefs.getDependenciesLocal();
		if (refs != null) {
			for (I_ClassParentsLocal ref: refs){
				initalRefsToIdentify.add(ref);
			}
		}
		while (initalRefsToIdentify.size() >= 1) {
			I_ClassParentsLocal ref = initalRefsToIdentify.iterator().next();
			
			I_ClassDependenciesLocal cached = cache.getDependencies(ref.getName());
			if (cached != null) {
				refMap.put(ref, cached);
				fullRefsFound.add(ref);
				initalRefsToIdentify.remove(0);
			}
			if (cached == null) {
				I_ClassDependenciesLocal initalDelRefs = refMap.get(ref);
				if (initalDelRefs == null) {
					initalDelRefs = initialDependenciesDiscovery.findOrLoad(ref.getTarget());
					refMap.put(initalDelRefs, initalDelRefs);
				}
				Set<I_ClassParentsLocal> delRefsRefs = initalDelRefs.getDependenciesLocal();
				if (delRefsRefs != null) {
					for (I_ClassParentsLocal cpl: delRefsRefs) {
						if (!fullRefsFound.contains(cpl)) {
							initalRefsToIdentify.add(cpl);
						}
					}
				}
			}
			initalRefsToIdentify.removeAll(refMap.keySet());
		}
		
		return buildModel(initalRefs);
	}
	
	/**
	 * All leaf references are in the refMap,
	 * this method puts them in the ClassDependenciesLocal returned.
	 * @diagram_sync with DiscoveryOverview.seq on 8/17/2014
	 * @return
	 */
	private ClassDependenciesLocal buildModel(I_ClassDependenciesLocal initalRefs) {
		ClassDependenciesLocalMutant crlm = new ClassDependenciesLocalMutant(initalRefs);
		
		Set<Entry<I_ClassAliasLocal, I_ClassDependenciesLocal>> entries = refMap.entrySet();
		for (Entry<I_ClassAliasLocal, I_ClassDependenciesLocal> e: entries) {
			I_ClassDependenciesLocal refs = e.getValue();
			crlm.addReferences(refs.getDependenciesLocal());
		}
		return new ClassDependenciesLocal(crlm);
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

	public I_ClassDependenciesDiscovery getInitialDependenciesDiscovery() {
		return initialDependenciesDiscovery;
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

	public void setInitialDependenciesDiscovery(
			I_ClassDependenciesDiscovery classDependenciesDiscovery) {
		this.initialDependenciesDiscovery = classDependenciesDiscovery;
	}

}
