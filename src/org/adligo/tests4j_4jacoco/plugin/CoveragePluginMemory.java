package org.adligo.tests4j_4jacoco.plugin;

import org.adligo.tests4j.models.shared.association.I_ClassAssociationsCache;
import org.adligo.tests4j.models.shared.association.I_ClassParentsCache;
import org.adligo.tests4j.run.discovery.I_PackageDiscovery;
import org.adligo.tests4j.run.discovery.PackageDiscovery;
import org.adligo.tests4j.run.helpers.CachedClassBytesClassLoader;
import org.adligo.tests4j.run.helpers.ClassFilter;
import org.adligo.tests4j.run.helpers.ClassFilterMutant;
import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;
import org.adligo.tests4j.run.helpers.I_ClassFilter;
import org.adligo.tests4j.shared.common.CacheControl;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;
import org.adligo.tests4j_4jacoco.plugin.common.I_ClassInstrumenterFactory;
import org.adligo.tests4j_4jacoco.plugin.common.I_CoveragePluginMemory;
import org.adligo.tests4j_4jacoco.plugin.common.I_OrderedClassDiscoveryFactory;
import org.adligo.tests4j_4jacoco.plugin.common.I_ProbeDataAccessorFactory;
import org.adligo.tests4j_4jacoco.plugin.common.I_Runtime;
import org.adligo.tests4j_4jacoco.plugin.common.I_TrialInstrumenterFactory;
import org.adligo.tests4j_4jacoco.plugin.discovery.ClassDependenciesCache;
import org.adligo.tests4j_4jacoco.plugin.discovery.ClassParentsCache;
import org.adligo.tests4j_4jacoco.plugin.discovery.OrderedClassDiscoveryFactory;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.TrialInstrumenterFactory;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.ClassInstrumentationMetadataStoreMutant;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ClassInstrumentationMetadataStoreMutant;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.map.MapClassInstrumenterFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class CoveragePluginMemory implements I_CoveragePluginMemory {
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
	private CacheControl cacheControl = new CacheControl();
	private I_ClassParentsCache parentsCache = new ClassParentsCache(cacheControl);
	
	private ClassDependenciesCache dependencyCache = new ClassDependenciesCache();
	private ClassDependenciesCache initalDependencyCache = new ClassDependenciesCache(cacheControl);
	private ClassDependenciesCache fullDependencyCache = new ClassDependenciesCache(cacheControl);
	private I_TrialInstrumenterFactory trialInstrumenterFactory = new TrialInstrumenterFactory();
	private I_ClassInstrumenterFactory classInstrumenterFactory = new MapClassInstrumenterFactory();
	private I_OrderedClassDiscoveryFactory orderedClassDiscoveryFactory = new OrderedClassDiscoveryFactory();
	private I_ClassInstrumentationMetadataStoreMutant classInstrumentationInfoStore_ = new ClassInstrumentationMetadataStoreMutant();
	
	private I_ClassFilter classFilter;
	private I_ClassFilter basicClassFilter;
	
	private I_Runtime runtime;
	private I_ProbeDataAccessorFactory probeDataAccessorFactory;
	private I_ClassInstrumenterFactory instrumenterFactory;
	private I_Tests4J_Log log;
	private boolean canThreadGroupLocalRecord = true;
	private String instrumentedClassFileOutputFolder = "instrumentedClasses";
	private boolean writeOutInstrumentedClassFiles = false;
	private boolean concurrentRecording = true;
	private Set<String> whitelist_;
	private ConcurrentSkipListSet<String> allSourceFiles_ = new ConcurrentSkipListSet<String>();
	private ConcurrentSkipListSet<String> nonResultPackageParts_ = new ConcurrentSkipListSet<String>();
	
	@SuppressWarnings("unchecked")
  protected CoveragePluginMemory(Map<String,Object> input) {
		log = (I_Tests4J_Log) input.get(CoveragePluginMapParams.LOGGER);
		
		Set<String> packagesNotRequired = (Set<String>) input.get(
		    CoveragePluginMapParams.NON_INSTRUMENTED_PACKAGES);
		
		whitelist_ = (Set<String>) input.get(CoveragePluginMapParams.WHITELIST);
		instrumentedClassLoader = new CachedClassBytesClassLoader(log, 
				packagesNotRequired, whitelist_, null);
		
		Set<String> nonResultPackages = (Set<String>) input.get(CoveragePluginMapParams.NON_RESULT_PACKAGES);
		if (nonResultPackages != null) {
		  nonResultPackageParts_.addAll(nonResultPackages);
		}
		/**
		 * note the original classes are required for 
		 * re-instrumentation for coverage in the LazyCoverage classes
		 */
		cachedClassLoader = new CachedClassBytesClassLoader(log, 
				packagesNotRequired, whitelist_, null);
		
		ClassFilterMutant cfm = new ClassFilterMutant();
		cfm.setIgnoredClassNames(whitelist_);
		Set<String> pkgNames = cfm.getIgnoredPackageNames();
		pkgNames.addAll(packagesNotRequired);
    
		cfm.setIgnoredPackageNames(packagesNotRequired);
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
	public I_ClassInstrumenterFactory getInstrumenterFactory() {
		return instrumenterFactory;
	}

	public I_CachedClassBytesClassLoader getCachedClassLoader() {
		return cachedClassLoader;
	}

	public boolean isFiltered(Class<?> clazz) {
		return classFilter.isFiltered(clazz);
	}

	public boolean isFiltered(String className) {
		return classFilter.isFiltered(className);
	}


	public I_ClassFilter getBasicClassFilter() {
		return basicClassFilter;
	}

	public I_ClassAssociationsCache getIDependencyCache() {
		return dependencyCache;
	}
	public I_ClassAssociationsCache getInitialDependencyCache() {
		return initalDependencyCache;
	}

	public I_ClassAssociationsCache getFullDependencyCache() {
		return fullDependencyCache;
	}
	
	public void clearTemporaryCaches() {
		cacheControl.clear();
	}


	public I_TrialInstrumenterFactory getTrialInstrumenterFactory() {
		return trialInstrumenterFactory;
	}


	public I_ClassInstrumenterFactory getClassInstrumenterFactory() {
		return classInstrumenterFactory;
	}


	public I_OrderedClassDiscoveryFactory getOrderedClassDiscoveryFactory() {
		return orderedClassDiscoveryFactory;
	}


	public void setTrialInstrumenterFactory(
			I_TrialInstrumenterFactory trialInstrumenterFactory) {
		this.trialInstrumenterFactory = trialInstrumenterFactory;
	}


	public void setClassInstrumenterFactory(
			I_ClassInstrumenterFactory classInstrumenterFactory) {
		this.classInstrumenterFactory = classInstrumenterFactory;
	}


	public void setOrderedClassDiscoveryFactory(
			I_OrderedClassDiscoveryFactory orderedClassDiscoveryFactory) {
		this.orderedClassDiscoveryFactory = orderedClassDiscoveryFactory;
	}


	@Override
	public I_Tests4J_Log getLog() {
		return log;
	}

	@Override
	public I_ClassFilter getClassFilter() {
		return classFilter;
	}

	@Override
	public I_ClassAssociationsCache getDependencyCache() {
		return dependencyCache;
	}

	public I_ProbeDataAccessorFactory getProbeDataAccessorFactory() {
		return probeDataAccessorFactory;
	}

	public void setProbeDataAccessorFactory(
			I_ProbeDataAccessorFactory probeDataAccessorFactory) {
		this.probeDataAccessorFactory = probeDataAccessorFactory;
	}

	public void setRuntime(I_Runtime runtime) {
		this.runtime = runtime;
	}

	public void setInstrumenterFactory(
			I_ClassInstrumenterFactory instrumenterFactory) {
		this.instrumenterFactory = instrumenterFactory;
	}
	
	@Override
	public boolean isCanThreadGroupLocalRecord() {
		return canThreadGroupLocalRecord;
	}

	public void setCanThreadGroupLocalRecord(boolean canThreadGroupLocalRecord) {
		this.canThreadGroupLocalRecord = canThreadGroupLocalRecord;
	}

	public String getInstrumentedClassFileOutputFolder() {
		return instrumentedClassFileOutputFolder;
	}

	public void setInstrumentedClassFileOutputFolder(
			String instrumentedClassFileOutputFolder) {
		this.instrumentedClassFileOutputFolder = instrumentedClassFileOutputFolder;
		File file = new File(instrumentedClassFileOutputFolder);
		if (file.exists()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				File classFile = files[i];
				if (classFile.getName().endsWith(".class")) {
					file.delete();
				}
			}
		}
	}

	public boolean isWriteOutInstrumentedClassFiles() {
		return writeOutInstrumentedClassFiles;
	}

	public void setWriteOutInstrumentedClassFiles(
			boolean writeOutInstrumentedClassFiles) {
		this.writeOutInstrumentedClassFiles = writeOutInstrumentedClassFiles;
	}

	public I_ClassParentsCache getParentsCache() {
		return parentsCache;
	}

	public boolean isConcurrentRecording() {
		return concurrentRecording;
	}

	public void setConcurrentRecording(boolean concurrentRecording) {
		this.concurrentRecording = concurrentRecording;
	}

  public Set<String> getWhitelist() {
    return whitelist_;
  }

  public I_ClassInstrumentationMetadataStoreMutant getClassInstrumentationInfoStore() {
    return classInstrumentationInfoStore_;
  }

  @Override
  public I_PackageDiscovery getPackage(String packageName) {
    try {
      return new PackageDiscovery(packageName);
    } catch (IOException x) {
      throw new RuntimeException(x);
    }
  }
  
  public boolean isResultPackage(String packageName) {
    for(String pkgPart: nonResultPackageParts_) {
      if (packageName.indexOf(pkgPart) == 0) {
        return false;
      }
    }
    return true;
  }
  
  public Set<String> getAllSourceFileTrials() {
    return allSourceFiles_;
  }
  
  public void addSourceFileTrial(String className) {
    allSourceFiles_.add(className);
  }
}
