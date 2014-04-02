package org.adligo.jacoco4jtests.run;

import java.util.List;

import org.adligo.jacoco4jtests.run.setup.PackageSet;
import org.adligo.jacoco4jtests.wrapper.JacocoCoverageRecorder;
import org.adligo.jtests.models.shared.API_Trial;
import org.adligo.jtests.models.shared.ClassScope;
import org.adligo.jtests.models.shared.ClassTrial;
import org.adligo.jtests.models.shared.I_AbstractTrial;
import org.adligo.jtests.models.shared.JTrialType;
import org.adligo.jtests.models.shared.PackageScope;
import org.adligo.jtests.models.shared.Test;
import org.adligo.jtests.models.shared.UseCaseTrial;
import org.adligo.jtests.models.shared.common.TrialType;
import org.adligo.jtests.models.shared.results.I_TrialResult;
import org.adligo.jtests.models.shared.results.I_TrialRunResult;
import org.adligo.jtests.models.shared.results.TrialResult;
import org.adligo.jtests.models.shared.results.TrialRunResult;
import org.adligo.jtests.models.shared.system.I_TrialResultsProcessor;
import org.adligo.jtests.models.shared.system.I_TrialRunListener;
import org.adligo.jtests.models.shared.system.RunParameters;
import org.adligo.jtests.run.I_JTests;
import org.adligo.jtests.run.JTests;
import org.jacoco.core.instr.Instrumenter;

public class JacocoRunner implements I_TrialRunListener, I_JTests {
	private I_TrialRunListener trialRunListener;
	JacocoCoverageRecorder recorder = new JacocoCoverageRecorder();
	
	private boolean log = true;
	
	
	
	public JacocoRunner() {
		
	}
	
	public void run(RunParameters pParams) {
		runInternal(pParams, this);
	}
	
	public void run(RunParameters pParams, I_TrialRunListener pProcessor) {
		runInternal(pParams, pProcessor);
	}
	
	
	private void runInternal(RunParameters pParams, I_TrialRunListener pProcessor) {
		trialRunListener = pProcessor;
		pParams.setCoverageRecorder(recorder);
		Instrumenter instr = recorder.getInstrumenter();
		PackageSet packages = getPackages(pParams);
		
		// In this tutorial we use a special class loader to directly load the
		// instrumented class definition from a byte[] instances.
		RunParameters newParams = recorder.loadClasses(packages, pParams);
		JTests jt = new JTests();
		jt.run(newParams, this);
	}

	
	private PackageSet getPackages(RunParameters pParams) {
		PackageSet packages = new PackageSet();
		//ok find what may show up in the coverage
		List<Class<? extends I_AbstractTrial>> trials = pParams.getTrials();
		for (Class<? extends I_AbstractTrial> trial: trials) {
			PackageScope ps = trial.getAnnotation(PackageScope.class);
			if (ps != null) {
				String pkg = ps.packageName();
				packages.add(pkg);
			} else {
				ClassScope cs = trial.getAnnotation(ClassScope.class);
				String pkg = cs.testedClass().getPackage().getName();
				packages.add(pkg);
			}
		}
		return packages;
	}

	@Override
	public void onTestCompleted(Class<? extends I_AbstractTrial> testClass,
			I_AbstractTrial test, I_TrialResult result) {
		if (trialRunListener != null) {
			trialRunListener.onTestCompleted(testClass, test, result);
		}
	}

	@Override
	public void onRunCompleted(I_TrialRunResult result) {
		recorder.getCoverage();
		if (trialRunListener != null) {
			trialRunListener.onRunCompleted(result);
		} else {
			System.exit(0);
		}
	}
	

 }
