package org.adligo.tests4j_4jacoco.plugin.whitelists;

import org.adligo.tests4j.run.common.ClassesDelegate;
import org.adligo.tests4j.run.common.I_Classes;
import org.adligo.tests4j_4jacoco.plugin.common.I_InstrumentedClassDependencies;

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
public class RequiredList extends BaseClassList {
	private static Set<String> getSharedClassWhitelist() {
		Set<String> toRet = new HashSet<String>();
	
    
    toRet.add("org.adligo.tests4j.models.shared.coverage.I_ClassProbes");
    toRet.add("org.adligo.tests4j.models.shared.coverage.I_ClassProbesMutant");
    toRet.add("org.adligo.tests4j.models.shared.coverage.I_CoverageIntContainer");
    toRet.add("org.adligo.tests4j.models.shared.coverage.I_CoverageIntContainerMutant");
    toRet.add("org.adligo.tests4j.models.shared.coverage.I_CoverageUnits");
		toRet.add("org.adligo.tests4j.models.shared.coverage.I_CoverageUnitsContainer");
		
		toRet.add("org.adligo.tests4j.models.shared.coverage.I_LineCoverage");
		toRet.add("org.adligo.tests4j.models.shared.coverage.I_LineCoverageSegment");
		toRet.add("org.adligo.tests4j.models.shared.coverage.I_PackageCoverageBrief");
		
		toRet.add("org.adligo.tests4j.models.shared.coverage.I_PercentCovered");
		toRet.add("org.adligo.tests4j.models.shared.coverage.I_Probes");

		toRet.add("org.adligo.tests4j.models.shared.coverage.I_SourceFileCoverage");
		toRet.add("org.adligo.tests4j.models.shared.coverage.I_SourceFileCoverageBrief");
		toRet.add("org.adligo.tests4j.models.shared.coverage.I_SourceFileCoverageBriefMutant");
		
		toRet.add("org.adligo.tests4j.models.shared.association.I_ClassParents");
		toRet.add("org.adligo.tests4j.models.shared.association.I_ClassParentsCache");
		toRet.add("org.adligo.tests4j.models.shared.association.I_ClassParentsLocal");
		toRet.add("org.adligo.tests4j.models.shared.association.I_ClassAssociations");
		toRet.add("org.adligo.tests4j.models.shared.association.I_ClassAssociationsCache");
		toRet.add("org.adligo.tests4j.models.shared.association.I_ClassAssociationsLocal");
		
		toRet.add("org.adligo.tests4j.models.shared.metadata.I_SourceInfoMetadata");
		toRet.add("org.adligo.tests4j.models.shared.metadata.I_TestMetadata");
		toRet.add("org.adligo.tests4j.models.shared.metadata.I_TrialMetadata");
		toRet.add("org.adligo.tests4j.models.shared.metadata.I_TrialRunMetadata");
		toRet.add("org.adligo.tests4j.models.shared.metadata.I_UseCaseBrief");
		
		
		toRet.add("org.adligo.tests4j.models.shared.results.I_ApiTrialResult");
		
		toRet.add("org.adligo.tests4j.models.shared.results.I_Duration");
		toRet.add("org.adligo.tests4j.models.shared.results.I_PhaseState");
		toRet.add("org.adligo.tests4j.models.shared.results.I_SourceFileTrialResult");
		
		toRet.add("org.adligo.tests4j.models.shared.results.I_TestResult");
		
		toRet.add("org.adligo.tests4j.models.shared.results.I_TrialFailure");
		toRet.add("org.adligo.tests4j.models.shared.results.I_TrialResult");
		toRet.add("org.adligo.tests4j.models.shared.results.I_TrialRunResult");
		toRet.add("org.adligo.tests4j.models.shared.results.I_UseCaseTrialResult");
		
		
		toRet.add("org.adligo.tests4j.system.shared.api.I_Tests4J_Controls");
		toRet.add("org.adligo.tests4j.system.shared.api.I_Tests4J_CoveragePlugin");

		toRet.add("org.adligo.tests4j.system.shared.api.I_Tests4J_CoveragePluginParams");
		toRet.add("org.adligo.tests4j.system.shared.api.I_Tests4J_CoverageRecorder");
		toRet.add("org.adligo.tests4j.system.shared.api.I_Tests4J_CoverageTrialInstrumentation");
		
		toRet.add("org.adligo.tests4j.system.shared.api.I_Tests4J_Delegate");
		toRet.add("org.adligo.tests4j.system.shared.api.I_Tests4J_DelegateFactory");
		
		toRet.add("org.adligo.tests4j.system.shared.api.I_Tests4J_Listener");
		
		toRet.add("org.adligo.tests4j.system.shared.api.I_Tests4J_Params");
		
		toRet.add("org.adligo.tests4j.system.shared.api.I_Tests4J_ProgressParams");
		toRet.add("org.adligo.tests4j.system.shared.api.I_Tests4J_RemoteInfo");
		toRet.add("org.adligo.tests4j.system.shared.api.I_Tests4J_Runnable");
		
		toRet.add("org.adligo.tests4j.system.shared.api.I_Tests4J_TestFinishedListener");
		toRet.add("org.adligo.tests4j.system.shared.api.I_Tests4J_TrialList");
		toRet.add("org.adligo.tests4j.system.shared.api.I_Tests4J_TrialProgress");
		toRet.add("org.adligo.tests4j.system.shared.api.I_Tests4J_SourceInfoParams");
		
		toRet.add("org.adligo.tests4j.system.shared.trials.AfterTrial");
		toRet.add("org.adligo.tests4j.system.shared.trials.BeforeTrial");
		toRet.add("org.adligo.tests4j.system.shared.trials.I_AbstractTrial");
		toRet.add("org.adligo.tests4j.system.shared.trials.I_ApiTrial");
		
		toRet.add("org.adligo.tests4j.system.shared.trials.IgnoreTest");
		toRet.add("org.adligo.tests4j.system.shared.trials.IgnoreTrial");
		toRet.add("org.adligo.tests4j.system.shared.trials.I_Trial");
		
		toRet.add("org.adligo.tests4j.system.shared.trials.I_MetaTrialInputData");
		toRet.add("org.adligo.tests4j.system.shared.trials.I_MetaTrialParams");
		toRet.add("org.adligo.tests4j.system.shared.trials.I_MetaTrialParamsAware");
		
		toRet.add("org.adligo.tests4j.system.shared.trials.I_TrialParams");
		toRet.add("org.adligo.tests4j.system.shared.trials.I_TrialParamsAware");
		toRet.add("org.adligo.tests4j.system.shared.trials.I_TrialParamsFactory");
		
		toRet.add("org.adligo.tests4j.system.shared.trials.I_TrialBindings");
		toRet.add("org.adligo.tests4j.system.shared.trials.I_MetaTrial");
		toRet.add("org.adligo.tests4j.system.shared.trials.I_Progress");
		toRet.add("org.adligo.tests4j.system.shared.trials.I_SourceFileTrial");
		toRet.add("org.adligo.tests4j.system.shared.trials.I_SubProgress");
		toRet.add("org.adligo.tests4j.system.shared.trials.I_UseCaseTrial");
		
		toRet.add("org.adligo.tests4j.system.shared.trials.PackageScope");
		toRet.add("org.adligo.tests4j.system.shared.trials.SourceFileScope");
		toRet.add("org.adligo.tests4j.system.shared.trials.SuppressOutput");
		toRet.add("org.adligo.tests4j.system.shared.trials.Test");
		toRet.add("org.adligo.tests4j.system.shared.trials.TrialTypeAnnotation");
		toRet.add("org.adligo.tests4j.system.shared.trials.UseCaseScope");
		
		toRet.add("org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader");
		toRet.add("org.adligo.tests4j.run.common.I_JseSystem");
		toRet.add("org.adligo.tests4j.run.common.I_Memory");
		toRet.add("org.adligo.tests4j.run.common.I_Notifier");
		
		toRet.add("org.adligo.tests4j.run.remote.socket_api.I_AfterMessageHandler");

		toRet.add("org.adligo.tests4j.shared.asserts.common.I_AssertCompareFailure");
		toRet.add("org.adligo.tests4j.shared.asserts.common.I_AssertCommand");
		toRet.add("org.adligo.tests4j.shared.asserts.common.I_AssertionData");
		toRet.add("org.adligo.tests4j.shared.asserts.common.I_AssertThrownFailure");
		toRet.add("org.adligo.tests4j.shared.asserts.common.I_Asserts");
		toRet.add("org.adligo.tests4j.shared.asserts.common.I_AssertType");
		
		toRet.add("org.adligo.tests4j.shared.asserts.common.I_CollectionContainsAssertionData");
		toRet.add("org.adligo.tests4j.shared.asserts.common.I_CollectionAssertionData");
		toRet.add("org.adligo.tests4j.shared.asserts.common.I_CompareAssertCommand");
		toRet.add("org.adligo.tests4j.shared.asserts.common.I_CompareAssertionData");
		
		toRet.add("org.adligo.tests4j.shared.asserts.common.I_ExpectedThrowable");
		
		toRet.add("org.adligo.tests4j.shared.asserts.common.I_SimpleAssertCommand");
		toRet.add("org.adligo.tests4j.shared.asserts.common.I_SimpleCompareAssertCommand");
		
		toRet.add("org.adligo.tests4j.shared.asserts.common.I_AssertListener");
		toRet.add("org.adligo.tests4j.shared.asserts.common.I_AssertType");
		toRet.add("org.adligo.tests4j.shared.asserts.common.I_MatchType");
		toRet.add("org.adligo.tests4j.shared.asserts.common.I_TestFailure");
		toRet.add("org.adligo.tests4j.shared.asserts.common.I_TestFailureType");
		toRet.add("org.adligo.tests4j.shared.asserts.common.I_ThrowableInfo");
		toRet.add("org.adligo.tests4j.shared.asserts.common.I_Thrower");
		toRet.add("org.adligo.tests4j.shared.asserts.common.I_ThrownAssertCommand");
		toRet.add("org.adligo.tests4j.shared.asserts.common.I_ThrownAssertionData");
		toRet.add("org.adligo.tests4j.shared.asserts.common.I_SourceTestFailure");
		
		
		toRet.add("org.adligo.tests4j.shared.asserts.reference.I_ClassAttributes");
		toRet.add("org.adligo.tests4j.shared.asserts.reference.I_ClassAlias");
		toRet.add("org.adligo.tests4j.shared.asserts.reference.I_ClassAliasLocal");
		toRet.add("org.adligo.tests4j.shared.asserts.reference.I_Dependency");
		toRet.add("org.adligo.tests4j.shared.asserts.reference.I_ReferenceGroup");
		toRet.add("org.adligo.tests4j.shared.asserts.reference.I_AllowedReferencesFailure");
		toRet.add("org.adligo.tests4j.shared.asserts.reference.I_CircularDependencyFailure");
		toRet.add("org.adligo.tests4j.shared.asserts.reference.I_CircularDependencies");
		toRet.add("org.adligo.tests4j.shared.asserts.reference.I_FieldSignature");
		toRet.add("org.adligo.tests4j.shared.asserts.reference.I_MethodSignature");
		toRet.add("org.adligo.tests4j.shared.asserts.reference.AllowedReferences");
		
		
		toRet.add("org.adligo.tests4j.shared.asserts.line_text.I_DiffIndexes");
		toRet.add("org.adligo.tests4j.shared.asserts.line_text.I_DiffIndexesPair");
		toRet.add("org.adligo.tests4j.shared.asserts.line_text.I_LineDiff");
		toRet.add("org.adligo.tests4j.shared.asserts.line_text.I_LineDiffType");
		toRet.add("org.adligo.tests4j.shared.asserts.line_text.I_TextLines");
		toRet.add("org.adligo.tests4j.shared.asserts.line_text.I_TextLinesCompareResult");
		
		toRet.add("org.adligo.tests4j.shared.asserts.uniform.I_Evaluation");
		toRet.add("org.adligo.tests4j.shared.asserts.uniform.I_EvaluatorLookup");
		toRet.add("org.adligo.tests4j.shared.asserts.uniform.I_UniformAssertionCommand");
		toRet.add("org.adligo.tests4j.shared.asserts.uniform.I_UniformAssertionEvaluator");
		
		
		toRet.add("org.adligo.tests4j.shared.asserts.uniform.I_UniformThrownAssertionCommand");
		toRet.add("org.adligo.tests4j.shared.asserts.uniform.I_UniformThrownAssertionEvaluator");
		
		toRet.add("org.adligo.tests4j.shared.common.I_TrialType");
		toRet.add("org.adligo.tests4j.shared.common.I_Platform");
		toRet.add("org.adligo.tests4j.shared.common.I_PlatformContainer");
		toRet.add("org.adligo.tests4j.shared.common.I_Immutable");
		toRet.add("org.adligo.tests4j.shared.common.I_Time");
		toRet.add("org.adligo.tests4j.shared.common.I_System");
		
		toRet.add("org.adligo.tests4j.shared.i18n.I_Tests4J_AnnotationMessages");
		toRet.add("org.adligo.tests4j.shared.i18n.I_Tests4J_AssertionInputMessages");
		
		toRet.add("org.adligo.tests4j.shared.i18n.I_Tests4J_Constants");
		toRet.add("org.adligo.tests4j.shared.i18n.I_Tests4J_CoveragePluginMessages");
		toRet.add("org.adligo.tests4j.shared.i18n.I_Tests4J_EclipseErrors");
		
		toRet.add("org.adligo.tests4j.shared.i18n.I_Tests4J_LineDiffTextDisplayMessages");
		toRet.add("org.adligo.tests4j.shared.i18n.I_Tests4J_LogMessages");
		toRet.add("org.adligo.tests4j.shared.i18n.I_Tests4J_ParamsReaderMessages");
		toRet.add("org.adligo.tests4j.shared.i18n.I_Tests4J_ReportMessages");
		toRet.add("org.adligo.tests4j.shared.i18n.I_Tests4J_ResultMessages");
		
		toRet.add("org.adligo.tests4j.shared.output.I_ConcurrentOutputDelegator");
		toRet.add("org.adligo.tests4j.shared.output.I_OutputBuffer");
		toRet.add("org.adligo.tests4j.shared.output.I_OutputDelegateor");
		toRet.add("org.adligo.tests4j.shared.output.I_ToggleOutputBuffer");
		toRet.add("org.adligo.tests4j.shared.output.I_Tests4J_Log");
		
		toRet.add("org.adligo.tests4j.shared.xml.I_XML_Builder");
		toRet.add("org.adligo.tests4j.shared.xml.I_XML_Consumer");
		toRet.add("org.adligo.tests4j.shared.xml.I_XML_Producer");
		
		toRet.add("org.adligo.tests4j.run.discovery.I_PackageDiscovery");
    toRet.add("org.adligo.tests4j.run.helpers.I_ClassFilter");
    toRet.add("org.adligo.tests4j.run.helpers.I_ClassFilterModel");
		add4JacocoAndAsmClasses(toRet);
		
		return Collections.unmodifiableSet(toRet);
	}

	protected static void add4JacocoAndAsmClasses(Set<String> toRet) {
		toRet.add("org.adligo.tests4j_4jacoco.plugin.common.I_ClassInstrumenter");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.common.I_ClassInstrumenterFactory");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.common.I_CoveragePluginMemory");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.common.I_Instrumenter");
		
		toRet.add("org.adligo.tests4j_4jacoco.plugin.common.I_InstrumentedClassDependencies");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.common.I_LoggerDataAccessorFactory");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.common.I_ObtainProbesStrategy");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.common.I_OrderedClassDependencies");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.common.I_OrderedClassDiscovery");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.common.I_OrderedClassDiscoveryFactory");
		
		
		toRet.add("org.adligo.tests4j_4jacoco.plugin.common.I_ProbeDataAccessorFactory");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.common.I_Runtime");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.common.I_StackHelper");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.common.I_TrialInstrumenter");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.common.I_TrialInstrumenterFactory");
		

		toRet.add("org.adligo.tests4j_4jacoco.plugin.data.common.I_MultiRecordingProbeDataStore");

		toRet.add("org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStoreMutant");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStoreAdaptor");
		
		toRet.add("org.adligo.tests4j_4jacoco.plugin.instrumentation.AbstractProbeInserter");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.instrumentation.I_ProbeInserterFactory");
		
		
		toRet.add("org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ClassInstrumentationInfo");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ClassInstrumentationMetadata");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ClassInstrumentationMetadataStore");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ClassInstrumentationMetadataStoreMutant");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ClassProbesVisitor");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_MethodProbesVisitor");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ProbeIdGenerator");
		toRet.add("org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ObtainProbesOfType");
		
		
		toRet.add("org.objectweb.asm.MethodVisitor");
	}
	
	public RequiredList() {
    this(new ClassesDelegate());
  }
  
  public RequiredList(I_Classes classes) {
    super(getSharedClassWhitelist(), classes);
  }
  
  /**
   * Note if you were testing java, org.w3c, org.jacoco, org.objectweb.
   * or org.xml you would need your own implementation
   * to allow those classes to be instrumented.  This
   * was done for general ease of use.
   * @return
   */
  public Set<String> getNonInstrumentedPackages() {
    Set<String> names = new HashSet<String>();
    names.add("java.");
    names.add("javax.");
    names.add("sun.");
    names.add("org.jacoco.");
    names.add("org.objectweb.");
    names.add("org.w3c.");
    names.add("org.xml.");
    return names;
  }
}
