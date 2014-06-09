package org.adligo.tests4j_4jacoco.plugin;

import java.util.List;

import org.adligo.tests4j.run.discovery.TopPackageSet;
import org.adligo.tests4j_4jacoco.plugin.data.coverage.I_ClassContainer;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.MemoryClassLoader;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_Instrumenter;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_Runtime;

public class Tests4J_4JacocoMemory implements I_ClassContainer {
	private MemoryClassLoader memoryClassLoader = new MemoryClassLoader();
	private I_Runtime runtime;
	private I_Instrumenter instrumenter;
	
	protected Tests4J_4JacocoMemory(I_Runtime pRuntime, I_Instrumenter pInstrumenter) {
		runtime = pRuntime;
		instrumenter = pInstrumenter;
	}
	
	private TopPackageSet packages;
	
	public MemoryClassLoader getMemoryClassLoader() {
		return memoryClassLoader;
	}
	public I_Runtime getRuntime() {
		return runtime;
	}
	public I_Instrumenter getInstrumenter() {
		return instrumenter;
	}
	public TopPackageSet getPackages() {
		return packages;
	}
	public void setPackages(TopPackageSet packages) {
		this.packages = packages;
	}
	
	public  List<String> getClassesInPackage(String pkgName) {
		return memoryClassLoader.getClassesInPackage(pkgName);
	}
	@Override
	public List<String> getAllClasses() {
		return memoryClassLoader.getAllClasses();
	}
}
