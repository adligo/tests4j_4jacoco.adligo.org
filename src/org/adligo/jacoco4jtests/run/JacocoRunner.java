package org.adligo.jacoco4jtests.run;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.adligo.jacoco4jtests.run.setup.ClassDiscovery;
import org.adligo.jacoco4jtests.run.setup.MemoryClassLoader;
import org.adligo.jacoco4jtests.run.setup.PackageSet;
import org.adligo.jacoco4jtests.wrapper.JacocoCoverageRecorder;
import org.adligo.jtests.models.shared.ClassScope;
import org.adligo.jtests.models.shared.I_AbstractTrial;
import org.adligo.jtests.models.shared.PackageScope;
import org.adligo.jtests.models.shared.common.LineSeperator;
import org.adligo.jtests.models.shared.results.I_TrialResult;
import org.adligo.jtests.models.shared.results.I_TrialRunResult;
import org.adligo.jtests.models.shared.system.I_TestRunListener;
import org.adligo.jtests.models.shared.system.RunParameters;
import org.adligo.jtests.run.I_JTests;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;

import com.sun.org.apache.bcel.internal.generic.JsrInstruction;

public class JacocoRunner implements I_TestRunListener, I_JTests {

	private boolean log = true;
	
	
	
	public JacocoRunner() {
		
	}
	
	public void run(RunParameters pParams, I_TestRunListener pProcessor) {
		runInternal(pParams, pProcessor);
	}
	
	public void run(RunParameters pParams) {
		runInternal(pParams, this);
	}
	
	private void runInternal(RunParameters pParams, I_TestRunListener pProcessor) {
		RunParameters newParams = new RunParameters();
		newParams.setPackageScope(pParams.isPackageScope());
		newParams.setFailFast(pParams.isFailFast());
		newParams.setSilent(pParams.isSilent());
		
		JacocoCoverageRecorder recorder = new JacocoCoverageRecorder();
		newParams.setCoverageRecorder(recorder);
		Instrumenter instr = recorder.getInstrumenter();
		PackageSet packages = getPackages(pParams);
		
		
		// In this tutorial we use a special class loader to directly load the
		// instrumented class definition from a byte[] instances.
		final MemoryClassLoader memoryClassLoader = new MemoryClassLoader();
				
		Set<String> testedPackages = packages.get();
		try {
			//load the classes to be tesed in the memory class loader
			for (String pkg: testedPackages) {
				loadTestedClasses(instr, memoryClassLoader, pkg);
			}

			List<Class<? extends I_AbstractTrial>> trials = pParams.getTrials();
			List<Class<? extends I_AbstractTrial>> newTrials = new ArrayList<Class<? extends I_AbstractTrial>>();
			//load the trials in the memory class loader
			for (Class<? extends I_AbstractTrial> trialClazz: trials) {
				String trialClassName = trialClazz.getName();
				if (log) {
					System.out.println("loading class " + trialClassName);
				}
				final byte[] instrumented = instr.instrument(
						getTargetClass(trialClassName), trialClassName);
				memoryClassLoader.addDefinition(trialClassName, instrumented);
				Class<? extends I_AbstractTrial> newTrial = 
						 (Class<? extends I_AbstractTrial>) 
						 memoryClassLoader.loadClass(trialClassName);
				newTrials.add(newTrial);
			}
			newParams.setTrials(newTrials);
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
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
			if (log) {
				System.out.println("loading class " + clazz);
			}
			final byte[] instrumented = instr.instrument(
								getTargetClass(clazz), clazz);
			memoryClassLoader.addDefinition(clazz, instrumented);
			if (!memoryClassLoader.hasDefinition(clazz)) {
				memoryClassLoader.loadClass(clazz);
			}
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
		
		
	}

	@Override
	public void onRunCompleted(I_TrialRunResult result) {
		// TODO Auto-generated method stub
		
	}
 }
