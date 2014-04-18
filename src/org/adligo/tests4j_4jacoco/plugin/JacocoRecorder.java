package org.adligo.tests4j_4jacoco.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.adligo.tests4j.models.shared.coverage.I_PackageCoverage;
import org.adligo.tests4j.models.shared.system.I_CoverageRecorder;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Logger;
import org.adligo.tests4j_4jacoco.plugin.instrumenation.ClassNameToInputStream;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_JacocoRuntime;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_JacocoRuntimeData;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.runtime.IRuntime;

public class JacocoRecorder implements I_CoverageRecorder {
	protected I_Tests4J_Logger log;
	protected JacocoMemory memory;
	private boolean root;
	private String scope;
	
	
	public JacocoRecorder(String pScope, JacocoMemory pMemory, I_Tests4J_Logger pLog) {
		scope = pScope;
		memory = pMemory;
		log = pLog;
	}
	
	@Override
	public String getScope() {
		return scope;
	}
	

	@Override
	public void startRecording() {
		try {
			I_JacocoRuntime runtime = memory.getRuntime();
			if (isRoot()) {
				runtime.startup();
			} else {
				runtime.startRecording(getScope());
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
	
	@Override
	public List<I_PackageCoverage> getCoverage() {
		I_JacocoRuntime runtime = memory.getRuntime();
		if (isRoot()) {
			runtime.shutdown();
		}
		I_JacocoRuntimeData data = runtime.stopRecording(getScope());
		
		final ExecutionDataStore executionData = new ExecutionDataStore();
		final SessionInfoStore sessionInfos = new SessionInfoStore();
		data.collect(executionData, sessionInfos, false);
		
		try {
			
			final CoverageBuilder coverageBuilder = new CoverageBuilder();
			final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
			String targetName = "org.adligo.tests4j.models.shared.AbstractTrial";
			analyzer.analyzeClass(ClassNameToInputStream.getTargetClass(targetName), targetName);
	
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
					if (line.getInstructionCounter().getCoveredCount() >= 1 
							|| line.getBranchCounter().getCoveredCount() >= 1) {
						
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
			}
						
		} catch (Exception x) {
			x.printStackTrace();
		}
		List<I_PackageCoverage> toRet = new ArrayList<I_PackageCoverage>();
		return toRet;
	}


	private void printCounter(final String unit, final ICounter counter) {
		final Integer missed = Integer.valueOf(counter.getMissedCount());
		final Integer total = Integer.valueOf(counter.getTotalCount());
		log.log("" + missed + " of " + total + " missed " + unit);
	}

	public boolean isRoot() {
		return root;
	}

	public void setRoot(boolean root) {
		this.root = root;
	}

	@Override
	public void stopRecording() {
		I_JacocoRuntime runtime = memory.getRuntime();
		
		runtime.stopRecording(getScope());
	}
	
	
}
