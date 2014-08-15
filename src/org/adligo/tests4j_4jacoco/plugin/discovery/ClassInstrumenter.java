package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;
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
	//private ClassDependenciesDiscovery classDependenciesDiscovery;
	private ClassReferencesDiscovery classReferencesDiscovery;
	private I_ClassBytesInstrumenter classBytesInstrumenter;
	
	public void setup() {
		//classDependenciesDiscovery = new ClassDependenciesDiscovery(cleanClassLoader, log, memory);
		classReferencesDiscovery = new ClassReferencesDiscovery(cleanClassLoader, log, memory);
	}
	
	public Class<?> instrumentClass(Class<?> c) throws ClassNotFoundException, IOException {
		String className = c.getName();
		if (log.isLogEnabled(ClassInstrumenter.class)) {
			log.log("ClassInstrumenter instrumenting class " + className);
		}
		List<String> refs = classReferencesDiscovery.findOrLoad(c);
		for (String dep: refs) {
			if ( !memory.isFiltered(dep)) {
				if ( !instrumentedClassLoader.hasCache(dep)) {
					if (log.isLogEnabled(ClassInstrumenter.class)) {
						log.log("ClassInstrumenter " + className + " instrumenting delegate " + dep);
					}
					InputStream bais = cleanClassLoader.getCachedBytesStream(dep);
					
					byte [] bytes = classBytesInstrumenter.instrumentClass(bais, dep);
					//instrumentedClassLoader should close the input stream
					instrumentedClassLoader.addCache(new ByteArrayInputStream(bytes), dep);
				}
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
