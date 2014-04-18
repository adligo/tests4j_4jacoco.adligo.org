package org.adligo.tests4j_4jacoco.plugin;

import org.adligo.tests4j_4jacoco.plugin.instrumentation.JacocoInstrumenter;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.MemoryClassLoader;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.PackageSet;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_JacocoRuntime;
import org.adligo.tests4j_4jacoco.plugin.runtime.multicast.MulticastLoggerRuntime;
import org.jacoco.core.instr.Instrumenter;

public class JacocoMemory {
	private MemoryClassLoader memoryClassLoader = new MemoryClassLoader();
	private I_JacocoRuntime runtime = new MulticastLoggerRuntime();
	private JacocoInstrumenter instrumenter = new JacocoInstrumenter(runtime);
	
	private PackageSet packages;
	
	public MemoryClassLoader getMemoryClassLoader() {
		return memoryClassLoader;
	}
	public I_JacocoRuntime getRuntime() {
		return runtime;
	}
	public JacocoInstrumenter getInstrumenter() {
		return instrumenter;
	}
	public PackageSet getPackages() {
		return packages;
	}
	public void setPackages(PackageSet packages) {
		this.packages = packages;
	}
}
