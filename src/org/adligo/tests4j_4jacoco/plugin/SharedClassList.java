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
	
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_AssertCompareFailure");
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_AssertCommand");
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_AssertionData");
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_AssertThrownFailure");
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_Asserts");
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_AssertType");
		
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_CollectionContainsAssertionData");
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_CollectionAssertionData");
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_CompareAssertCommand");
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_CompareAssertionData");
		
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_ExpectedThrownData");
		
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_SimpleAssertCommand");
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_SimpleCompareAssertCommand");
		
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_TestFailure");
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_TestFailureType");
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_ThrowableInfo");
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_Thrower");
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_ThrownAssertCommand");
		toRet.add("org.adligo.tests4j.models.shared.asserts.common.I_ThrownAssertionData");
		
		
		toRet.add("org.adligo.tests4j.models.shared.asserts.line_text.I_DiffIndexes");
		toRet.add("org.adligo.tests4j.models.shared.asserts.line_text.I_DiffIndexesPair");
		toRet.add("org.adligo.tests4j.models.shared.asserts.line_text.I_LineDiff");
		toRet.add("org.adligo.tests4j.models.shared.asserts.line_text.I_LineDiffType");
		toRet.add("org.adligo.tests4j.models.shared.asserts.line_text.I_TextLines");
		toRet.add("org.adligo.tests4j.models.shared.asserts.line_text.I_TextLinesCompareResult");
		
		toRet.add("org.adligo.tests4j.models.shared.asserts.uniform.I_Evaluation");
		toRet.add("org.adligo.tests4j.models.shared.asserts.uniform.I_EvaluatorLookup");
		toRet.add("org.adligo.tests4j.models.shared.asserts.uniform.I_UniformAssertionCommand");
		toRet.add("org.adligo.tests4j.models.shared.asserts.uniform.I_UniformAssertionEvaluator");
		
		
		toRet.add("org.adligo.tests4j.models.shared.asserts.uniform.I_UniformThrownAssertionCommand");
		toRet.add("org.adligo.tests4j.models.shared.asserts.uniform.I_UniformThrownAssertionEvaluator");
		
		toRet.add("org.adligo.tests4j.models.shared.common.I_TrialType");
		toRet.add("org.adligo.tests4j.models.shared.common.I_Platform");
		toRet.add("org.adligo.tests4j.models.shared.common.I_PlatformContainer");
		toRet.add("org.adligo.tests4j.models.shared.common.I_Immutable");
		toRet.add("org.adligo.tests4j.models.shared.common.I_System");
		
		toRet.add("org.adligo.tests4j.models.shared.coverage.I_CoverageUnits");
		toRet.add("org.adligo.tests4j.models.shared.coverage.I_CoverageUnitsContainer");
		toRet.add("org.adligo.tests4j.models.shared.coverage.I_LineCoverage");
		toRet.add("org.adligo.tests4j.models.shared.coverage.I_LineCoverageSegment");
		toRet.add("org.adligo.tests4j.models.shared.coverage.I_PackageCoverage");
		toRet.add("org.adligo.tests4j.models.shared.coverage.I_SourceFileCoverage");
		
		toRet.add("org.adligo.tests4j.models.shared.dependency.I_ClassDependencies");
		toRet.add("org.adligo.tests4j.models.shared.dependency.I_ClassFilter");
		toRet.add("org.adligo.tests4j.models.shared.dependency.I_ClassFilterModel");
		toRet.add("org.adligo.tests4j.models.shared.dependency.I_ClassReferences");
		toRet.add("org.adligo.tests4j.models.shared.dependency.I_Dependency");
		
		toRet.add("org.adligo.tests4j.models.shared.metadata.I_SourceInfoMetadata");
		toRet.add("org.adligo.tests4j.models.shared.metadata.I_TestMetadata");
		toRet.add("org.adligo.tests4j.models.shared.metadata.I_TrialMetadata");
		toRet.add("org.adligo.tests4j.models.shared.metadata.I_TrialRunMetadata");
		toRet.add("org.adligo.tests4j.models.shared.metadata.I_UseCaseMetadata");
		
		toRet.add("org.adligo.tests4j.models.shared.results.I_ApiTrialResult");
		toRet.add("org.adligo.tests4j.models.shared.results.I_Duration");
		toRet.add("org.adligo.tests4j.models.shared.results.I_SourceFileTrialResult");
		
		toRet.add("org.adligo.tests4j.models.shared.results.I_TestResult");
		
		toRet.add("org.adligo.tests4j.models.shared.results.I_TrialFailure");
		toRet.add("org.adligo.tests4j.models.shared.results.I_TrialResult");
		toRet.add("org.adligo.tests4j.models.shared.results.I_TrialRunResult");
		toRet.add("org.adligo.tests4j.models.shared.results.I_UseCaseTrialResult");
		
		toRet.add("org.adligo.tests4j.models.shared.system.I_Tests4J_AssertListener");
		toRet.add("org.adligo.tests4j.models.shared.system.I_Tests4J_Controls");
		toRet.add("org.adligo.tests4j.models.shared.system.I_Tests4J_CoveragePlugin");
		toRet.add("org.adligo.tests4j.models.shared.system.I_Tests4J_CoverageRecorder");
		
		toRet.add("org.adligo.tests4j.models.shared.system.I_Tests4J_Delegate");
		toRet.add("org.adligo.tests4j.models.shared.system.I_Tests4J_DelegateFactory");
		
		toRet.add("org.adligo.tests4j.models.shared.system.I_Tests4J_Listener");
		toRet.add("org.adligo.tests4j.models.shared.system.I_Tests4J_Log");
		
		toRet.add("org.adligo.tests4j.models.shared.system.I_Tests4J_Params");
		toRet.add("org.adligo.tests4j.models.shared.system.I_Tests4J_RemoteInfo");
		
		toRet.add("org.adligo.tests4j.models.shared.system.I_Tests4J_TestFinishedListener");
		toRet.add("org.adligo.tests4j.models.shared.system.I_Tests4J_TrialList");
		
		toRet.add("org.adligo.tests4j.models.shared.i18n.I_Tests4J_AnnotationErrors");
		toRet.add("org.adligo.tests4j.models.shared.i18n.I_Tests4J_AssertionInputMessages");
		toRet.add("org.adligo.tests4j.models.shared.i18n.I_Tests4J_ResultMessages");
		
		toRet.add("org.adligo.tests4j.models.shared.i18n.I_Tests4J_Constants");
		toRet.add("org.adligo.tests4j.models.shared.i18n.I_Tests4J_EclipseErrors");
		
		toRet.add("org.adligo.tests4j.models.shared.i18n.I_Tests4J_LineDiffTextDisplayMessages");
		toRet.add("org.adligo.tests4j.models.shared.i18n.I_Tests4J_ParamReaderMessages");
		toRet.add("org.adligo.tests4j.models.shared.i18n.I_Tests4J_ReportMessages");
		
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
		
		toRet.add("org.adligo.tests4j.models.shared.xml.I_XML_Builder");
		toRet.add("org.adligo.tests4j.models.shared.xml.I_XML_Consumer");
		toRet.add("org.adligo.tests4j.models.shared.xml.I_XML_Producer");
		
		toRet.add("org.adligo.tests4j.run.discovery.I_ClassDependenciesCache");
		
		
		toRet.add("org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader");
		
		toRet.add("org.adligo.tests4j.run.remote.socket_api.I_AfterMessageHandler");
		
		toRet.add("org.adligo.tests4j_4jacoco.plugin.data.common.I_Probes");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.data.common.I_ClassProbes");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.data.common.I_ClassProbesMutant");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStoreAdaptor");
		
		toRet.add("org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ClassInstrumentationInfo");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ClassProbesVisitor");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_MethodProbesVisitor");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ProbeIdGenerator");
		
		toRet.add("org.adligo.tests4j_4jacoco.plugin.runtime.I_ProbeDataAccessorFactory");
		
		toRet.add("org.adligo.tests4j_4jacoco.plugin.discovery.I_DiscoveryMemory");
		
		toRet.add("org.adligo.tests4j_4jacoco.plugin.asm.I_StackHelper");
		
		for (String clazz: toRet) {
			try {
				//actually load all of the classes using the default classloader here
				Class.forName(clazz);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return Collections.unmodifiableSet(toRet);
	}
}
