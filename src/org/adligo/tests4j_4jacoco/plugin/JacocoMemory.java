package org.adligo.tests4j_4jacoco.plugin;

import org.adligo.tests4j_4jacoco.plugin.instrumentation.MemoryClassLoader;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.PackageSet;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_Instrumenter;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_JacocoRuntime;

public class JacocoMemory {
	private MemoryClassLoader memoryClassLoader = new MemoryClassLoader();
	private I_JacocoRuntime runtime;
	private I_Instrumenter instrumenter;
	
	public JacocoMemory(I_JacocoRuntime pRuntime, I_Instrumenter pInstrumenter) {
		runtime = pRuntime;
		instrumenter = pInstrumenter;
	}
	
	private PackageSet packages;
	
	public MemoryClassLoader getMemoryClassLoader() {
		return memoryClassLoader;
	}
	public I_JacocoRuntime getRuntime() {
		return runtime;
	}
	public I_Instrumenter getInstrumenter() {
		return instrumenter;
	}
	public PackageSet getPackages() {
		return packages;
	}
	public void setPackages(PackageSet packages) {
		this.packages = packages;
	}
}
