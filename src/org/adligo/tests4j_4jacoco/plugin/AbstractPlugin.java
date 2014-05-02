package org.adligo.tests4j_4jacoco.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.adligo.tests4j.models.shared.AbstractTrial;
import org.adligo.tests4j.models.shared.I_AbstractTrial;
import org.adligo.tests4j.models.shared.PackageScope;
import org.adligo.tests4j.models.shared.SourceFileScope;
import org.adligo.tests4j.models.shared.system.I_CoveragePlugin;
import org.adligo.tests4j.models.shared.system.I_CoverageRecorder;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Params;
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
	
	@Override
	public List<Class<? extends I_AbstractTrial>> instrumentClasses(I_Tests4J_Params pParams) {
		reporter = pParams.getReporter();
		PackageSet packages = getPackages(pParams);
		memory.setPackages(packages);
		return loadClasses(packages, pParams);
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
	
	public List<Class<? extends I_AbstractTrial>>  loadClasses(PackageSet packages, I_Tests4J_Params pParams) {
		List<Class<? extends I_AbstractTrial>> newTrials = 
				new ArrayList<Class<? extends I_AbstractTrial>>();
		
		Set<String> testedPackages = packages.get();
		try {
			//load the classes to be tesed in the memory class loader
			for (String pkg: testedPackages) {
				loadTestedClasses(pkg);
			}
	
			List<Class<? extends I_AbstractTrial>> trials = pParams.getTrials();
			//load the trials in the memory class loader
			for (Class<? extends I_AbstractTrial> trialClazz: trials) {
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
		if (reporter.isLogEnabled(AbstractPlugin.class)) {
			reporter.log("loading class " + clazzName);
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
