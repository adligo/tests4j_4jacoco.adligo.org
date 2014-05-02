package org.adligo.tests4j_4jacoco.plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.adligo.tests4j.models.shared.AbstractTrial;
import org.adligo.tests4j.models.shared.coverage.I_PackageCoverage;
import org.adligo.tests4j.models.shared.system.I_CoverageRecorder;
import org.adligo.tests4j.models.shared.system.report.I_Tests4J_Reporter;
import org.adligo.tests4j_4jacoco.plugin.analysis.common.CoverageAnalyzer;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.ClassNameToInputStream;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_Runtime;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceFileCoverage;

public class Recorder implements I_CoverageRecorder {
	protected I_Tests4J_Reporter reporter;
	protected Tests4J_4JacocoMemory memory;
	private boolean root;
	private String scope;
	
	
	public Recorder(String pScope, Tests4J_4JacocoMemory pMemory, I_Tests4J_Reporter pLog) {
		scope = pScope;
		memory = pMemory;
		reporter = pLog;
	}
	
	@Override
	public String getScope() {
		return scope;
	}
	

	@Override
	public void startRecording() {
		try {
			I_Runtime runtime = memory.getRuntime();
			runtime.startup();
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
	
	@Override
	public List<I_PackageCoverage> getCoverage() {
		I_Runtime runtime = memory.getRuntime();

		/*
		final ExecutionDataStore executionData = new ExecutionDataStore();
		final SessionInfoStore sessionInfos = new SessionInfoStore();
		data.collect(executionData, sessionInfos, false);
		*/
		I_ProbesDataStore executionData = runtime.getCoverageData(scope);
		
		try {
			if (reporter.isLogEnabled(Recorder.class)) {
				List<String> classes = new ArrayList<String>();
				classes.add(AbstractTrial.class.getName());
				logCoverage(executionData, classes);
			}
		} catch (Exception x) {
			x.printStackTrace();
		}
		List<I_PackageCoverage> toRet = new ArrayList<I_PackageCoverage>();
		return toRet;
	}

	private void logCoverage(I_ProbesDataStore executionData, List<String> classes)
			throws IOException {
		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final CoverageAnalyzer analyzer = new CoverageAnalyzer(executionData, coverageBuilder);
		for (String clazz: classes) {
			analyzer.analyzeClass(ClassNameToInputStream.getTargetClass(clazz), clazz);
		}
		
		Collection<ISourceFileCoverage> sourceCoverages = coverageBuilder.getSourceFiles();
		
		Map<String, ISourceFileCoverage> coverageMap = new TreeMap<String, ISourceFileCoverage>();
		for (ISourceFileCoverage sfc: sourceCoverages) {
			String clazzName = sfc.getPackageName();
			StringBuilder sb = new StringBuilder();
			char [] chars = clazzName.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				char c = chars[i];
				if (c == File.separatorChar) {
					sb.append(".");
				} else {
					sb.append(c);
				}
			}
			String fileName = sfc.getName();
			fileName = fileName.substring(0, fileName.length() - 5);
			clazzName = sb.toString() + "." + fileName;
			coverageMap.put(clazzName, sfc);
		}
		Set<String> keys = coverageMap.keySet();
		for (String key: keys ) {
			reporter.log("found class " + key);
		}
			
		for (String clazz: classes) {
			logCoverage(coverageMap.get(clazz));
		}
	}

	public static String getPackageDir(final String name) {
		final String resource = '/' + name.replace('.', '/');
		int lastDot = resource.lastIndexOf("/");
		return resource.substring(0, lastDot);
	}
	
	private void logCoverage(ISourceFileCoverage cc) throws IOException {
		
		reporter.log("Coverage of class " + cc.getName());

		printCounter("instructions", cc.getInstructionCounter());
		printCounter("branches", cc.getBranchCounter());
		printCounter("lines", cc.getLineCounter());
		printCounter("methods", cc.getMethodCounter());
		printCounter("complexity", cc.getComplexityCounter());

		
		for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++) {
			ILine line = cc.getLine(i);
				
			reporter.log("Line " + i + " instructions " + 
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


	private void printCounter(final String unit, final ICounter counter) {
		final Integer missed = Integer.valueOf(counter.getMissedCount());
		final Integer total = Integer.valueOf(counter.getTotalCount());
		reporter.log("" + missed + " of " + total + " missed " + unit);
	}

	public boolean isRoot() {
		return root;
	}

	public void setRoot(boolean root) {
		this.root = root;
	}

	@Override
	public void stopRecording() {
		
	}
	
	
}
