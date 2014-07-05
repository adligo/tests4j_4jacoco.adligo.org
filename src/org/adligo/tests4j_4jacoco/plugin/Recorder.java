package org.adligo.tests4j_4jacoco.plugin;

import java.util.List;

import org.adligo.tests4j.models.shared.coverage.I_PackageCoverage;
import org.adligo.tests4j.models.shared.system.I_CoverageRecorder;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Reporter;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.coverage.LazyPackageCoverageFactory;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_Runtime;

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
		
		return LazyPackageCoverageFactory.create(executionData, memory);
	}


	public static String getPackageDir(final String name) {
		final String resource = '/' + name.replace('.', '/');
		int lastDot = resource.lastIndexOf("/");
		return resource.substring(0, lastDot);
	}
	



	public boolean isRoot() {
		return root;
	}

	public void setRoot(boolean root) {
		this.root = root;
	}

	
}

class CoverageDetail {}