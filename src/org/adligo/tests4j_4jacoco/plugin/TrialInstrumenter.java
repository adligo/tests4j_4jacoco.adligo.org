package org.adligo.tests4j_4jacoco.plugin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;
import org.adligo.tests4j.models.shared.trials.I_AbstractTrial;
import org.adligo.tests4j.run.discovery.ClassDiscovery;
import org.adligo.tests4j.run.discovery.TopPackageSet;
import org.adligo.tests4j.run.helpers.ThreadLogMessageBuilder;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.MemoryClassLoader;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_Instrumenter;

public class TrialInstrumenter {
	private static ConcurrentLinkedQueue<String> startedClasses = new ConcurrentLinkedQueue<String>();
	private I_Tests4J_Log tests4jLogger;
	private boolean writeOutInstrumentedClassFiles = false;
	private MemoryClassLoader cachedClassLoader;
	private MemoryClassLoader instrumentedClassLoader;
	private I_Instrumenter instrumenter;
	
	@SuppressWarnings("unchecked")
	public Class<? extends I_AbstractTrial> instrument(Class<? extends I_AbstractTrial> trial) {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		classes.add(trial);
		
		TopPackageSet packages = TopPackageSet.getPackagesForInstrumentation(classes);
		List<Class<?>> newClasses =  loadClasses(packages, classes);
		for (Class<?> c: newClasses) {
			if (c != null) {
				String name = c.getName();
				if (trial.getName().equals(name)) {
					return (Class<? extends I_AbstractTrial>) c;
				}
			}
		}
		return null;
	}
	
	public List<Class<?>> instrumentClassesAny(List<Class<?>> classes) {
		TopPackageSet packages = TopPackageSet.getPackagesForInstrumentation(classes);
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
		
		if (instrumentedClassLoader.getClass(clazzName) == null) {
			if (!SharedClassList.WHITELIST.contains(clazzName)) {
				return instrumentClass(clazzName);
			} 
		}
		return instrumentedClassLoader.getClass(clazzName);
	}
	private Class<?> instrumentClass(String clazzName) throws IOException,
			ClassNotFoundException {
		
		if (!canInstrument(clazzName)) {
			return null;
		}
		//a quick hacky optimiztion
		if (startedClasses.contains(clazzName)) {
			return null;
		}
		startedClasses.add(clazzName);
		
		if (instrumentedClassLoader.hasDefinition(clazzName)) {
			return instrumentedClassLoader.getClass(clazzName);
		}
		
		try {
			
			//instrument parent classes
			Class<?> c = Class.forName(clazzName);
			Class<?> s = c.getSuperclass();
			if (s != null) {
				if (!Object.class.equals(s)) {
					if (!instrumentedClassLoader.hasDefinition(s.getName())) {
						instrumentClass(s.getName());
					}
				}
			}
		} catch (ClassNotFoundException e) {
			//do nothing, this is the break in the loop
		}
		//instrument inner classes
		try {
			int i = 1;
			while (true) {
				Class<?> c = Class.forName(clazzName + "$" + i);
				if (!instrumentedClassLoader.hasDefinition(c.getName())) {
					instrumentClass(c.getName());
				}
				i++;
			}
		} catch (ClassNotFoundException e) {
			//do nothing, this is the break in the loop
		}
		
		InputStream classInputStream = null;
		if (cachedClassLoader.hasDefinition(clazzName)) {
			classInputStream = cachedClassLoader.getResourceAsStream(clazzName);
		} else {
			final String resource = '/' + clazzName.replace('.', '/') + ".class";
			InputStream in = this.getClass().getResourceAsStream(resource);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte [] bytes = new byte[1];
			while(in.read(bytes) != -1) {
				baos.write(bytes);
			}
			bytes = baos.toByteArray();
			cachedClassLoader.addDefinition(clazzName, bytes);
			classInputStream = new ByteArrayInputStream(bytes);
		}
		
		final byte[] instrumented = instrumenter.instrument(
				classInputStream, clazzName);
		if (writeOutInstrumentedClassFiles) {
			FileOutputStream fos = new FileOutputStream(new File("./" + clazzName + ".class"));
			fos.write(instrumented);
			fos.close();
		}

		if (tests4jLogger.isLogEnabled(TrialInstrumenter.class)) {
			tests4jLogger.log(ThreadLogMessageBuilder.getThreadForLog() +  " instrumenting class " + clazzName);
		}
		synchronized (instrumentedClassLoader) {
			Class<?> toRet = instrumentedClassLoader.getClass(clazzName);
			if (toRet == null) {
				instrumentedClassLoader.addDefinition(clazzName, instrumented);
				toRet =  instrumentedClassLoader.loadClass(clazzName);
			}
			return toRet;
			
		}
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

	public I_Tests4J_Log getTests4jLogger() {
		return tests4jLogger;
	}

	public boolean isWriteOutInstrumentedClassFiles() {
		return writeOutInstrumentedClassFiles;
	}

	public void setTests4jLogger(I_Tests4J_Log log) {
		this.tests4jLogger = log;
	}

	public void setWriteOutInstrumentedClassFiles(
			boolean writeOutInstrumentedClassFiles) {
		this.writeOutInstrumentedClassFiles = writeOutInstrumentedClassFiles;
	}

	protected MemoryClassLoader getCachedClassLoader() {
		return cachedClassLoader;
	}

	protected MemoryClassLoader getInstrumentedClassLoader() {
		return instrumentedClassLoader;
	}

	protected void setCachedClassLoader(MemoryClassLoader cachedClassLoader) {
		this.cachedClassLoader = cachedClassLoader;
	}

	protected void setInstrumentedClassLoader(
			MemoryClassLoader instrumentedClassLoader) {
		this.instrumentedClassLoader = instrumentedClassLoader;
	}

	protected I_Instrumenter getInstrumenter() {
		return instrumenter;
	}

	protected void setInstrumenter(I_Instrumenter instrumenter) {
		this.instrumenter = instrumenter;
	}

	
	private boolean canInstrument(String clazzName) {
		if (clazzName.indexOf("java") == 0) {
			return false;
		}
		if (clazzName.indexOf("org.objectweb.asm") == 0) {
			return false;
		}
		if (clazzName.indexOf("org.adligo.tests4j_4jacoco.plugin.analysis") == 0) {
			return false;
		}
		return true;
	}
}
