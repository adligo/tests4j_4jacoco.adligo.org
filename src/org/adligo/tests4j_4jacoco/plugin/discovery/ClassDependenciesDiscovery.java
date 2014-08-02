package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;
import org.adligo.tests4j.run.discovery.ClassDependencies;
import org.adligo.tests4j.run.discovery.ClassDependenciesMutant;
import org.adligo.tests4j.run.discovery.ClassReferencesMutant;
import org.adligo.tests4j.run.discovery.DependencyMutant;
import org.adligo.tests4j.run.discovery.I_ClassDependencies;
import org.adligo.tests4j.run.discovery.I_ClassDependenciesCache;
import org.adligo.tests4j.run.discovery.I_ClassReferences;
import org.adligo.tests4j.run.discovery.I_Dependency;
import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;
import org.objectweb.asm.Opcodes;

/**
 * a model like (non thread safe) class that loads classes into
 * the class loader, discovers 
 * references in the class passed in,
 * and makes sure all things down the reference
 * tree are loaded.
 * 
 *    The I_CachedClassBytesClassLoader is shared memory between threads.
 * Also this model keeps a cache of the references for classes
 * it reads, so it doesn't need to re-ASM byte code read them.
 * 
 * 
 * @author scott
 *
 */
public class ClassDependenciesDiscovery {
	private I_Tests4J_Log log;
	private I_DiscoveryMemory dependencyCache;
	private ClassReferencesDiscovery classReferencesDiscovery;
	
	public ClassDependenciesDiscovery(I_CachedClassBytesClassLoader pClassLoader,
			I_Tests4J_Log pLog,  I_DiscoveryMemory dc) {
		log = pLog;
		dependencyCache = dc;
		classReferencesDiscovery = new ClassReferencesDiscovery(pClassLoader, pLog, dc);
	}
	
	/**
	 * @diagram_sync with Discovery_ClassInstrumenter.seq on 8/1/2014
	 * 
	 * @param c
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public I_ClassDependencies discoverAndLoad(Class<?> c) throws IOException, ClassNotFoundException {
		String name = c.getName();
		//@diagram_sync with Discovery_ClassInstrumenter.seq on 8/1/2014
		I_ClassDependencies toRet = dependencyCache.get(name);
		//@diagram_sync with Discovery_ClassInstrumenter.seq on 8/1/2014
		if (toRet == null) {
			//@diagram_sync with Discovery_ClassInstrumenter.seq on 8/1/2014
			ClassDependenciesMutant top = new ClassDependenciesMutant();
			top.setClazzName(name);
			//@diagram_sync with Discovery_ClassInstrumenter.seq on 8/1/2014
			List<String> refOrder = classReferencesDiscovery.discoverAndLoad(c);
			
			refOrder.remove(name);
			for (String className: refOrder) {
				if (log.isLogEnabled(ClassDependenciesDiscovery.class)) {
					log.log("ClassDependenciesDiscovery " + name + " calcDependencies on " + className + " ");
				}
				//@diagram_sync with Discovery_ClassInstrumenter.seq on 8/1/2014
				ClassDependenciesMutant deps = calcDependencies(className, name);
				top.add(deps);
			}
			DependencyMutant dm = new DependencyMutant();
			dm.setClazzName(name);
			top.addDependency(dm);
			
			toRet = new ClassDependencies(top);
			dependencyCache.putIfAbsent(toRet);
		}
		
		return toRet;
	}

	
	/**
	 * this caches dependencies 
	 * @param name
	 */
	private ClassDependenciesMutant calcDependencies(String name, String parentName) {
		//@diagram_sync with Discovery_ClassInstrumenter.seq on 8/1/2014
		I_ClassDependencies toRet = dependencyCache.get(name);
		//@diagram_sync with Discovery_ClassInstrumenter.seq on 8/1/2014
		if (toRet != null) {
			return new ClassDependenciesMutant(toRet);
		}
		//@diagram_sync with Discovery_ClassInstrumenter.seq on 8/1/2014
		ClassDependenciesMutant target = new ClassDependenciesMutant();
		target.setClazzName(name);
		if (log.isLogEnabled(ClassDependenciesDiscovery.class)) {
			log.log("ClassDependenciesDiscovery " + parentName + " calcDependenciesFromReferences on " + name + " ");
		}
		calcDependenciesFromReferences(name, target);
		dependencyCache.putIfAbsent(new ClassDependencies(target));
		return target;
	}
	
	private void calcDependenciesFromReferences(String name, ClassDependenciesMutant target) {
		//@diagram_sync with Discovery_ClassInstrumenter.seq on 8/1/2014
		I_ClassReferences crs =  classReferencesDiscovery.getReferences(name);
		Stack<String> refTree = new Stack<String>();
		doRecursion(crs, refTree, target);
	}
	
	/**
	 * ok there wasn't one in cache add all 
	 * references local and pass through references
	 * to the target.   
	 * @param refs
	 * @param refTree
	 * @param target
	 */
	private void doRecursion(I_ClassReferences refs, Stack<String> refTree, ClassDependenciesMutant target) {
		String name = refs.getClassName();
		if (refTree.contains(name)) {
			//block stack overflow
			return;
		}
		refTree.add(name);
		
		Set<String> references = refs.getReferences();
		Set<String> refsLocal = new HashSet<String>(references);
		refsLocal.remove(refs.getClassName());
		
		for (String ref: refsLocal) {
			DependencyMutant dm =  target.getDependency(ref);
			if (dm == null) {
				dm = new DependencyMutant();
				dm.setClazzName(ref);
				dm.addReference();
				target.addDependency(dm);
			} else {
				dm.addReference();
			}
			I_ClassReferences refsRefs = classReferencesDiscovery.getReferences(ref);
			doRecursion(refsRefs, refTree, target);
		}
		DependencyMutant dm = target.getDependency(name);
		if (dm == null) {
			dm = new DependencyMutant();
			dm.setClazzName(name);
			target.addDependency(dm);
		} 
		refTree.remove(name);
	}
}
