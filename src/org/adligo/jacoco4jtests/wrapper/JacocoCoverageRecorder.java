package org.adligo.jacoco4jtests.wrapper;

import java.util.ArrayList;
import java.util.List;

import org.adligo.jtests.models.shared.coverage.I_CoverageRecorder;
import org.adligo.jtests.models.shared.coverage.I_PackageCoverage;
import org.adligo.jtests.models.shared.coverage.PackageCoverageMutant;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;

public class JacocoCoverageRecorder implements I_CoverageRecorder {
	// For instrumentation and runtime we need a IRuntime instance
	// to collect execution data:
	final IRuntime runtime = new LoggerRuntime();

	// The Instrumenter creates a modified version of our test target class
	// that contains additional probes for execution data recording:
	final Instrumenter instr = new Instrumenter(runtime);
	final RuntimeData data = new RuntimeData();
	
	public JacocoCoverageRecorder() {
		try {
			runtime.startup(data);
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
	@Override
	public void startRecording()  {
		
	}

	@Override
	public I_PackageCoverage getCoverage(String packageName) {
		return null;
		
	}
	
	public IRuntime getRuntime() {
		return runtime;
	}
	public Instrumenter getInstrumenter() {
		return instr;
	}
	@Override
	public List<I_PackageCoverage> getCoverage() {
		final ExecutionDataStore executionData = new ExecutionDataStore();
		final SessionInfoStore sessionInfos = new SessionInfoStore();
		data.collect(executionData, sessionInfos, false);
		runtime.shutdown();

		List<I_PackageCoverage> toRet = new ArrayList<I_PackageCoverage>();
		return toRet;
	}

}
