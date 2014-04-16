package org.adligo.jacoco4jtests.wrapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.adligo.jacoco4jtests.run.setup.ClassDiscovery;
import org.adligo.jacoco4jtests.run.setup.MemoryClassLoader;
import org.adligo.jacoco4jtests.run.setup.PackageSet;
import org.adligo.jtests.models.shared.I_AbstractTrial;
import org.adligo.jtests.models.shared.coverage.I_PackageCoverage;
import org.adligo.jtests.models.shared.system.I_CoverageRecorder;
import org.adligo.jtests.models.shared.system.JTestParameters;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;

public class JacocoCoverageRecorder implements I_CoverageRecorder {
	private boolean log = true;
	// For instrumentation and runtime we need a IRuntime instance
	// to collect execution data:
	final IRuntime runtime = new LoggerRuntime();

	// The Instrumenter creates a modified version of our test target class
	// that contains additional probes for execution data recording:
	final Instrumenter instr = new Instrumenter(runtime);
	final RuntimeData data = new RuntimeData();
	
	IRuntime trialRuntime;
	Instrumenter trialInstr;
	RuntimeData trialData;
	final MemoryClassLoader memoryClassLoader = new MemoryClassLoader();
	
	public JacocoCoverageRecorder() {
		try {
			runtime.startup(data);
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
	@Override
	public void startRecording()  {
		
	}

	@Override
	public I_PackageCoverage getCoverage(String packageName) {
		try {
			final ExecutionDataStore executionData = new ExecutionDataStore();
			final SessionInfoStore sessionInfos = new SessionInfoStore();
			trialData.collect(executionData, sessionInfos, false);
			trialRuntime.shutdown();
	
			final CoverageBuilder coverageBuilder = new CoverageBuilder();
			final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
			String targetName = "org.adligo.jtests.models.shared.AbstractTrial";
			analyzer.analyzeClass(getTargetClass(targetName), targetName);
	
			// Let's dump some metrics and line coverage information:
			for (final IClassCoverage cc : coverageBuilder.getClasses()) {
				System.out.printf("Coverage of class %s%n", cc.getName());
	
				printCounter("instructions", cc.getInstructionCounter());
				printCounter("branches", cc.getBranchCounter());
				printCounter("lines", cc.getLineCounter());
				printCounter("methods", cc.getMethodCounter());
				printCounter("complexity", cc.getComplexityCounter());
	
				for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++) {
					System.out.printf("Line %s: %s%n", Integer.valueOf(i), getColor(cc
							.getLine(i).getStatus()));
				}
			}
		} catch (Exception x) {
			x.printStackTrace();
		}
		return null;
	}
	
	public IRuntime getRuntime() {
		return runtime;
	}
	public Instrumenter getInstrumenter() {
		return instr;
	}
	@Override
	public List<I_PackageCoverage> getCoverage() {
		
		
		final ExecutionDataStore executionData = new ExecutionDataStore();
		final SessionInfoStore sessionInfos = new SessionInfoStore();
		data.collect(executionData, sessionInfos, false);
		runtime.shutdown();
		try {
			
			final CoverageBuilder coverageBuilder = new CoverageBuilder();
			final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
			String targetName = "org.adligo.jtests.models.shared.AbstractTrial";
			analyzer.analyzeClass(getTargetClass(targetName), targetName);
	
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
					System.out.println("Line " + i + " instructions " + 
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
						
		} catch (Exception x) {
			x.printStackTrace();
		}
		List<I_PackageCoverage> toRet = new ArrayList<I_PackageCoverage>();
		return toRet;
	}
	
	public RuntimeData getData() {
		return data;
	}
	
	public JTestParameters loadClasses(PackageSet packages, JTestParameters pParams) {
		JTestParameters newParams = new JTestParameters();
		newParams.setCheckMins(pParams.isCheckMins());
		newParams.setFailFast(pParams.isFailFast());
		newParams.setMinAsserts(pParams.getMinAsserts());
		newParams.setMinTests(pParams.getMinTests());
		newParams.setMinUniqueAssertions(pParams.getMinUniqueAssertions());
		
		Set<String> testedPackages = packages.get();
		try {
			//load the classes to be tesed in the memory class loader
			for (String pkg: testedPackages) {
				loadTestedClasses(instr, memoryClassLoader, pkg);
			}
	
			List<Class<? extends I_AbstractTrial>> trials = pParams.getTrials();
			List<Class<? extends I_AbstractTrial>> newTrials = new 
					ArrayList<Class<? extends I_AbstractTrial>>();
			//load the trials in the memory class loader
			for (Class<? extends I_AbstractTrial> trialClazz: trials) {
				String trialClassName = trialClazz.getName();
				
				@SuppressWarnings("unchecked")
				Class<? extends I_AbstractTrial> customClassLoadedClazz =
						(Class<? extends I_AbstractTrial>)
						loadClass(trialClassName);
				newTrials.add(customClassLoadedClazz);
			}
			newParams.setTrials(newTrials);
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
		return newParams;
	}
	
	private Class<?> loadClass(String clazzName) throws IOException,
			ClassNotFoundException {
		
		
		if (memoryClassLoader.getClass(clazzName) == null) {
			if (!clazzName.contains("org.adligo.jtests")) {
				return loadClassInternal(clazzName);
			} else {
				Class<?> clz = Class.forName(clazzName);
				if (clz.isAnnotation() || clz.isInterface() || clz.isEnum()
						 || clazzName.contains("org.adligo.jtests.models.shared.asserts")
						 || clazzName.contains("org.adligo.jtests.models.shared.results")) {
					//skip use the parent classloader for def
				} else {
					return loadClassInternal(clazzName);
				}
			}
			
		}
		return memoryClassLoader.getClass(clazzName);
	}
	private Class<?> loadClassInternal(String clazzName) throws IOException,
			ClassNotFoundException {
		if (log) {
			System.out.println("loading class " + clazzName);
		}
		final byte[] instrumented = instr.instrument(
				getTargetClass(clazzName), clazzName);
		memoryClassLoader.addDefinition(clazzName, instrumented);
		return memoryClassLoader.loadClass(clazzName);
	}
	
	private void loadTestedClasses(Instrumenter instr,
			final MemoryClassLoader memoryClassLoader, String pkg)
			throws ClassNotFoundException, IOException {
		ClassDiscovery cd = new ClassDiscovery(pkg);
		loadTestedClasses(instr, memoryClassLoader, cd);
			
	}

	private void loadTestedClasses(Instrumenter instr,
			final MemoryClassLoader memoryClassLoader, ClassDiscovery cd)
			throws IOException, ClassNotFoundException {
		List<String> classNames = cd.getClassNames();
		for (String clazz: classNames) {
			loadClass(clazz);
		}
		List<ClassDiscovery> subCds = cd.getSubPackages();
		for (ClassDiscovery subCd: subCds) {
			loadTestedClasses(instr, memoryClassLoader, subCd);
		}
	}

	private InputStream getTargetClass(final String name) {
		final String resource = '/' + name.replace('.', '/') + ".class";
		return getClass().getResourceAsStream(resource);
	}
	
	private void printCounter(final String unit, final ICounter counter) {
		final Integer missed = Integer.valueOf(counter.getMissedCount());
		final Integer total = Integer.valueOf(counter.getTotalCount());
		System.out.printf("%s of %s %s missed%n", missed, total, unit);
	}

	private String getColor(final int status) {
		switch (status) {
		case ICounter.NOT_COVERED:
			return "red";
		case ICounter.PARTLY_COVERED:
			return "yellow";
		case ICounter.FULLY_COVERED:
			return "green";
		}
		return "";
	}
	@Override
	public void startTrialRecording() {
		IRuntime trialRuntime = new LoggerRuntime();

		// The Instrumenter creates a modified version of our test target class
		// that contains additional probes for execution data recording:
		Instrumenter trialInstr = new Instrumenter(runtime);
		RuntimeData trialData = new RuntimeData();
		try {
			trialRuntime.startup(trialData);
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
}
