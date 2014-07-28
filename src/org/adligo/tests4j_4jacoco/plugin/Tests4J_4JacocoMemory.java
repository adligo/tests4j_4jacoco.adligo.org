package org.adligo.tests4j_4jacoco.plugin;

import java.util.Collections;
import java.util.Set;

import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;
import org.adligo.tests4j.run.helpers.CachedClassBytesClassLoader;
import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_InstrumenterFactory;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_Runtime;

public class Tests4J_4JacocoMemory {
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
	private I_Runtime runtime;
	private I_InstrumenterFactory instrumenterFactory;
	private I_Tests4J_Log log;
	
	protected Tests4J_4JacocoMemory(I_Runtime pRuntime, I_InstrumenterFactory pInstrumenterFactory, I_Tests4J_Log pLog) {
		runtime = pRuntime;
		instrumenterFactory = pInstrumenterFactory;
		log = pLog;
		
		Set<String> packagesWithoutWarning = Collections.singleton("java.");
		Set<String> classesWithoutWarning = SharedClassList.WHITELIST;
		instrumentedClassLoader = new CachedClassBytesClassLoader(log, 
				packagesWithoutWarning, classesWithoutWarning);
		cachedClassLoader = new CachedClassBytesClassLoader(log, 
				packagesWithoutWarning, classesWithoutWarning);
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
}
