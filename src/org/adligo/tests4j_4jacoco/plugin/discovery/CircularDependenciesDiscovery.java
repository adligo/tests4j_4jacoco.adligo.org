package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.adligo.tests4j.models.shared.dependency.ClassAliasLocal;
import org.adligo.tests4j.models.shared.dependency.ClassDependenciesLocal;
import org.adligo.tests4j.models.shared.dependency.ClassDependenciesLocalMutant;
import org.adligo.tests4j.models.shared.dependency.I_ClassAliasLocal;
import org.adligo.tests4j.models.shared.dependency.I_ClassDependenciesCache;
import org.adligo.tests4j.models.shared.dependency.I_ClassDependenciesLocal;
import org.adligo.tests4j.models.shared.dependency.I_ClassFilter;
import org.adligo.tests4j.models.shared.dependency.I_ClassParentsLocal;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;

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
public class CircularDependenciesDiscovery implements I_ClassDependenciesDiscovery {
	private I_Tests4J_Log log;
	private I_ClassFilter classFilter;
	private I_ClassDependenciesCache cache;
	/**
	 * this contains the initial references
	 */
	private Map<I_ClassAliasLocal, I_ClassDependenciesLocal> refMap = new HashMap<I_ClassAliasLocal,I_ClassDependenciesLocal>();
	private Set<I_ClassParentsLocal> initalRefsToIdentify = new HashSet<I_ClassParentsLocal>();
	private Set<I_ClassParentsLocal> fullRefsFound = new HashSet<I_ClassParentsLocal>();
	private I_ClassDependenciesDiscovery fullDependenciesDiscovery;
	
	public CircularDependenciesDiscovery() {}
	
	public I_ClassDependenciesLocal findOrLoad(Class<?> c) throws IOException, ClassNotFoundException {
		if (log.isLogEnabled(CircularDependenciesDiscovery.class)) {
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
			I_ClassDependenciesLocal toRet = new ClassDependenciesLocal(fullDependenciesDiscovery.findOrLoad(c));
			cache.putDependenciesIfAbsent(toRet);
			return toRet;
		}
		I_ClassDependenciesLocal preCircleRefs = fullDependenciesDiscovery.findOrLoad(c);
		refMap.put(new ClassAliasLocal(preCircleRefs), preCircleRefs);
		
		Set<I_ClassParentsLocal> refs = preCircleRefs.getDependenciesLocal();
		for (I_ClassParentsLocal ref: refs) {
			I_ClassDependenciesLocal preCircleDelegate = fullDependenciesDiscovery.findOrLoad(ref.getTarget());
			refMap.put(new ClassAliasLocal(preCircleDelegate), preCircleDelegate);
		}
		
		ClassDependenciesLocal toRet = calcCircles(preCircleRefs);
		cache.putDependenciesIfAbsent(toRet);;
		return toRet;
	}


	private ClassDependenciesLocal calcCircles(I_ClassDependenciesLocal preCircleRefs) {
		ClassDependenciesLocalMutant crlm = new ClassDependenciesLocalMutant(preCircleRefs);
		Collection<I_ClassDependenciesLocal> entries = refMap.values();
		
		Set<I_ClassDependenciesLocal> copy  = 
				new HashSet<I_ClassDependenciesLocal>(entries);
		copy.remove(new ClassAliasLocal(crlm.getTarget()));
		for (I_ClassDependenciesLocal cr: copy) {
			Set<I_ClassParentsLocal> refs =  cr.getDependenciesLocal();
			if (refs != null) {
				if (refs.contains(crlm)) {
					crlm.addCircularReferences(cr);
				}
			}
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

	public I_ClassDependenciesDiscovery getFullDependenciesDiscovery() {
		return fullDependenciesDiscovery;
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
	
}
