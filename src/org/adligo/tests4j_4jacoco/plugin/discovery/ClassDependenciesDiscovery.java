package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	private Set<String> ignoredPackages = Collections.singleton("java.");
	private Map<String,ClassReferencesMutant> refMap = new HashMap<String,ClassReferencesMutant>();
	private ClassReferencesDiscovery classReferencesDiscovery;
	
	public ClassDependenciesDiscovery(I_CachedClassBytesClassLoader pClassLoader,
			I_Tests4J_Log pLog,  I_DiscoveryMemory dc) {
		log = pLog;
		dependencyCache = dc;
		classReferencesDiscovery = new ClassReferencesDiscovery(pClassLoader, pLog, dc);
	}
	
	public I_ClassDependencies discoverAndLoad(Class<?> c) throws IOException, ClassNotFoundException {
		List<String> refOrder = classReferencesDiscovery.discoverAndLoad(c);
		
		refOrder.remove(c.getName());
		for (String className: refOrder) {
			if (dependencyCache.get(className) == null) {
				calcDependencies(className);
			}
		}
		//ok all dependencies are calculate except the input classes dependencies
		return calcDependencies(c.getName());
	}

	
	/**
	 * this caches dependencies 
	 * @param name
	 */
	private I_ClassDependencies calcDependencies(String name) {
		Set<String> working = new HashSet<String>();
		
		return calcDependencies(name, working);
	}
	
	/**
	 * a recursive method to try to correctly calculate
	 * dependencies, of course there could be circular references in
	 * here so the working set keeps stack overflows from occurring.
	 * @param name
	 * @param working
	 */
	private I_ClassDependencies calcDependencies(String name, Set<String> working) {
		if (log.isLogEnabled(ClassDependenciesDiscovery.class)) {
			log.log("calcDependencies " + name);
		}
		working.add(name);
		ClassDependenciesMutant cdm = new ClassDependenciesMutant();
		cdm.setClazzName(name);
		
		I_ClassReferences crm = refMap.get(name);
		Set<String> topNames = crm.getReferences();
		
		Set<String> topNamesClone = new HashSet<String>(topNames);
		topNamesClone.remove(name);
		for (String tn: topNamesClone) {
			I_ClassDependencies classDeps =  dependencyCache.get(tn);
			if (classDeps == null) {
				 if (working.contains(tn)) {
					//there is some sort of circular reference
					 //in the reference, so crap in crap out
					 //the dependency counts are not 100% accurate
					 DependencyMutant dm = new DependencyMutant();
					 dm.addReference();
					 dm.setClazzName(tn);
					 cdm.addDependency(dm);
					 if (log.isLogEnabled(ClassDependenciesDiscovery.class)) {
							log.log("calcDependencies circular ? " + name + " 2 " + tn);
					  }
				 } else {
					 classDeps = calcDependencies(tn, working);
				 }
			}
			if (classDeps != null) {
				if (log.isLogEnabled(ClassDependenciesDiscovery.class)) {
					log.log("calcDependencies adding " + name + " 2 " + tn);
				 }
				 
				cdm.add(classDeps);
			}
		}
		DependencyMutant dm = new DependencyMutant();
		dm.setClazzName(name);
		cdm.addDependency(dm);
		
		working.remove(name);
		ClassDependencies toRet = new ClassDependencies(cdm);
		dependencyCache.putIfAbsent(toRet);
		return toRet;
	}
	


	protected void addReflectionNames(Set<String> classNames, Class<?> clazz, ClassReferencesMutant classReferences) {
		if (clazz != null) {
			//don't add arrays
			if (clazz.isArray()) {
				Class<?> type = clazz.getComponentType();
				addApprovedName(classNames, type, classReferences);
			} else {
				addApprovedName(classNames, clazz, classReferences);
			}
		}
	}
	
	protected void addApprovedName(Set<String> classNames, Class<?> clazz, ClassReferencesMutant classReferences) {
		if (clazz != null) {
			
			if (isApprovedReferencedClass(clazz, classReferences)) {
				classNames.add(clazz.getName());
			}
		}
	}

	protected boolean isApprovedReferencedClass(Class<?> clazz, ClassReferencesMutant classReferences) {
		if (clazz != null) {
			//always block primitives
			if (!clazz.isPrimitive()) {
				if (!dependencyCache.isFiltered(clazz)) {
					String cn = clazz.getName();
					if ( !"void".equals(cn)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public Set<String> getIgnoredPackages() {
		return ignoredPackages;
	}

	public void setIgnoredPackages(Set<String> pIgnoredPackages) {
		if (pIgnoredPackages != null) {
			ignoredPackages.clear();
			ignoredPackages.addAll(pIgnoredPackages);
		}
	}
}
