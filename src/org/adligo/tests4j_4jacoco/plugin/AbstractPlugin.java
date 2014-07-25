package org.adligo.tests4j_4jacoco.plugin;

import java.util.concurrent.atomic.AtomicBoolean;

import org.adligo.tests4j.models.shared.system.I_Tests4J_CoveragePlugin;
import org.adligo.tests4j.models.shared.system.I_Tests4J_CoverageRecorder;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;
import org.adligo.tests4j.models.shared.trials.I_AbstractTrial;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_InstrumenterFactory;

public abstract class AbstractPlugin implements I_Tests4J_CoveragePlugin {
	protected Tests4J_4JacocoMemory memory;
	private I_Tests4J_Log tests4jLogger;
	private boolean writeOutInstrumentedClassFiles = false;
	private AtomicBoolean firstRecorder = new AtomicBoolean(false);
	private ThreadLocal<TrialInstrumenter> trialIntrumenters = new ThreadLocal<TrialInstrumenter>();
	
	@Override
	public Class<? extends I_AbstractTrial> instrument(Class<? extends I_AbstractTrial> trial) {
		if (trialIntrumenters.get() == null) {
			TrialInstrumenter ti = new TrialInstrumenter();
			ti.setWriteOutInstrumentedClassFiles(writeOutInstrumentedClassFiles);
			ti.setTests4jLogger(tests4jLogger);
			ti.setCachedClassLoader(memory.getCachedClassLoader());
			ti.setInstrumentedClassLoader(memory.getInstrumentedClassLoader());
			I_InstrumenterFactory instFact = memory.getInstrumenterFactory();
			ti.setInstrumenter(instFact.createInstrumenter());
			
			trialIntrumenters.set(ti);
		}
		TrialInstrumenter ti = trialIntrumenters.get();
		return ti.instrument(trial);
	}
	
	
	@Override
	public synchronized I_Tests4J_CoverageRecorder createRecorder() {
		Recorder rec = new Recorder(memory, tests4jLogger);
		if (!firstRecorder.get()) {
			firstRecorder.set(true);
			rec.setRoot(true);
		}
		return rec;
	}

	public Tests4J_4JacocoMemory getMemory() {
		return memory;
	}

	public I_Tests4J_Log getTests4jLogger() {
		return tests4jLogger;
	}

	public boolean isWriteOutInstrumentedClassFiles() {
		return writeOutInstrumentedClassFiles;
	}

	public void setMemory(Tests4J_4JacocoMemory memory) {
		this.memory = memory;
	}

	public void setTests4jLogger(I_Tests4J_Log log) {
		this.tests4jLogger = log;
	}

	public void setWriteOutInstrumentedClassFiles(
			boolean writeOutInstrumentedClassFiles) {
		this.writeOutInstrumentedClassFiles = writeOutInstrumentedClassFiles;
	}

}
