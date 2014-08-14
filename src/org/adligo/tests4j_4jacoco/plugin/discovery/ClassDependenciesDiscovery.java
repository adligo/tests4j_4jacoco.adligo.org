package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.adligo.tests4j.models.shared.dependency.ClassDependencies;
import org.adligo.tests4j.models.shared.dependency.ClassDependenciesMutant;
import org.adligo.tests4j.models.shared.dependency.Dependency;
import org.adligo.tests4j.models.shared.dependency.DependencyMutant;
import org.adligo.tests4j.models.shared.dependency.I_ClassDependencies;
import org.adligo.tests4j.models.shared.dependency.I_ClassReferences;
import org.adligo.tests4j.models.shared.dependency.I_Dependency;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;
import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;

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
	private I_DiscoveryMemory memory;
	private ClassReferencesDiscovery classReferencesDiscovery;
	private List<String> depsToFind = new ArrayList<String>();
	private Map<String,I_ClassDependencies> localDeps = new HashMap<String,I_ClassDependencies>();
	
	public ClassDependenciesDiscovery(I_CachedClassBytesClassLoader pClassLoader,
			I_Tests4J_Log pLog,  I_DiscoveryMemory dc) {
		log = pLog;
		memory = dc;
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
		
		depsToFind.clear();
		localDeps.clear();
		String name = c.getName();
		//@diagram_sync with Discovery_ClassInstrumenter.seq on 8/1/2014
		I_ClassDependencies toRet = memory.getDependencies(name);
		//@diagram_sync with Discovery_ClassInstrumenter.seq on 8/1/2014
		if (toRet == null) {
			if (log.isLogEnabled(ClassDependenciesDiscovery.class)) {
				log.log("ClassDependenciesDiscovery.discoverAndLoad " + c.getName());
			}
			//@diagram_sync with Discovery_ClassInstrumenter.seq on 8/1/2014
			List<String> refOrder = classReferencesDiscovery.discoverAndLoad(c);
			I_ClassReferences classRefs = classReferencesDiscovery.getReferences(name);
			for (String ro: refOrder) {
				if (!memory.isFiltered(ro)) {
					depsToFind.add(ro);
				}
			}
			depsToFind.remove(name);
			if (depsToFind.size() >= 1) {
				findOrCreateRefDeps();
			}
			//the dependency for the param class may have been 
			// created as part of the findOrCreateRefDeps (when there are circular refs)
			toRet = localDeps.get(name);
			if (toRet == null) {
				toRet = createDependencies(name, refOrder, classRefs);
			} else if (toRet.hasCircularReferences()) {
				//cache the circular dependency
				memory.putDependenciesIfAbsent(toRet);
			}
		}
		return toRet;
	}

	private I_ClassDependencies createDependencies(String name,
			List<String> refOrder, I_ClassReferences classRefs) {
		I_ClassDependencies toRet;
		refOrder.remove(name);
		
		ClassDependenciesMutant cdm = new ClassDependenciesMutant();
		cdm.setClassName(name);
		if (classRefs.hasCircularReferences()) {
			cdm.setCircularReferences(classRefs.getCircularReferences());
		}
		for (String ref: refOrder) {
			I_ClassDependencies cds =  localDeps.get(ref);
			if (cds == null) {
				DependencyMutant dm = (DependencyMutant) cdm.getDependency(ref);
				if (dm != null) {
					dm.addReference();
				} else {
					dm = new DependencyMutant();
					dm.setClassName(ref);
					dm.addReference();
					cdm.addDependency(dm);
				}
			} else {
				List<I_Dependency> cdDeps = cds.getDependencies();
				for (I_Dependency d: cdDeps) {
					if (ref.equals(d.getClassName())) {
						DependencyMutant dm = new DependencyMutant(d);
						dm.addReference();
						d = dm;
					}
					cdm.addDependency(d);
				}
			}
		}
		if (cdm.getDependency(name) == null) {
			DependencyMutant dm = new DependencyMutant();
			dm.setClassName(name);
			cdm.addDependency(dm);
		}
		toRet = new ClassDependencies(cdm);
		localDeps.put(name, toRet);
		if (!toRet.hasCircularReferences()) {
			memory.putDependenciesIfAbsent(toRet);
		}
		return toRet;
	}

	private void findOrCreateRefDeps() throws ClassNotFoundException,
			IOException {
		while (depsToFind.size() >= 1) {
			String dep = depsToFind.get(0);
			
			I_ClassDependencies icd = localDeps.get(dep);
			if (icd == null) {
				icd = memory.getDependencies(dep);
				if (icd == null) {
					Class<?> depClass = Class.forName(dep);
					List<String>  depOrder = classReferencesDiscovery.discoverAndLoad(depClass);
					for (String depO: depOrder){
						if (!depsToFind.contains(depO)) {
							if (!localDeps.containsKey(depO)) {
								if ( !memory.isFiltered(depO)) {
									depsToFind.add(depO);
								}
							}
						}
					}
					I_ClassReferences refs = classReferencesDiscovery.getReferences(dep);
					createDependencies(dep, depOrder, refs);
				}
				depsToFind.remove(dep);
			}
		}
	}

}
