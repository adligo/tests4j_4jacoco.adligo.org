package org.adligo.tests4j_4jacoco.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.adligo.tests4j.models.shared.AdditionalInstrumentation;
import org.adligo.tests4j.models.shared.I_AbstractTrial;
import org.adligo.tests4j.models.shared.I_Trial;
import org.adligo.tests4j.models.shared.PackageScope;
import org.adligo.tests4j.models.shared.SourceFileScope;
import org.adligo.tests4j.models.shared.system.I_CoveragePlugin;
import org.adligo.tests4j.models.shared.system.I_CoverageRecorder;
import org.adligo.tests4j.models.shared.system.report.I_Tests4J_Reporter;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.ClassDiscovery;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.ClassNameToInputStream;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.MemoryClassLoader;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.PackageSet;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_Instrumenter;

public abstract class AbstractPlugin implements I_CoveragePlugin {
	protected Tests4J_4JacocoMemory memory;
	private I_Tests4J_Reporter reporter;
	private boolean writeOutInstrumentedClassFiles = false;
	private AtomicBoolean firstRecorder = new AtomicBoolean(false);
	private static Set<String> TESTS4J_SHARED_CLASS_WHITELIST = getSharedClassWhitelist();
	
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
		
		toRet.add("org.adligo.tests4j.models.shared.metadata.I_TestMetadata");
		toRet.add("org.adligo.tests4j.models.shared.metadata.I_TrialMetadata");
		toRet.add("org.adligo.tests4j.models.shared.metadata.I_TrialRunMetadata");
		
		toRet.add("org.adligo.tests4j.models.shared.results.I_ApiTrialResult");
		toRet.add("org.adligo.tests4j.models.shared.results.I_Duration");
		toRet.add("org.adligo.tests4j.models.shared.results.I_SourceFileTrialResult");
		toRet.add("org.adligo.tests4j.models.shared.results.I_TestFailure");
		toRet.add("org.adligo.tests4j.models.shared.results.I_TestResult");
		toRet.add("org.adligo.tests4j.models.shared.results.I_TrialFailure");
		toRet.add("org.adligo.tests4j.models.shared.results.I_TrialResult");
		toRet.add("org.adligo.tests4j.models.shared.results.I_TrialRunResult");
		toRet.add("org.adligo.tests4j.models.shared.results.I_UseCase");
		toRet.add("org.adligo.tests4j.models.shared.results.I_UseCaseTrialResult");
		
		
		toRet.add("org.adligo.tests4j.models.shared.system.I_AssertListener");
		toRet.add("org.adligo.tests4j.models.shared.system.I_CoveragePlugin");
		toRet.add("org.adligo.tests4j.models.shared.system.I_Tests4J_Params");
		
		toRet.add("org.adligo.tests4j_4jacoco.plugin.data.common.I_Probes");
		
		return Collections.unmodifiableSet(toRet);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Class<? extends I_AbstractTrial>> instrumentClasses(List<Class<? extends I_AbstractTrial>> trials) {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		classes.addAll(trials);
		PackageSet packages = getPackages(classes);
		memory.setPackages(packages);
		List<Class<?>> newClasses =  loadClasses(packages, classes);
		 List<Class<? extends I_AbstractTrial>> toRet = new ArrayList<Class<? extends I_AbstractTrial>>();
		 for (Class<?> clazz: newClasses) {
			 toRet.add((Class<? extends I_Trial>) clazz);
		 }
		 return toRet;
	}
	
	public List<Class<?>> instrumentClassesAny(List<Class<?>> classes) {
		PackageSet packages = getPackages(classes);
		memory.setPackages(packages);
		return loadClasses(packages, classes);
	}
	
	private PackageSet getPackages(List<Class<?>> classes) {
		PackageSet packages = new PackageSet();
		for (Class<?> clazz: classes) {
			if (I_Trial.class.isAssignableFrom(clazz)) {
				PackageScope ps = clazz.getAnnotation(PackageScope.class);
				if (ps != null) {
					String pkg = ps.packageName();
					packages.add(pkg);
				} else {
					SourceFileScope cs = clazz.getAnnotation(SourceFileScope.class);
					if (cs != null) {
						String pkg = cs.sourceClass().getPackage().getName();
						packages.add(pkg);
					}
				}
				AdditionalInstrumentation ai = clazz.getAnnotation(AdditionalInstrumentation.class);
				if (ai != null) {
					String pkgs = ai.javaPackages();
					StringTokenizer tokens = new StringTokenizer(pkgs, ",");
					while (tokens.hasMoreElements()) {
						packages.add(tokens.nextToken().trim());
					}
				}
			} else {
				packages.add(clazz.getPackage().getName());
			}
		}
		return packages;
	}
	
	private List<Class<?>>  loadClasses(PackageSet packages, List<Class<?>> classes) {
		List<Class<?>> newClasses = 
				new ArrayList<Class<?>>();
		
		Set<String> testedPackages = packages.get();
		try {
			//load the classes to be tesed in the memory class loader
			for (String pkg: testedPackages) {
				loadTestedClasses(pkg);
			}
	
			//load the trials in the memory class loader
			for (Class<?> clazz: classes) {
				String className = clazz.getName();
				
				@SuppressWarnings("unchecked")
				Class<?> customClassLoadedClazz =
						loadClass(className);
				newClasses.add(customClassLoadedClazz);
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
		return newClasses;
	}
	
	/**
	 * load a class into a memoryClassLoader
	 *    keep some classes out of the memoryClassLoader
	 *    because they need to work in multiple class loaders
	 * @param clazzName
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private Class<?> loadClass(String clazzName) throws IOException,
			ClassNotFoundException {
		
		MemoryClassLoader memoryClassLoader = memory.getMemoryClassLoader();
		if (memoryClassLoader.getClass(clazzName) == null) {
			if (!TESTS4J_SHARED_CLASS_WHITELIST.contains(clazzName)) {
				return instrumentClass(clazzName);
			} 
		}
		return memoryClassLoader.getClass(clazzName);
	}
	private Class<?> instrumentClass(String clazzName) throws IOException,
			ClassNotFoundException {
		if (reporter != null) {
			if (reporter.isLogEnabled(AbstractPlugin.class)) {
				reporter.log("instrumenting class " + clazzName);
			}
		}
		MemoryClassLoader memoryClassLoader = memory.getMemoryClassLoader();
		I_Instrumenter instr = memory.getInstrumenter();
		
		final byte[] instrumented = instr.instrument(
				ClassNameToInputStream.getTargetClass(clazzName), clazzName);
		if (writeOutInstrumentedClassFiles) {
			FileOutputStream fos = new FileOutputStream(new File("./" + clazzName + ".class"));
			fos.write(instrumented);
			fos.close();
		}
		memoryClassLoader.addDefinition(clazzName, instrumented);
		return memoryClassLoader.loadClass(clazzName);
	}

	
	private void loadTestedClasses(String pkg)
			throws ClassNotFoundException, IOException {
		ClassDiscovery cd = new ClassDiscovery(pkg);
		loadTestedClasses(cd);
			
	}

	private void loadTestedClasses(ClassDiscovery cd)
			throws IOException, ClassNotFoundException {
		List<String> classNames = cd.getClassNames();
		for (String clazz: classNames) {
			loadClass(clazz);
		}
		List<ClassDiscovery> subCds = cd.getSubPackages();
		for (ClassDiscovery subCd: subCds) {
			loadTestedClasses(subCd);
		}
	}
	
	@Override
	public synchronized I_CoverageRecorder createRecorder(String scope) {
		Recorder rec = new Recorder(scope, memory, reporter);
		if (!firstRecorder.get()) {
			firstRecorder.set(true);
			rec.setRoot(true);
		}
		return rec;
	}

	public Tests4J_4JacocoMemory getMemory() {
		return memory;
	}

	public I_Tests4J_Reporter getReporter() {
		return reporter;
	}

	public boolean isWriteOutInstrumentedClassFiles() {
		return writeOutInstrumentedClassFiles;
	}

	public void setMemory(Tests4J_4JacocoMemory memory) {
		this.memory = memory;
	}

	public void setReporter(I_Tests4J_Reporter log) {
		this.reporter = log;
	}

	public void setWriteOutInstrumentedClassFiles(
			boolean writeOutInstrumentedClassFiles) {
		this.writeOutInstrumentedClassFiles = writeOutInstrumentedClassFiles;
	}

}
