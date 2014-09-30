package org.adligo.tests4j_4jacoco.plugin;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.adligo.tests4j.models.shared.dependency.I_ClassDependenciesCache;
import org.adligo.tests4j.models.shared.dependency.I_ClassDependenciesLocal;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;
import org.adligo.tests4j.system.shared.api.I_Tests4J_CoveragePlugin;
import org.adligo.tests4j.system.shared.api.I_Tests4J_CoverageRecorder;
import org.adligo.tests4j.system.shared.api.I_Tests4J_CoverageTrialInstrumentation;
import org.adligo.tests4j.system.shared.trials.I_AbstractTrial;
import org.adligo.tests4j_4jacoco.plugin.common.I_TrialInstrumenter;
import org.adligo.tests4j_4jacoco.plugin.common.I_TrialInstrumenterFactory;
import org.adligo.tests4j_4jacoco.plugin.data.multi.MultiProbeDataStoreAdaptor;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.ProbeDataAccessorByLoggingApiFactory;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.map.MapClassInstrumenterFactory;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.map.MapInstrConstants;
import org.adligo.tests4j_4jacoco.plugin.runtime.simple.SimpleLoggerRuntime;

public class CoveragePlugin implements I_Tests4J_CoveragePlugin {
	private CoveragePluginMemory memory;
	private I_Tests4J_Log tests4jLogger;
	private AtomicBoolean firstRecorder = new AtomicBoolean(false);
	private ThreadLocal<I_TrialInstrumenter> trialIntrumenters = new ThreadLocal<I_TrialInstrumenter>();
	private ConcurrentHashMap<String, I_TrialInstrumenter> trialIntrumenterByWork = 
			new ConcurrentHashMap<String, I_TrialInstrumenter>();
	
	public CoveragePlugin(I_Tests4J_Log logger) {
		
		ProbeDataAccessorByLoggingApiFactory factory = new ProbeDataAccessorByLoggingApiFactory(
				MapInstrConstants.DATAFIELD_DESC);
		tests4jLogger = logger;
		
		memory = new CoveragePluginMemory(logger);
		memory.setProbeDataAccessorFactory(factory);
		memory.setInstrumenterFactory(new MapClassInstrumenterFactory());
		
		SimpleLoggerRuntime runtime = new SimpleLoggerRuntime(factory);
		runtime.setup(new MultiProbeDataStoreAdaptor(logger));
		memory.setRuntime(runtime);
	}
	
	public I_Tests4J_CoverageTrialInstrumentation instrument(Class<? extends I_AbstractTrial> trial) 
			throws IOException  {
		if (trialIntrumenters.get() == null) {
			I_TrialInstrumenterFactory factory =  memory.getTrialInstrumenterFactory();
			I_TrialInstrumenter ti = factory.create(memory);
			trialIntrumenters.set(ti);
		}
		I_TrialInstrumenter ti = trialIntrumenters.get();
		String trialName = trial.getName();
		trialIntrumenterByWork.put(trialName, ti);
		I_Tests4J_CoverageTrialInstrumentation result =  ti.instrument(trial);
		trialIntrumenterByWork.remove(trialName);
		return result;
	}
	
	
	@Override
	public synchronized I_Tests4J_CoverageRecorder createRecorder() {
		Recorder rec = new Recorder(memory, tests4jLogger);
		if (memory.isConcurrentRecording()) {
			if (!firstRecorder.get()) {
				firstRecorder.set(true);
				rec.setMain(true);
			}
		} else {
			rec.setMain(true);
		}
		return rec;
	}

	protected CoveragePluginMemory getMemory() {
		return memory;
	}

	public I_Tests4J_Log getTests4jLogger() {
		return tests4jLogger;
	}

	public void setTests4jLogger(I_Tests4J_Log log) {
		this.tests4jLogger = log;
	}

	@Override
	public void instrumentationComplete() {
		memory.clearTemporaryCaches();
	}

	@Override
	public boolean isCanThreadGroupLocalRecord() {
		return memory.isCanThreadGroupLocalRecord();
	}

	@Override
	public double getInstrumentProgress(Class<? extends I_AbstractTrial> trial) {
		String trialName = trial.getName();
		I_TrialInstrumenter progress = trialIntrumenterByWork.get(trialName);
		if (progress == null) {
			return 100.00;
		}
		return progress.getPctDone();
	}

}
