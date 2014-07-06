package org.adligo.tests4j_4jacoco.plugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * this provides a way to load classes (mostly interfaces) in the default class loader
 * so that they are the same in child class loaders.
 * 
 * @author scott
 *
 */
public class SharedClassList {
	public static Set<String> WHITELIST = getSharedClassWhitelist();

	private static Set<String> getSharedClassWhitelist() {
		Set<String> toRet = new HashSet<String>();
	
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_AssertCommand");
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_AssertionData");
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_Asserts");
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_AssertType");
		
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_CompareAssertCommand");
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_CompareAssertionData");
		
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_ExpectedThrownData");
		
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_SimpleAssertCommand");
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_SimpleCompareAssertCommand");
		
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_Thrower");
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_ThrownAssertCommand");
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_ThrownAssertionData");
		
		toRet.add("org.adligo.tests4j.models.shared.asserts.line_text.I_LineTextCompareResult");
		
		toRet.add("org.adligo.tests4j.models.shared.asserts.uniform.I_Evaluation");
		toRet.add("org.adligo.tests4j.models.shared.asserts.uniform.I_EvaluatorLookup");
		toRet.add("org.adligo.tests4j.models.shared.asserts.uniform.I_UniformAssertionCommand");
		toRet.add("org.adligo.tests4j.models.shared.asserts.uniform.I_UniformAssertionEvaluator");
		
		toRet.add("org.adligo.tests4j.models.shared.common.TrialType");
		toRet.add("org.adligo.tests4j.models.shared.common.I_Platform");
		toRet.add("org.adligo.tests4j.models.shared.common.Platform");
		
		toRet.add("org.adligo.tests4j.models.shared.coverage.I_CoverageUnits");
		toRet.add("org.adligo.tests4j.models.shared.coverage.I_CoverageUnitsContainer");
		toRet.add("org.adligo.tests4j.models.shared.coverage.I_LineCoverage");
		toRet.add("org.adligo.tests4j.models.shared.coverage.I_LineCoverageSegment");
		toRet.add("org.adligo.tests4j.models.shared.coverage.I_PackageCoverage");
		toRet.add("org.adligo.tests4j.models.shared.coverage.I_SourceFileCoverage");
		
		toRet.add("org.adligo.tests4j.models.shared.metadata.I_SourceInfo");
		toRet.add("org.adligo.tests4j.models.shared.metadata.I_TestMetadata");
		toRet.add("org.adligo.tests4j.models.shared.metadata.I_TrialMetadata");
		toRet.add("org.adligo.tests4j.models.shared.metadata.I_TrialRunMetadata");
		toRet.add("org.adligo.tests4j.models.shared.metadata.I_UseCase");
		
		toRet.add("org.adligo.tests4j.models.shared.results.I_ApiTrialResult");
		toRet.add("org.adligo.tests4j.models.shared.results.I_Duration");
		toRet.add("org.adligo.tests4j.models.shared.results.I_SourceFileTrialResult");
		toRet.add("org.adligo.tests4j.models.shared.results.I_TestFailure");
		toRet.add("org.adligo.tests4j.models.shared.results.I_TestResult");
		toRet.add("org.adligo.tests4j.models.shared.results.I_TrialFailure");
		toRet.add("org.adligo.tests4j.models.shared.results.I_TrialResult");
		toRet.add("org.adligo.tests4j.models.shared.results.I_TrialRunResult");
		toRet.add("org.adligo.tests4j.models.shared.results.I_UseCaseTrialResult");
		
		toRet.add("org.adligo.tests4j.models.shared.results.feedback.I_ApiTrial_TestsResults");
		toRet.add("org.adligo.tests4j.models.shared.results.feedback.I_SourceFileTrial_TestsResults");
		
		toRet.add("org.adligo.tests4j.models.shared.system.I_ThreadCount");
		toRet.add("org.adligo.tests4j.models.shared.system.I_AssertListener");
		toRet.add("org.adligo.tests4j.models.shared.system.I_CoveragePlugin");
		toRet.add("org.adligo.tests4j.models.shared.system.I_Tests4J_Params");
		toRet.add("org.adligo.tests4j.models.shared.system.I_Tests4J_RemoteInfo");
		toRet.add("org.adligo.tests4j.models.shared.system.I_Tests4J_Reporter");
		
		toRet.add("org.adligo.tests4j.models.shared.i18n.I_Tests4J_Constants");
		toRet.add("org.adligo.tests4j.models.shared.i18n.eclipse.I_EclipseErrors");
		
		toRet.add("org.adligo.tests4j.models.shared.i18n.trials.I_Tests4J_AfterTrialErrors");
		toRet.add("org.adligo.tests4j.models.shared.i18n.trials.I_Tests4J_AfterTrialTestErrors");
		toRet.add("org.adligo.tests4j.models.shared.i18n.trials.I_Tests4J_AnnotationErrors");
		toRet.add("org.adligo.tests4j.models.shared.i18n.trials.I_Tests4J_BeforeTrialErrors");
		toRet.add("org.adligo.tests4j.models.shared.i18n.trials.I_Tests4J_TestMethodErrors");
		toRet.add("org.adligo.tests4j.models.shared.i18n.trials.I_Tests4J_TrialDescriptionMessages");
		
		toRet.add("org.adligo.tests4j.models.shared.i18n.asserts.I_Tests4J_AssertionInputMessages");
		toRet.add("org.adligo.tests4j.models.shared.i18n.asserts.I_Tests4J_AssertionResultMessages");
		
		toRet.add("org.adligo.tests4j.models.shared.trials.AfterTrial");
		toRet.add("org.adligo.tests4j.models.shared.trials.BeforeTrial");
		toRet.add("org.adligo.tests4j.models.shared.trials.I_AbstractTrial");
		toRet.add("org.adligo.tests4j.models.shared.trials.I_ApiTrial");
		toRet.add("org.adligo.tests4j.models.shared.trials.IgnoreTest");
		toRet.add("org.adligo.tests4j.models.shared.trials.IgnoreTrial");
		toRet.add("org.adligo.tests4j.models.shared.trials.I_Trial");
		toRet.add("org.adligo.tests4j.models.shared.trials.I_TrialBindings");
		toRet.add("org.adligo.tests4j.models.shared.trials.I_MetaTrial");
		toRet.add("org.adligo.tests4j.models.shared.trials.I_SourceFileTrial");
		toRet.add("org.adligo.tests4j.models.shared.trials.I_UseCaseTrial");
		
		
		toRet.add("org.adligo.tests4j.models.shared.trials.PackageScope");
		toRet.add("org.adligo.tests4j.models.shared.trials.SourceFileScope");
		toRet.add("org.adligo.tests4j.models.shared.trials.Test");
		toRet.add("org.adligo.tests4j.models.shared.trials.TrialTypeAnnotation");
		toRet.add("org.adligo.tests4j.models.shared.trials.UseCaseScope");
		
		toRet.add("org.adligo.tests4j_4jacoco.plugin.data.common.I_Probes");
		
		return Collections.unmodifiableSet(toRet);
	}
}
