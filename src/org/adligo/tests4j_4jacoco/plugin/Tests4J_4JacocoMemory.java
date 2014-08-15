package org.adligo.tests4j_4jacoco.plugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.adligo.tests4j.models.shared.dependency.ClassFilter;
import org.adligo.tests4j.models.shared.dependency.ClassFilterMutant;
import org.adligo.tests4j.models.shared.dependency.I_ClassFilter;
import org.adligo.tests4j.models.shared.dependency.I_ClassParentsLocal;
import org.adligo.tests4j.models.shared.dependency.I_ClassReferences;
import org.adligo.tests4j.models.shared.dependency.I_ClassReferencesCache;
import org.adligo.tests4j.models.shared.dependency.I_ClassReferencesLocal;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;
import org.adligo.tests4j.run.helpers.CachedClassBytesClassLoader;
import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;
import org.adligo.tests4j_4jacoco.plugin.discovery.ClassReferencesCache;
import org.adligo.tests4j_4jacoco.plugin.discovery.I_DiscoveryMemory;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_InstrumenterFactory;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_Runtime;

public class Tests4J_4JacocoMemory implements I_DiscoveryMemory {
	/**
	 * this holds regular versions of the class which
	 * have been instrumented 
	 */
	private I_CachedClassBytesClassLoader instrumentedClassLoader;
	/**
	 * this holds regular versions of the class which
	 * have NOT been instrumented since
	 * getResourceAsStream seems to cause ThreadSafty issues
	 */
	private I_CachedClassBytesClassLoader cachedClassLoader;
	private ConcurrentHashMap<String, I_ClassReferencesLocal> refCache = new ConcurrentHashMap<String, I_ClassReferencesLocal>();
	private ConcurrentHashMap<String, I_ClassParentsLocal> parentsCache = new ConcurrentHashMap<String, I_ClassParentsLocal>();
	private ClassReferencesCache initalReferencesCache = new ClassReferencesCache();
	
	private I_ClassFilter classFilter;
	private I_ClassFilter basicClassFilter;
	
	private I_Runtime runtime;
	private I_InstrumenterFactory instrumenterFactory;
	private I_Tests4J_Log log;
	
	protected Tests4J_4JacocoMemory(I_Runtime pRuntime, I_InstrumenterFactory pInstrumenterFactory, I_Tests4J_Log pLog) {
		runtime = pRuntime;
		instrumenterFactory = pInstrumenterFactory;
		log = pLog;
		
		Set<String> packagesNotRequired = new HashSet<String>();
		packagesNotRequired.add("java.");
		packagesNotRequired.add("sun.");
		packagesNotRequired.add("org.jacoco.");
		packagesNotRequired.add("org.objectweb.");
		Set<String> classesNotRequired = SharedClassList.WHITELIST;
		instrumentedClassLoader = new CachedClassBytesClassLoader(log, 
				packagesNotRequired, classesNotRequired);
		cachedClassLoader = new CachedClassBytesClassLoader(log, 
				packagesNotRequired, classesNotRequired);
		
		ClassFilterMutant cfm = new ClassFilterMutant();
		cfm.setIgnoredClassNames(SharedClassList.WHITELIST);
		Set<String> pkgNames = cfm.getIgnoredPackageNames();
		Set<String> pkgNamesToSet = new HashSet<String>(pkgNames);
		pkgNamesToSet.add("java.");
		pkgNamesToSet.add("sun.");
		pkgNamesToSet.add("org.jacoco.");
		pkgNamesToSet.add("org.objectweb.");
		cfm.setIgnoredPackageNames(pkgNamesToSet);
		classFilter = new ClassFilter(cfm);
		
		ClassFilterMutant primitiveCFM = new ClassFilterMutant();
		primitiveCFM.setIgnoredClassNames(new HashSet<String>());
		basicClassFilter = new ClassFilter(primitiveCFM);
	}
	
	
	public I_CachedClassBytesClassLoader getInstrumentedClassLoader() {
		return instrumentedClassLoader;
	}
	public I_Runtime getRuntime() {
		return runtime;
	}
	public I_InstrumenterFactory getInstrumenterFactory() {
		return instrumenterFactory;
	}

	public I_CachedClassBytesClassLoader getCachedClassLoader() {
		return cachedClassLoader;
	}

	@Override
	public boolean isFiltered(Class<?> clazz) {
		return classFilter.isFiltered(clazz);
	}


	@Override
	public boolean isFiltered(String className) {
		return classFilter.isFiltered(className);
	}


	public I_ClassFilter getBasicClassFilter() {
		return basicClassFilter;
	}


	@Override
	public void putReferencesIfAbsent(I_ClassReferencesLocal p) {
		refCache.putIfAbsent(p.getName(), p);
	}


	@Override
	public I_ClassReferencesLocal getReferences(String name) {
		return refCache.get(name);
	}


	@Override
	public void putParentsIfAbsent(I_ClassParentsLocal p) {
		parentsCache.putIfAbsent(p.getName(), p);
	}


	@Override
	public I_ClassParentsLocal getParents(String name) {
		return parentsCache.get(name);
	}


	@Override
	public I_ClassReferencesCache getInitialReferencesCache() {
		return initalReferencesCache;
	}
}
