package org.adligo.tests4j_4jacoco.plugin;

import org.adligo.tests4j.models.shared.coverage.I_ClassProbes;
import org.adligo.tests4j.models.shared.coverage.I_CoverageUnits;
import org.adligo.tests4j.models.shared.coverage.I_SourceFileCoverageBrief;
import org.adligo.tests4j.models.shared.coverage.I_SourceFileCoverage;
import org.adligo.tests4j.models.shared.coverage.SourceFileCoverageBrief;
import org.adligo.tests4j.models.shared.coverage.SourceFileCoverageBriefMutant;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;
import org.adligo.tests4j.system.shared.api.I_Tests4J_CoveragePlugin;
import org.adligo.tests4j.system.shared.api.I_Tests4J_CoverageRecorder;
import org.adligo.tests4j.system.shared.api.I_Tests4J_CoverageTrialInstrumentation;
import org.adligo.tests4j.system.shared.trials.I_AbstractTrial;
import org.adligo.tests4j_4jacoco.plugin.common.I_TrialInstrumenter;
import org.adligo.tests4j_4jacoco.plugin.common.I_TrialInstrumenterFactory;
import org.adligo.tests4j_4jacoco.plugin.data.common.ProbesDataStoreMutant;
import org.adligo.tests4j_4jacoco.plugin.data.coverage.LazyPackageCoverageFactory;
import org.adligo.tests4j_4jacoco.plugin.data.multi.MultiContext;
import org.adligo.tests4j_4jacoco.plugin.data.multi.MultiProbeDataStoreAdaptor;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.ProbeDataAccessorByLoggingApiFactory;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.map.MapClassInstrumenterFactory;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.map.MapInstrConstants;
import org.adligo.tests4j_4jacoco.plugin.runtime.simple.SimpleLoggerRuntime;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class CoveragePlugin implements I_Tests4J_CoveragePlugin {
	private CoveragePluginMemory memory_;
	private I_Tests4J_Log log_;
	private ThreadLocal<I_TrialInstrumenter> trialIntrumenters_ = new ThreadLocal<I_TrialInstrumenter>();
	private ConcurrentHashMap<String, I_TrialInstrumenter> trialIntrumenterByWork_ = 
			new ConcurrentHashMap<String, I_TrialInstrumenter>();
	private AtomicBoolean instrumentedTrials_ = new AtomicBoolean(false);
	
	public CoveragePlugin(Map<String,Object> input) {
		
		ProbeDataAccessorByLoggingApiFactory factory = new ProbeDataAccessorByLoggingApiFactory(
				MapInstrConstants.DATAFIELD_DESC);
		log_ = (I_Tests4J_Log) input.get(CoveragePluginMapParams.LOGGER);
		
		memory_ = new CoveragePluginMemory(input);
		memory_.setProbeDataAccessorFactory(factory);
		memory_.setInstrumenterFactory(new MapClassInstrumenterFactory());
		
		SimpleLoggerRuntime runtime = new SimpleLoggerRuntime(factory);
		runtime.setup(
		    new MultiProbeDataStoreAdaptor(
		    new MultiContext(log_, runtime)));
		memory_.setRuntime(runtime);
	}
	
	public I_Tests4J_CoverageTrialInstrumentation instrument(Class<? extends I_AbstractTrial> trial) 
			throws IOException  {
	  synchronized (instrumentedTrials_) {
	    instrumentedTrials_.set(true); 
    }
		if (trialIntrumenters_.get() == null) {
			I_TrialInstrumenterFactory factory =  memory_.getTrialInstrumenterFactory();
			I_TrialInstrumenter ti = factory.create(memory_);
			trialIntrumenters_.set(ti);
		}
		I_TrialInstrumenter ti = trialIntrumenters_.get();
		String trialName = trial.getName();
		trialIntrumenterByWork_.put(trialName, ti);
		I_Tests4J_CoverageTrialInstrumentation result =  ti.instrument(trial);
		trialIntrumenterByWork_.remove(trialName);
		return result;
	}
	
	
	@Override
	public synchronized I_Tests4J_CoverageRecorder createRecorder() {
		return new Recorder(memory_, log_);
	}

	public CoveragePluginMemory getMemory() {
		return memory_;
	}

	public I_Tests4J_Log getTests4jLogger() {
		return log_;
	}

	public void setTests4jLogger(I_Tests4J_Log log) {
		this.log_ = log;
	}

	@Override
	public void instrumentationComplete() {
		memory_.clearTemporaryCaches();
	}

	@Override
	public boolean isCanThreadGroupLocalRecord() {
		return memory_.isCanThreadGroupLocalRecord();
	}

	@Override
	public double getInstrumentProgress(Class<? extends I_AbstractTrial> trial) {
		String trialName = trial.getName();
		I_TrialInstrumenter progress = trialIntrumenterByWork_.get(trialName);
		if (progress == null) {
			return 100.00;
		}
		return progress.getPctDone();
	}

  @Override
  public I_Tests4J_CoverageRecorder createRecorder(String threadGroup, String javaFilter) {
    return new Recorder(memory_, log_, threadGroup, javaFilter);
  }

  @Override
  public I_SourceFileCoverage analyze(I_SourceFileCoverageBrief sourceFileBrief, boolean instrument) {
    if (instrument) {
      synchronized (instrumentedTrials_) {
        if (instrumentedTrials_.get()) {
          throw new IllegalStateException("Instrumentation has already occured for this plugin.");
        }
      }
    }
    
    
    ProbesDataStoreMutant mut = new ProbesDataStoreMutant();
    Set<String> classNames = new HashSet<String>();
    mut.put(sourceFileBrief.getClassId(), sourceFileBrief);
    List<I_ClassProbes> cps = sourceFileBrief.getClassProbes();
    for (I_ClassProbes cp: cps) {
      mut.put(cp.getClassId(), cp);
      classNames.add(cp.getClassName());
    }
    try {
      return LazyPackageCoverageFactory.createSourceFileCoverage(
          mut, memory_, sourceFileBrief.getClassName(), classNames);
    } catch (ClassNotFoundException x) {
      throw new RuntimeException(x);
    }
  }

  @Override
  public I_SourceFileCoverage analyze(I_SourceFileCoverageBrief sourceFileBrief) {
    return analyze(sourceFileBrief, false);
  }

}
