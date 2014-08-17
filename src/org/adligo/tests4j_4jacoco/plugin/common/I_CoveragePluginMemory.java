package org.adligo.tests4j_4jacoco.plugin.common;

import org.adligo.tests4j.models.shared.dependency.I_ClassDependenciesCache;
import org.adligo.tests4j.models.shared.dependency.I_ClassFilter;
import org.adligo.tests4j.models.shared.dependency.I_ClassParentsCache;
import org.adligo.tests4j.models.shared.system.I_Tests4J_CoveragePlugin;
import org.adligo.tests4j.models.shared.system.I_Tests4J_CoveragePluginParams;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;
import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;

/**
 * the main memory interface for tests4j_4jacoco.
 * @author scott
 *
 */
public interface I_CoveragePluginMemory {
	/**
	 * @return a I_Tests4J_Log
	 */
	public I_Tests4J_Log getLog();
	
	public I_Runtime getRuntime();
	/**
	 * the finished dependencies cache
	 * @return
	 */
	public I_ClassDependenciesCache getDependencyCache(); 
	/**
	 * the initial dependencies cache
	 * @return
	 */
	public I_ClassDependenciesCache getInitialDependencyCache() ;
	/**
	 * the full dependencies cache, with out circles
	 * @return
	 */
	public I_ClassDependenciesCache getFullDependencyCache();
	/**
	 * @return a I_TrialInstrumenterFactory.
	 */
	public I_TrialInstrumenterFactory getTrialInstrumenterFactory();
	/**
	 * 
	 * @return a I_ClassInstrumenterFactory
	 */
	public I_ClassInstrumenterFactory getClassInstrumenterFactory();
	/**
	 * @return a I_OrderedClassDiscoveryFactory
	 */
	public I_OrderedClassDiscoveryFactory getOrderedClassDiscoveryFactory();
	
	/**
	 * a custom filter, may filter any class
	 * @return
	 */
	public I_ClassFilter getClassFilter();
	/**
	 * filters primitives and other 
	 * special classes like Void.
	 * @return
	 */
	public I_ClassFilter getBasicClassFilter();
	
	/**
	 * I_CachedClassBytesClassLoader which caches byte[] of the non modified class
	 * @return
	 */
	public I_CachedClassBytesClassLoader getCachedClassLoader();
	/**
	 * I_CachedClassBytesClassLoader which caches byte[] of the instrumented class
	 * @return
	 */
	public I_CachedClassBytesClassLoader getInstrumentedClassLoader();
	
	/**
	 * the probe data accessor factory
	 * @return
	 */
	public I_ProbeDataAccessorFactory getProbeDataAccessorFactory();
	
	/**
	 * @see I_Tests4J_CoveragePlugin#isCanThreadGroupLocalRecord()
	 * @return
	 */
	public boolean isCanThreadGroupLocalRecord();

	/**
	 * @see I_Tests4J_CoveragePluginParams#getInstrumentedClassOutputFolder()
	 * @return
	 */
	public String getInstrumentedClassFileOutputFolder();
	
	/**
	 * @see I_Tests4J_CoveragePluginParams#isWriteOutInstrumentedClasses()
	 * @return
	 */
	public boolean isWriteOutInstrumentedClassFiles();
	
	/**
	 * 
	 * @return
	 */
	public I_ClassParentsCache getParentsCache();
}
