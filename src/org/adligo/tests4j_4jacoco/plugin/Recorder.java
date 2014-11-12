package org.adligo.tests4j_4jacoco.plugin;

import org.adligo.tests4j.models.shared.coverage.I_ClassProbes;
import org.adligo.tests4j.models.shared.coverage.I_CoverageUnits;
import org.adligo.tests4j.models.shared.coverage.I_PackageCoverageBrief;
import org.adligo.tests4j.models.shared.coverage.I_SourceFileCoverage;
import org.adligo.tests4j.models.shared.coverage.I_SourceFileCoverageBrief;
import org.adligo.tests4j.models.shared.coverage.SourceFileCoverageBrief;
import org.adligo.tests4j.models.shared.coverage.SourceFileCoverageBriefMutant;
import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;
import org.adligo.tests4j.system.shared.api.I_Tests4J_CoverageRecorder;
import org.adligo.tests4j_4jacoco.plugin.common.I_CoveragePluginMemory;
import org.adligo.tests4j_4jacoco.plugin.common.I_Runtime;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.ProbesDataStoreMutant;
import org.adligo.tests4j_4jacoco.plugin.data.coverage.LazyPackageCoverageFactory;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Recorder implements I_Tests4J_CoverageRecorder {
	protected I_Tests4J_Log log_;
	protected I_CoveragePluginMemory memory_;
	private boolean main_ = false;
	private I_Runtime runtime_;
	private boolean jacocoInitOnFirstRecording_ = true;
	private String threadGroupName_;
	private String filter_;
	
	public Recorder(I_CoveragePluginMemory pMemory, I_Tests4J_Log pLog) {
		memory_ = pMemory;
		 
	    
		log_ = pLog;
		runtime_ = memory_.getRuntime();
		main_ = true;
	}
	
	public Recorder(I_CoveragePluginMemory pMemory, I_Tests4J_Log pLog, 
	    String threadGroupName, String filter) {
    memory_ = pMemory;
    memory_.addFilter(filter);
    log_ = pLog;
    runtime_ = memory_.getRuntime();
    threadGroupName_ = threadGroupName;
    filter_ = filter;
    runtime_.putThreadGroupFilter(threadGroupName, filter);
    runtime_.clearClassesCovered(threadGroupName);
  }

	@Override
	public void startRecording() {
		if (log_ != null) {
			if (log_.isLogEnabled(Recorder.class)) {
				log_.log("Recorder starting ");
			}
		}
		try {
			runtime_.startup();
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
		
		if (jacocoInitOnFirstRecording_) {
			if (main_) {
				I_CachedClassBytesClassLoader mcl = memory_.getInstrumentedClassLoader();
				List<String> allClasses = mcl.getAllCachedClasses();
				int progress = 0;
				double nextProgressLog = 10.0;
				for (String clazz: allClasses) {
					progress++;
					double dp = 0.0 + progress;
					double tot = 0.0 + allClasses.size();
					
					if (dp/tot >= nextProgressLog) {
						nextProgressLog = nextProgressLog + 10.0;
						DecimalFormat df = new DecimalFormat("###.#%");
						log_.log("tests4j_4jacoco " + df.format(dp/tot) + "% calling $jacocoInit()s");
					}
					if (clazz.indexOf("$") == -1) {
						Class<?> loadedClass = mcl.getCachedClass(clazz);
						if (loadedClass != null) {
							try {
								Method jacocoInit = loadedClass.getMethod("$jacocoInit", new Class[] {});
								
								if (jacocoInit != null) {
									jacocoInit.invoke(loadedClass, new Object[] {});
								}
							} catch (NoSuchMethodException x) {
								//interfaces don't have it
							} catch (Throwable e) {
								throw new RuntimeException("Class " + clazz + " failed to call $jacocoInit.", e);
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public List<I_PackageCoverageBrief> endRecording(Set<String> classNames) {
		if (log_ != null) {
			if (log_.isLogEnabled(Recorder.class)) {
				log_.log("Ending Recording " + Thread.currentThread().getName());
			}
		}
		I_ProbesDataStore executionData = runtime_.end(main_);
		
		return LazyPackageCoverageFactory.create(executionData, memory_, classNames);
	}

	public static String getPackageDir(final String name) {
		final String resource = '/' + name.replace('.', '/');
		int lastDot = resource.lastIndexOf("/");
		return resource.substring(0, lastDot);
	}
	
	public boolean isMain() {
		return main_;
	}

  @Override
  public I_SourceFileCoverageBrief getSourceFileCoverage() {
    I_SourceFileCoverageBrief probes = runtime_.getSourceFileCoverage(threadGroupName_, filter_);
    ProbesDataStoreMutant mut = new ProbesDataStoreMutant();
    Set<String> classNames = new HashSet<String>();
    mut.put(probes.getClassId(), probes);
    List<I_ClassProbes> cps = probes.getClassProbes();
    for (I_ClassProbes cp: cps) {
      mut.put(cp.getClassId(), cp);
      classNames.add(cp.getClassName());
    }
    I_SourceFileCoverage sfc;
    try {
      sfc = LazyPackageCoverageFactory.createSourceFileCoverage(
          mut, memory_, filter_, classNames);
      I_CoverageUnits cus = sfc.getCoverageUnits();
      
      SourceFileCoverageBriefMutant sfpm = new SourceFileCoverageBriefMutant(probes);
      sfpm.setCoverageUnits(cus.get());
      I_CoverageUnits ccus = sfc.getCoveredCoverageUnits();
      sfpm.setCoveredCoverageUnits(ccus.get());
      return new SourceFileCoverageBrief(sfpm);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public I_PackageCoverageBrief getPackageCoverage() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<I_PackageCoverageBrief> getAllCoverage() {
    Set<String> filters = memory_.getAllFilters();
    
    return null;
  }

}