package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
	private ClassInitialReferencesDiscovery cird;
	
	public ClassFullReferencesDiscovery(I_CachedClassBytesClassLoader pClassLoader,
			I_Tests4J_Log pLog,  I_DiscoveryMemory dc) {
		log = pLog;
		discoveryMemory = dc;
		cird = new ClassInitialReferencesDiscovery(pClassLoader, pLog, dc);
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
		I_ClassReferencesLocal initalRefs = cird.findOrLoad(c);
		refMap.put(new ClassAliasLocal(initalRefs), initalRefs);
		
		//ok get the parent inital references
		List<I_ClassParentsLocal> parents = initalRefs.getParentsLocal();
		for (I_ClassParentsLocal parent: parents) {
			Class<?> pc = parent.getTarget();
			I_ClassReferencesLocal parentRefs = cird.findOrLoad(pc);
			refMap.put(new ClassAliasLocal(parent), parentRefs);
			fill(parentRefs);
		}
		ClassReferencesLocal toRet = fill(initalRefs);
		discoveryMemory.putReferencesIfAbsent(toRet);
		return toRet;
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
	 * @param initalRefs
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	private ClassReferencesLocal fill(I_ClassReferencesLocal initalRefs) throws ClassNotFoundException, IOException {
		
		
		//should include the parents at this point in the references
		Set<I_ClassParentsLocal> refs =   initalRefs.getReferencesLocal();
		for (I_ClassParentsLocal ref: refs){
			initalRefsToIdentify.add(ref);
		}
		
		while (initalRefsToIdentify.size() >= 1) {
			I_ClassParentsLocal ref = initalRefsToIdentify.iterator().next();
			
			I_ClassReferencesLocal cached = discoveryMemory.getReferences(ref.getName());
			if (cached != null) {
				refMap.put(ref, cached);
				fullRefsFound.add(ref);
				initalRefsToIdentify.remove(0);
			}
			if (cached == null) {
				I_ClassReferencesLocal initalDelRefs = refMap.get(ref);
				if (initalDelRefs == null) {
					initalDelRefs = cird.findOrLoad(ref.getTarget());
					refMap.put(initalDelRefs, initalDelRefs);
				}
				Set<I_ClassParentsLocal> delRefsRefs = initalDelRefs.getReferencesLocal();
				for (I_ClassParentsLocal cpl: delRefsRefs) {
					if (!fullRefsFound.contains(cpl)) {
						initalRefsToIdentify.add(cpl);
					}
				}
			}
			initalRefsToIdentify.removeAll(refMap.keySet());
		}
		
		return build(initalRefs);
	}
	
	/**
	 * ok at this point all leaf references are either in 
	 * refMap;
	 * @return
	 */
	private ClassReferencesLocal build(I_ClassReferencesLocal initalRefs) {
		ClassReferencesLocalMutant crlm = new ClassReferencesLocalMutant(initalRefs);
		
		Set<Entry<I_ClassAliasLocal, I_ClassReferencesLocal>> entries = refMap.entrySet();
		for (Entry<I_ClassAliasLocal, I_ClassReferencesLocal> e: entries) {
			I_ClassReferencesLocal refs = e.getValue();
			crlm.addReferences(refs.getReferencesLocal());
		}
		calcCircles(crlm);
		return new ClassReferencesLocal(crlm);
	}

	private void calcCircles(ClassReferencesLocalMutant crlm) {
		Set<Entry<I_ClassAliasLocal, I_ClassReferencesLocal>> entries = refMap.entrySet();
		
		Set<Entry<I_ClassAliasLocal, I_ClassReferencesLocal>> copy  = 
				new HashSet<Entry<I_ClassAliasLocal, I_ClassReferencesLocal>>(entries);
		copy.remove(crlm);
		for (Entry<I_ClassAliasLocal, I_ClassReferencesLocal> e: copy) {
			I_ClassReferencesLocal cr =  e.getValue();
			
			Set<I_ClassParentsLocal> refs =  cr.getReferencesLocal();
			if (refs.contains(crlm)) {
				crlm.addCircularReferences(e.getKey());
			}
			
		}
	}
	
}
