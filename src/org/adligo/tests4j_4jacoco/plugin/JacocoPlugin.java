package org.adligo.tests4j_4jacoco.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.adligo.tests4j.models.shared.I_AbstractTrial;
import org.adligo.tests4j.models.shared.PackageScope;
import org.adligo.tests4j.models.shared.SourceFileScope;
import org.adligo.tests4j.models.shared.system.I_CoveragePlugin;
import org.adligo.tests4j.models.shared.system.I_CoverageRecorder;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Logger;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Params;
import org.adligo.tests4j.models.shared.system.I_TrialList;
import org.adligo.tests4j.models.shared.system.Tests4J_Params;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;

public class JacocoPlugin implements I_CoveragePlugin {
	private final MemoryClassLoader memoryClassLoader = new MemoryClassLoader();
	// For instrumentation and runtime we need a IRuntime instance
	// to collect execution data:
	final IRuntime runtime = new LoggerRuntime();

	// The Instrumenter creates a modified version of our test target class
	// that contains additional probes for execution data recording:
	final Instrumenter instr = new Instrumenter(runtime);
	private I_Tests4J_Logger log;
	
	@Override
	public void instrumentClasses(I_Tests4J_Params pParams) {
		log = pParams.getLog();
		PackageSet packages = getPackages(pParams);
		
	}
	
	private PackageSet getPackages(I_Tests4J_Params pParams) {
		PackageSet packages = new PackageSet();
		//ok find what may show up in the coverage
		List<Class<? extends I_AbstractTrial>> trials = pParams.getTrials();
		for (Class<? extends I_AbstractTrial> trial: trials) {
			PackageScope ps = trial.getAnnotation(PackageScope.class);
			if (ps != null) {
				String pkg = ps.packageName();
				packages.add(pkg);
			} else {
				SourceFileScope cs = trial.getAnnotation(SourceFileScope.class);
				String pkg = cs.sourceClass().getPackage().getName();
				packages.add(pkg);
			}
		}
		return packages;
	}
	
	public Tests4J_Params loadClasses(PackageSet packages, Tests4J_Params pParams) {
		Tests4J_Params newParams = new Tests4J_Params();
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
		if (log.isEnabled()) {
			log.log("loading class " + clazzName);
		}
		final byte[] instrumented = instr.instrument(
				getTargetClass(clazzName), clazzName);
		memoryClassLoader.addDefinition(clazzName, instrumented);
		return memoryClassLoader.loadClass(clazzName);
	}

	private InputStream getTargetClass(final String name) {
		final String resource = '/' + name.replace('.', '/') + ".class";
		return getClass().getResourceAsStream(resource);
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
	
	@Override
	public I_CoverageRecorder createRecorder(String scope) {
		return new JacocoRecorder(scope, log);
	}

}
