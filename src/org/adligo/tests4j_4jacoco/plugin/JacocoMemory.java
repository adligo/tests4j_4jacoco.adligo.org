package org.adligo.tests4j_4jacoco.plugin;

import org.adligo.tests4j_4jacoco.plugin.instrumenation.MemoryClassLoader;
import org.adligo.tests4j_4jacoco.plugin.instrumenation.PackageSet;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_JacocoRuntime;
import org.adligo.tests4j_4jacoco.plugin.runtime.JacocoLoggerRuntime;
import org.adligo.tests4j_4jacoco.plugin.runtime.JacocoRuntimeData;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;

public class JacocoMemory {
	private MemoryClassLoader memoryClassLoader = new MemoryClassLoader();
	private I_JacocoRuntime runtime = new JacocoLoggerRuntime();
	private Instrumenter instrumenter = new Instrumenter(runtime);
	
	private PackageSet packages;
	
	public MemoryClassLoader getMemoryClassLoader() {
		return memoryClassLoader;
	}
	public I_JacocoRuntime getRuntime() {
		return runtime;
	}
	public Instrumenter getInstrumenter() {
		return instrumenter;
	}
	public PackageSet getPackages() {
		return packages;
	}
	public void setPackages(PackageSet packages) {
		this.packages = packages;
	}
}
