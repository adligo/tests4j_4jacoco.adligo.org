package org.adligo.tests4j_4jacoco.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.adligo.tests4j.models.shared.system.I_CoveragePlugin;
import org.adligo.tests4j.models.shared.system.I_CoverageRecorder;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Reporter;
import org.adligo.tests4j.models.shared.trials.I_AbstractTrial;
import org.adligo.tests4j.models.shared.trials.I_Trial;
import org.adligo.tests4j.run.discovery.ClassDiscovery;
import org.adligo.tests4j.run.discovery.TopPackageSet;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.ClassNameToInputStream;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.MemoryClassLoader;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_Instrumenter;

public abstract class AbstractPlugin implements I_CoveragePlugin {
	protected Tests4J_4JacocoMemory memory;
	private I_Tests4J_Reporter reporter;
	private boolean writeOutInstrumentedClassFiles = false;
	private AtomicBoolean firstRecorder = new AtomicBoolean(false);

	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Class<? extends I_AbstractTrial>> instrumentClasses(List<Class<? extends I_AbstractTrial>> trials) {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		classes.addAll(trials);
		
		TopPackageSet packages = TopPackageSet.getPackagesForInstrumentation(classes);
		memory.setPackages(packages);
		List<Class<?>> newClasses =  loadClasses(packages, classes);
		 List<Class<? extends I_AbstractTrial>> toRet = new ArrayList<Class<? extends I_AbstractTrial>>();
		 for (Class<?> clazz: newClasses) {
			 toRet.add((Class<? extends I_Trial>) clazz);
		 }
		 return toRet;
	}
	
	public List<Class<?>> instrumentClassesAny(List<Class<?>> classes) {
		TopPackageSet packages = TopPackageSet.getPackagesForInstrumentation(classes);
		memory.setPackages(packages);
		return loadClasses(packages, classes);
	}
	
	
	
	private List<Class<?>>  loadClasses(TopPackageSet packages, List<Class<?>> classes) {
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
			if (!SharedClassList.WHITELIST.contains(clazzName)) {
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
		//instrument inner classes
		try {
			int i = 1;
			while (true) {
				Class<?> c = Class.forName(clazzName + "$" + i);
				if (!memoryClassLoader.hasDefinition(c.getName())) {
					instrumentClass(c.getName());
				}
				i++;
			}
		} catch (ClassNotFoundException e) {
			//do nothing, this is the break in the loop
		}
		
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
