package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.adligo.tests4j.models.shared.dependency.ClassAliasLocal;
import org.adligo.tests4j.models.shared.dependency.ClassReferencesLocal;
import org.adligo.tests4j.models.shared.dependency.ClassReferencesLocalMutant;
import org.adligo.tests4j.models.shared.dependency.I_ClassAliasLocal;
import org.adligo.tests4j.models.shared.dependency.I_ClassParentsLocal;
import org.adligo.tests4j.models.shared.dependency.I_ClassReferencesLocal;
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
public class ClassFullReferencesDiscovery {
	private I_Tests4J_Log log;
	private I_DiscoveryMemory discoveryMemory;
	/**
	 * this contains the initial references
	 */
	private Map<I_ClassAliasLocal, I_ClassReferencesLocal> refMap = new HashMap<I_ClassAliasLocal,I_ClassReferencesLocal>();
	private Set<I_ClassParentsLocal> initalRefsToIdentify = new HashSet<I_ClassParentsLocal>();
	private Set<I_ClassParentsLocal> fullRefsFound = new HashSet<I_ClassParentsLocal>();
	private ClassPreCirclesReferencesDiscovery cpcrd;
	
	public ClassFullReferencesDiscovery(I_CachedClassBytesClassLoader pClassLoader,
			I_Tests4J_Log pLog,  I_DiscoveryMemory dc) {
		log = pLog;
		discoveryMemory = dc;
		cpcrd = new ClassPreCirclesReferencesDiscovery(pClassLoader, pLog, dc);
	}
	
	public I_ClassReferencesLocal findOrLoad(Class<?> c) throws IOException, ClassNotFoundException {
		if (log.isLogEnabled(ClassFullReferencesDiscovery.class)) {
			log.log(".discoverAndLoad " + c.getName());
		}
		
		String className = c.getName();
		refMap.clear();
		initalRefsToIdentify.clear();
		fullRefsFound.clear();
		
		I_ClassReferencesLocal crefs =  discoveryMemory.getReferences(className);
		if (crefs != null) {
			return crefs;
		}
		if (discoveryMemory.isFiltered(c)) {
			I_ClassReferencesLocal toRet = new ClassReferencesLocal(cpcrd.findOrLoad(c));
			discoveryMemory.putReferencesIfAbsent(toRet);
			return toRet;
		}
		I_ClassReferencesLocal preCircleRefs = cpcrd.findOrLoad(c);
		refMap.put(new ClassAliasLocal(preCircleRefs), preCircleRefs);
		
		Set<I_ClassParentsLocal> refs = preCircleRefs.getReferencesLocal();
		for (I_ClassParentsLocal ref: refs) {
			I_ClassReferencesLocal preCircleDelegate = cpcrd.findOrLoad(ref.getTarget());
			refMap.put(new ClassAliasLocal(preCircleDelegate), preCircleDelegate);
		}
		
		ClassReferencesLocal toRet = calcCircles(preCircleRefs);
		discoveryMemory.putReferencesIfAbsent(toRet);;
		return toRet;
	}


	private ClassReferencesLocal calcCircles(I_ClassReferencesLocal preCircleRefs) {
		ClassReferencesLocalMutant crlm = new ClassReferencesLocalMutant(preCircleRefs);
		Collection<I_ClassReferencesLocal> entries = refMap.values();
		
		Set<I_ClassReferencesLocal> copy  = 
				new HashSet<I_ClassReferencesLocal>(entries);
		copy.remove(new ClassAliasLocal(crlm.getTarget()));
		for (I_ClassReferencesLocal cr: copy) {
			Set<I_ClassParentsLocal> refs =  cr.getReferencesLocal();
			if (refs != null) {
				if (refs.contains(crlm)) {
					crlm.addCircularReferences(cr);
				}
			}
		}
		return new ClassReferencesLocal(crlm);
	}
	
}
