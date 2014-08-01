package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;
import org.adligo.tests4j.run.discovery.I_ClassDependencies;
import org.adligo.tests4j.run.discovery.I_ClassDependenciesCache;
import org.adligo.tests4j.run.discovery.I_Dependency;
import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_ClassBytesInstrumenter;

/**
 * This class instruments
 * a class and all of it's dependencies
 * and puts the instrumented class
 * in the instrumented class loader;
 * 
 * @author scott
 *
 */
public class ClassInstrumenter {
	/**
	 * contains the instrumented classes with injected byte code
	 */
	private I_CachedClassBytesClassLoader instrumentedClassLoader;
	/**
	 * contains the regular class bytes cache, so reloading isn't necessary
	 * between threads
	 */
	private I_CachedClassBytesClassLoader cleanClassLoader;
	private I_Tests4J_Log log;
	private I_DiscoveryMemory memory;
	private ClassDependenciesDiscovery classDependenciesDiscovery;
	private I_ClassBytesInstrumenter classBytesInstrumenter;
	private Set<String> ignoredClasses;
	private Set<String> ignoredPackages;
	
	public void setup() {
		classDependenciesDiscovery = new ClassDependenciesDiscovery(cleanClassLoader, log, memory);
	}
	
	public Class<?> instrumentClass(Class<?> c) throws ClassNotFoundException, IOException {
		I_ClassDependencies cd = classDependenciesDiscovery.discoverAndLoad(c);
		List<I_Dependency> deps =  cd.getDependencies();
		for (I_Dependency dep: deps) {
			String clazzName = dep.getClassName();
			if ( !instrumentedClassLoader.hasCache(clazzName)) {
				InputStream bais = cleanClassLoader.getCachedBytesStream(clazzName);
				//instrumentedClassLoader should close the input stream
				instrumentedClassLoader.addCache(bais, clazzName);
			}
		}
		return instrumentedClassLoader.getCachedClass(c.getName());
	}


	public I_CachedClassBytesClassLoader getInstrumentedClassLoader() {
		return instrumentedClassLoader;
	}


	public I_CachedClassBytesClassLoader getCleanClassLoader() {
		return cleanClassLoader;
	}


	public I_Tests4J_Log getLog() {
		return log;
	}


	public I_DiscoveryMemory getCache() {
		return memory;
	}


	public void setInstrumentedClassLoader(
			I_CachedClassBytesClassLoader instrumentedClassLoader) {
		this.instrumentedClassLoader = instrumentedClassLoader;
	}


	public void setCleanClassLoader(I_CachedClassBytesClassLoader cleanClassLoader) {
		this.cleanClassLoader = cleanClassLoader;
	}


	public void setLog(I_Tests4J_Log log) {
		this.log = log;
	}


	public void setMemory(I_DiscoveryMemory pMem) {
		memory = pMem;
	}

	public I_ClassBytesInstrumenter getClassBytesInstrumenter() {
		return classBytesInstrumenter;
	}

	public void setClassBytesInstrumenter(
			I_ClassBytesInstrumenter classBytesInstrumenter) {
		this.classBytesInstrumenter = classBytesInstrumenter;
	}
}
