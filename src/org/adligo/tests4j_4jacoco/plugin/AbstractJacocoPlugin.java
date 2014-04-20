package org.adligo.tests4j_4jacoco.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.adligo.tests4j.models.shared.AbstractTrial;
import org.adligo.tests4j.models.shared.PackageScope;
import org.adligo.tests4j.models.shared.SourceFileScope;
import org.adligo.tests4j.models.shared.system.I_CoveragePlugin;
import org.adligo.tests4j.models.shared.system.I_CoverageRecorder;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Logger;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Params;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.ClassDiscovery;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.ClassNameToInputStream;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.MemoryClassLoader;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.PackageSet;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_Instrumenter;

public abstract class AbstractJacocoPlugin implements I_CoveragePlugin {
	protected JacocoMemory memory;
	private I_Tests4J_Logger log;
	
	@Override
	public List<Class<? extends AbstractTrial>> instrumentClasses(I_Tests4J_Params pParams) {
		log = pParams.getLog();
		PackageSet packages = getPackages(pParams);
		memory.setPackages(packages);
		return loadClasses(packages, pParams);
	}
	
	private PackageSet getPackages(I_Tests4J_Params pParams) {
		PackageSet packages = new PackageSet();
		//ok find what may show up in the coverage
		List<Class<? extends AbstractTrial>> trials = pParams.getTrials();
		for (Class<? extends AbstractTrial> trial: trials) {
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
	
	public List<Class<? extends AbstractTrial>>  loadClasses(PackageSet packages, I_Tests4J_Params pParams) {
		List<Class<? extends AbstractTrial>> newTrials = new ArrayList<Class<? extends AbstractTrial>>();
		
		Set<String> testedPackages = packages.get();
		try {
			//load the classes to be tesed in the memory class loader
			for (String pkg: testedPackages) {
				loadTestedClasses(pkg);
			}
	
			List<Class<? extends AbstractTrial>> trials = pParams.getTrials();
			//load the trials in the memory class loader
			for (Class<? extends AbstractTrial> trialClazz: trials) {
				String trialClassName = trialClazz.getName();
				
				@SuppressWarnings("unchecked")
				Class<? extends AbstractTrial> customClassLoadedClazz =
						(Class<? extends AbstractTrial>)
						loadClass(trialClassName);
				newTrials.add(customClassLoadedClazz);
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
		return newTrials;
	}
	
	private Class<?> loadClass(String clazzName) throws IOException,
			ClassNotFoundException {
		
		MemoryClassLoader memoryClassLoader = memory.getMemoryClassLoader();
		if (memoryClassLoader.getClass(clazzName) == null) {
			if (!clazzName.contains("org.adligo.tests4j")) {
				return loadClassInternal(clazzName);
			} else {
				Class<?> clz = Class.forName(clazzName);
				if (clz.isAnnotation() || clz.isInterface() || clz.isEnum()) {
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
		MemoryClassLoader memoryClassLoader = memory.getMemoryClassLoader();
		I_Instrumenter instr = memory.getInstrumenter();
		
		final byte[] instrumented = instr.instrument(
				ClassNameToInputStream.getTargetClass(clazzName), clazzName);
		FileOutputStream fos = new FileOutputStream(new File("./" + clazzName + ".class"));
		fos.write(instrumented);
		fos.close();
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
	public I_CoverageRecorder createRecorder(String scope) {
		JacocoRecorder rec = new JacocoRecorder(scope, memory, log);
		if (I_CoverageRecorder.TRIAL_RUN.equals(scope)) {
			rec.setRoot(true);
		}
		return rec;
	}

}
