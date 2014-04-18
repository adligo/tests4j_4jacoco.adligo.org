package org.adligo.tests4j_4jacoco.plugin;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.adligo.tests4j.models.shared.coverage.I_PackageCoverage;
import org.adligo.tests4j.models.shared.system.I_CoverageRecorder;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Logger;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;

public class JacocoRecorder implements I_CoverageRecorder {
	// For instrumentation and runtime we need a IRuntime instance
	// to collect execution data:
	private final IRuntime runtime = new LoggerRuntime();

	// The Instrumenter creates a modified version of our test target class
	// that contains additional probes for execution data recording:
	private final Instrumenter instr = new Instrumenter(runtime);
	private final RuntimeData data = new RuntimeData();
	private String scope;
	private I_Tests4J_Logger log;
	
	public JacocoRecorder(String pScope, I_Tests4J_Logger pLog) {
		scope = pScope;
		log = pLog;
	}
	
	@Override
	public String getScope() {
		return scope;
	}
	
	@Override
	public void startRecording() {
		try {
			runtime.startup(data);
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
	
	public List<I_PackageCoverage> getCoverage() {
		
		
		final ExecutionDataStore executionData = new ExecutionDataStore();
		final SessionInfoStore sessionInfos = new SessionInfoStore();
		data.collect(executionData, sessionInfos, false);
		runtime.shutdown();
		try {
			
			final CoverageBuilder coverageBuilder = new CoverageBuilder();
			final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
			String targetName = "org.adligo.tests4j.models.shared.AbstractTrial";
			analyzer.analyzeClass(getTargetClass(targetName), targetName);
	
			Collection<ISourceFileCoverage> sourceCoverages = coverageBuilder.getSourceFiles();
			// Let's dump some metrics and line coverage information:
			for (final ISourceFileCoverage cc : sourceCoverages) {
				System.out.printf("Coverage of class %s%n", cc.getName());
	
				printCounter("instructions", cc.getInstructionCounter());
				printCounter("branches", cc.getBranchCounter());
				printCounter("lines", cc.getLineCounter());
				printCounter("methods", cc.getMethodCounter());
				printCounter("complexity", cc.getComplexityCounter());
	
				for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++) {
					ILine line = cc.getLine(i);
					log.log("Line " + i + " instructions " + 
							line.getInstructionCounter().getCoveredCount() +
							"/" +
							line.getInstructionCounter().getTotalCount() + 
							" instructions " + 
							line.getBranchCounter().getCoveredCount() +
							"/" +
							line.getBranchCounter().getTotalCount() + 
							" branches ");
				}
			}
						
		} catch (Exception x) {
			x.printStackTrace();
		}
		List<I_PackageCoverage> toRet = new ArrayList<I_PackageCoverage>();
		return toRet;
	}

	private InputStream getTargetClass(final String name) {
		final String resource = '/' + name.replace('.', '/') + ".class";
		return getClass().getResourceAsStream(resource);
	}

	private void printCounter(final String unit, final ICounter counter) {
		final Integer missed = Integer.valueOf(counter.getMissedCount());
		final Integer total = Integer.valueOf(counter.getTotalCount());
		log.log("" + missed + " of " + total + " missed " + unit);
	}
}
