package org.adligo.tests4j_4jacoco.plugin;

import java.util.List;

import org.adligo.tests4j_4jacoco.plugin.data.coverage.I_ClassContainer;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.MemoryClassLoader;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_InstrumenterFactory;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_Runtime;

public class Tests4J_4JacocoMemory implements I_ClassContainer {
	/**
	 * this holds regular versions of the class which
	 * have been instrumented 
	 */
	private MemoryClassLoader instrumentedClassLoader = new MemoryClassLoader();
	/**
	 * this holds regular versions of the class which
	 * have NOT been instrumented since
	 * getResourceAsStream seems to cause ThreadSafty issues
	 */
	private MemoryClassLoader cachedClassLoader = new MemoryClassLoader();
	private I_Runtime runtime;
	private I_InstrumenterFactory instrumenterFactory;
	
	protected Tests4J_4JacocoMemory(I_Runtime pRuntime, I_InstrumenterFactory pInstrumenterFactory) {
		runtime = pRuntime;
		instrumenterFactory = pInstrumenterFactory;
	}
	
	
	public MemoryClassLoader getInstrumentedClassLoader() {
		return instrumentedClassLoader;
	}
	public I_Runtime getRuntime() {
		return runtime;
	}
	public I_InstrumenterFactory getInstrumenterFactory() {
		return instrumenterFactory;
	}
	public  List<String> getClassesInPackage(String pkgName) {
		return instrumentedClassLoader.getClassesInPackage(pkgName);
	}
	@Override
	public List<String> getAllClasses() {
		return instrumentedClassLoader.getAllClasses();
	}
	public MemoryClassLoader getCachedClassLoader() {
		return cachedClassLoader;
	}
}
