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
		toRet.add("org.adligo.tests4j.models.shared.AfterTrial");
		toRet.add("org.adligo.tests4j.models.shared.BeforeTrial");
		toRet.add("org.adligo.tests4j.models.shared.I_AbstractTrial");
		toRet.add("org.adligo.tests4j.models.shared.IgnoreTest");
		toRet.add("org.adligo.tests4j.models.shared.IgnoreTrial");
		toRet.add("org.adligo.tests4j.models.shared.I_Trial");
		toRet.add("org.adligo.tests4j.models.shared.I_TrialProcessorBindings");
		toRet.add("org.adligo.tests4j.models.shared.I_MetaTrial");
		
		toRet.add("org.adligo.tests4j.models.shared.PackageScope");
		toRet.add("org.adligo.tests4j.models.shared.SourceFileScope");
		toRet.add("org.adligo.tests4j.models.shared.Test");
		toRet.add("org.adligo.tests4j.models.shared.TrialType");
		toRet.add("org.adligo.tests4j.models.shared.UseCaseScope");
		
		
		toRet.add("org.adligo.tests4j.models.shared.asserts.AssertType");
		toRet.add("org.adligo.tests4j.models.shared.asserts.I_AssertType");
		toRet.add("org.adligo.tests4j.models.shared.asserts.I_Thrower");
		toRet.add("org.adligo.tests4j.models.shared.asserts.line_text.I_LineTextCompareResult");
		
		toRet.add("org.adligo.tests4j.models.shared.common.TrialTypeEnum");
		
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
		
		
		toRet.add("org.adligo.tests4j.models.shared.system.I_ThreadCount");
		toRet.add("org.adligo.tests4j.models.shared.system.I_AssertListener");
		toRet.add("org.adligo.tests4j.models.shared.system.I_CoveragePlugin");
		toRet.add("org.adligo.tests4j.models.shared.system.I_Tests4J_Params");
		toRet.add("org.adligo.tests4j.models.shared.system.I_Tests4J_RemoteInfo");
		
		toRet.add("org.adligo.tests4j.models.shared.system.i18n.I_Tests4J_Constants");
		toRet.add("org.adligo.tests4j.models.shared.system.i18n.eclipse.I_EclipseErrors");
		
		toRet.add("org.adligo.tests4j.models.shared.system.i18n.trials.I_Tests4J_AfterTrialErrors");
		toRet.add("org.adligo.tests4j.models.shared.system.i18n.trials.I_Tests4J_AfterTrialTestErrors");
		toRet.add("org.adligo.tests4j.models.shared.system.i18n.trials.I_Tests4J_AnnotationErrors");
		toRet.add("org.adligo.tests4j.models.shared.system.i18n.trials.I_Tests4J_BeforeTrialErrors");
		toRet.add("org.adligo.tests4j.models.shared.system.i18n.trials.I_Tests4J_TestMethodErrors");
		toRet.add("org.adligo.tests4j.models.shared.system.i18n.trials.I_Tests4J_TrialDescriptionMessages");
		
		toRet.add("org.adligo.tests4j.models.shared.system.i18n.trials.asserts.I_Tests4J_AssertionInputMessages");
		toRet.add("org.adligo.tests4j.models.shared.system.i18n.trials.asserts.I_Tests4J_AssertionResultMessages");
		
		toRet.add("org.adligo.tests4j_4jacoco.plugin.data.common.I_Probes");
		
		return Collections.unmodifiableSet(toRet);
	}
}
