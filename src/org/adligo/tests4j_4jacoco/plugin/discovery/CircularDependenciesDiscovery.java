package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.adligo.tests4j.models.shared.association.ClassAssociationsLocal;
import org.adligo.tests4j.models.shared.association.ClassAssociationsLocalMutant;
import org.adligo.tests4j.models.shared.association.I_ClassAssociationsCache;
import org.adligo.tests4j.models.shared.association.I_ClassAssociationsLocal;
import org.adligo.tests4j.models.shared.association.I_ClassParentsLocal;
import org.adligo.tests4j.run.helpers.I_ClassFilter;
import org.adligo.tests4j.shared.asserts.reference.ClassAliasLocal;
import org.adligo.tests4j.shared.asserts.reference.I_ClassAliasLocal;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;

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
	private I_ClassAssociationsCache cache;
	/**
	 * this contains the initial references
	 */
	private Map<I_ClassAliasLocal, I_ClassAssociationsLocal> refMap = new HashMap<I_ClassAliasLocal,I_ClassAssociationsLocal>();
	private Set<I_ClassParentsLocal> initalRefsToIdentify = new HashSet<I_ClassParentsLocal>();
	private Set<I_ClassParentsLocal> fullRefsFound = new HashSet<I_ClassParentsLocal>();
	private I_ClassDependenciesDiscovery fullDependenciesDiscovery;
	
	public CircularDependenciesDiscovery() {}
	
	/**
	 * @diagram_sync with DiscoveryOverview.seq on 8/17/2014
	 */
	public I_ClassAssociationsLocal findOrLoad(Class<?> c) throws IOException, ClassNotFoundException {
		if (log.isLogEnabled(CircularDependenciesDiscovery.class)) {
			log.log(".discoverAndLoad " + c.getName());
		}
		
		String className = c.getName();
		refMap.clear();
		initalRefsToIdentify.clear();
		fullRefsFound.clear();
		
		I_ClassAssociationsLocal crefs =  cache.getDependencies(className);
		if (crefs != null) {
			return crefs;
		}
		if (classFilter.isFiltered(c)) {
			I_ClassAssociationsLocal toRet = new ClassAssociationsLocal(fullDependenciesDiscovery.findOrLoad(c));
			cache.putDependenciesIfAbsent(toRet);
			return toRet;
		}
		I_ClassAssociationsLocal preCircleRefs = fullDependenciesDiscovery.findOrLoad(c);
		refMap.put(new ClassAliasLocal(preCircleRefs), preCircleRefs);
		
		Set<I_ClassParentsLocal> refs = preCircleRefs.getDependenciesLocal();
		for (I_ClassParentsLocal ref: refs) {
			I_ClassAssociationsLocal preCircleDelegate = fullDependenciesDiscovery.findOrLoad(ref.getTarget());
			refMap.put(new ClassAliasLocal(preCircleDelegate), preCircleDelegate);
		}
		
		ClassAssociationsLocal toRet = calcCircles(preCircleRefs);
		cache.putDependenciesIfAbsent(toRet);;
		return toRet;
	}


	/**
	 * @diagram_sync with DiscoveryOverview.seq on 8/17/2014
	 * @param preCircleRefs
	 * @return
	 */
	private ClassAssociationsLocal calcCircles(I_ClassAssociationsLocal preCircleRefs) {
		ClassAssociationsLocalMutant crlm = new ClassAssociationsLocalMutant(preCircleRefs);
		Collection<I_ClassAssociationsLocal> entries = refMap.values();
		
		Set<I_ClassAssociationsLocal> copy  = 
				new HashSet<I_ClassAssociationsLocal>(entries);
		copy.remove(new ClassAliasLocal(crlm.getTarget()));
		for (I_ClassAssociationsLocal cr: copy) {
			Set<I_ClassParentsLocal> refs =  cr.getDependenciesLocal();
			if (refs != null) {
				if (refs.contains(crlm)) {
					crlm.addCircularReferences(cr);
				}
			}
		}
		return new ClassAssociationsLocal(crlm);
	}

	public I_Tests4J_Log getLog() {
		return log;
	}

	public I_ClassFilter getClassFilter() {
		return classFilter;
	}

	public I_ClassAssociationsCache getCache() {
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

	public void setCache(I_ClassAssociationsCache cache) {
		this.cache = cache;
	}

	public void setFullDependenciesDiscovery(
			I_ClassDependenciesDiscovery classDependenciesDiscovery) {
		this.fullDependenciesDiscovery = classDependenciesDiscovery;
	}
	
}
