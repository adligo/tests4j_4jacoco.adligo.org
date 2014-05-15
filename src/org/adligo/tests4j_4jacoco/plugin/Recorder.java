package org.adligo.tests4j_4jacoco.plugin;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.adligo.tests4j.models.shared.AbstractTrial;
import org.adligo.tests4j.models.shared.coverage.I_PackageCoverage;
import org.adligo.tests4j.models.shared.system.ByteListOutputStream;
import org.adligo.tests4j.models.shared.system.I_CoverageRecorder;
import org.adligo.tests4j.models.shared.system.report.I_Tests4J_Reporter;
import org.adligo.tests4j_4jacoco.plugin.analysis.common.CoverageAnalyzer;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.coverage.LazyPackageCoverageFactory;
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
	private I_Runtime runtime;

	
	public Recorder(String pScope, Tests4J_4JacocoMemory pMemory, I_Tests4J_Reporter pLog) {
		scope = pScope;
		memory = pMemory;
		reporter = pLog;
		runtime = memory.getRuntime();
	}
	
	@Override
	public String getScope() {
		return scope;
	}
	

	@Override
	public void startRecording() {
		if (reporter != null) {
			if (reporter.isLogEnabled(Recorder.class)) {
				reporter.log("Recorder starting " + scope);
			}
		}
		try {
			runtime.startup(scope);
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
	
	@Override
	public List<I_PackageCoverage> endRecording() {
		if (reporter != null) {
			if (reporter.isLogEnabled(Recorder.class)) {
				reporter.log("Ending Recording " + scope);
			}
		}
		/*
		final ExecutionDataStore executionData = new ExecutionDataStore();
		final SessionInfoStore sessionInfos = new SessionInfoStore();
		data.collect(executionData, sessionInfos, false);
		*/
		I_ProbesDataStore executionData = runtime.end(scope);
		try {
			if (reporter != null) {
				if (reporter.isLogEnabled(Recorder.class)) {
					List<String> classes = new ArrayList<String>();
					classes.add(AbstractTrial.class.getName());
					classes.add(ByteListOutputStream.class.getName());
					logCoverage(executionData, classes);
				}
			}
		} catch (Exception x) {
			x.printStackTrace();
		}
		return LazyPackageCoverageFactory.create(executionData, memory);
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
		
		
		if (reporter.isLogEnabled(CoverageDetail.class)) {
			reporter.log("Coverage of class " + cc.getName());

			printCounter("\tinstructions", cc.getInstructionCounter());
			printCounter("\tbranches", cc.getBranchCounter());
			printCounter("\tlines", cc.getLineCounter());
			printCounter("\tmethods", cc.getMethodCounter());
			printCounter("\tcomplexity", cc.getComplexityCounter());
		}
		double cus = 0;
		double ccus = 0;
		for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++) {
			ILine line = cc.getLine(i);
			cus = cus + line.getInstructionCounter().getTotalCount()
						+ line.getBranchCounter().getTotalCount();
			ccus = ccus + line.getInstructionCounter().getCoveredCount() 
					+ line.getBranchCounter().getCoveredCount();
			
			if (reporter.isLogEnabled(CoverageDetail.class)) {	
				reporter.log("\tLine " + i + " instructions " + 
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
		double pct = ccus/cus * 100;
		DecimalFormat df = new DecimalFormat("##.##");
		reporter.log(cc.getName() + " is " + df.format(pct) 
				+ " percent covered ");
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
	public void pauseRecording() {
		if (reporter != null) {
			if (reporter.isLogEnabled(Recorder.class)) {
				reporter.log("Recording on Pause " + scope);
			}
		}
		runtime.pause(scope);
	}
	
	
}

class CoverageDetail {}