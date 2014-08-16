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
import org.adligo.tests4j.models.shared.dependency.I_ClassFilter;
import org.adligo.tests4j.models.shared.dependency.I_ClassParentsLocal;
import org.adligo.tests4j.models.shared.dependency.I_ClassReferencesCache;
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
public class ClassPreCirclesReferencesDiscovery {
	private I_Tests4J_Log log;
	private I_ClassFilter classFilter;
	private I_ClassReferencesCache preCirclesCache;
	/**
	 * this contains the initial references
	 */
	private Map<I_ClassAliasLocal, I_ClassReferencesLocal> refMap = new HashMap<I_ClassAliasLocal,I_ClassReferencesLocal>();
	private Set<I_ClassParentsLocal> initalRefsToIdentify = new HashSet<I_ClassParentsLocal>();
	private Set<I_ClassParentsLocal> fullRefsFound = new HashSet<I_ClassParentsLocal>();
	private ClassInitialReferencesDiscovery cird;
	
	public ClassPreCirclesReferencesDiscovery(I_CachedClassBytesClassLoader pClassLoader,
			I_Tests4J_Log pLog,  I_DiscoveryMemory dc) {
		log = pLog;
		classFilter = dc;
		preCirclesCache = dc.getPreCirclesReferencesCache();
		cird = new ClassInitialReferencesDiscovery(pClassLoader, pLog, dc);
	}
	
	public I_ClassReferencesLocal findOrLoad(Class<?> c) throws IOException, ClassNotFoundException {
		if (log.isLogEnabled(ClassPreCirclesReferencesDiscovery.class)) {
			log.log(".discoverAndLoad " + c.getName());
		}
		
		String className = c.getName();
		refMap.clear();
		initalRefsToIdentify.clear();
		fullRefsFound.clear();
		
		I_ClassReferencesLocal crefs =  preCirclesCache.getReferences(className);
		if (crefs != null) {
			return crefs;
		}
		if (classFilter.isFiltered(c)) {
			I_ClassReferencesLocal toRet = new ClassReferencesLocal(cird.findOrLoad(c));
			preCirclesCache.putReferencesIfAbsent(toRet);
			return toRet;
		}
		I_ClassReferencesLocal initalRefs = cird.findOrLoad(c);
		refMap.put(new ClassAliasLocal(initalRefs), initalRefs);
		
		//ok get the parent inital references
		List<I_ClassParentsLocal> parents = initalRefs.getParentsLocal();
		for (I_ClassParentsLocal parent: parents) {
			Class<?> pc = parent.getTarget();
			I_ClassReferencesLocal parentRefs = cird.findOrLoad(pc);
			refMap.put(new ClassAliasLocal(parent), parentRefs);
			
			I_ClassReferencesLocal prefs =  preCirclesCache.getReferences(className);
			if (prefs != null) {
				fullRefsFound.add(prefs);
				refMap.put(prefs, prefs);
			} else if (classFilter.isFiltered(pc)) {
				prefs = new ClassReferencesLocal(cird.findOrLoad(pc));
				preCirclesCache.putReferencesIfAbsent(prefs);
				fullRefsFound.add(prefs);
				refMap.put(prefs, prefs);
			} else {
				fill(parentRefs);
			}
		}
		ClassReferencesLocal toRet = fill(initalRefs);
		preCirclesCache.putReferencesIfAbsent(toRet);
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
		if (refs != null) {
			for (I_ClassParentsLocal ref: refs){
				initalRefsToIdentify.add(ref);
			}
		}
		while (initalRefsToIdentify.size() >= 1) {
			I_ClassParentsLocal ref = initalRefsToIdentify.iterator().next();
			
			I_ClassReferencesLocal cached = preCirclesCache.getReferences(ref.getName());
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
		return new ClassReferencesLocal(crlm);
	}

}
