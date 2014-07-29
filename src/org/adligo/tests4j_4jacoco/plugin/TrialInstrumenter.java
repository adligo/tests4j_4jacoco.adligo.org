package org.adligo.tests4j_4jacoco.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.adligo.tests4j.models.shared.common.ClassMethods;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;
import org.adligo.tests4j.models.shared.trials.I_AbstractTrial;
import org.adligo.tests4j.run.discovery.PackageDiscovery;
import org.adligo.tests4j.run.discovery.TopPackageSet;
import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;
import org.adligo.tests4j.run.helpers.ThreadLogMessageBuilder;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_Instrumenter;

public class TrialInstrumenter {
	private static ConcurrentLinkedQueue<String> startedClasses = new ConcurrentLinkedQueue<String>();
	private I_Tests4J_Log tests4jLogger;
	private boolean writeOutInstrumentedClassFiles = false;
	private I_CachedClassBytesClassLoader cachedClassLoader;
	private I_CachedClassBytesClassLoader instrumentedClassLoader;
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
		
		if (instrumentedClassLoader.getCachedClass(clazzName) == null) {
			if (!SharedClassList.WHITELIST.contains(clazzName)) {
				return instrumentClass(clazzName);
			} 
		}
		return instrumentedClassLoader.getCachedClass(clazzName);
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
		
		if (instrumentedClassLoader.hasCache(clazzName)) {
			return instrumentedClassLoader.getCachedClass(clazzName);
		}
		
		discoverAndLoadParentClasses(clazzName);
		discoverAndInstrumentFields(clazzName);
		discoverAndInstrumentInnerClasses(clazzName);
		
		InputStream classInputStream = null;
		if (cachedClassLoader.hasCache(clazzName)) {
			classInputStream = cachedClassLoader.getCachedBytesStream(clazzName);
		} else {
			final String resource = ClassMethods.toResource(clazzName);
			InputStream in = this.getClass().getResourceAsStream(resource);
			cachedClassLoader.addCache(in, clazzName);
			classInputStream = cachedClassLoader.getCachedBytesStream(clazzName);
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
			Class<?> toRet = instrumentedClassLoader.getCachedClass(clazzName);
			if (toRet == null) {
				toRet = instrumentedClassLoader.addCache(clazzName, instrumented);
			}
			return toRet;
			
		}
	}

	private void discoverAndLoadParentClasses(String clazzName)
			throws IOException {
		try {
			
			//instrument parent classes
			Class<?> c = Class.forName(clazzName);
			Class<?> s = c.getSuperclass();
			if (s != null) {
				if (!Object.class.equals(s)) {
					if (!instrumentedClassLoader.hasCache(s.getName())) {
						instrumentClass(s.getName());
					}
				}
			}
		} catch (ClassNotFoundException e) {
			//do nothing, this is the break in the loop
			//this is because we are discovering class
		}
	}

	private void discoverAndInstrumentInnerClasses(String clazzName)
			throws IOException {
		//instrument inner classes
		try {
			int i = 1;
			while (true) {
				Class<?> c = Class.forName(clazzName + "$" + i);
				if (!instrumentedClassLoader.hasCache(c.getName())) {
					instrumentClass(c.getName());
				}
				i++;
			}
		} catch (ClassNotFoundException e) {
			//do nothing, this is the break in the loop
			//because we are discovering classes
		}
	}

	private void discoverAndInstrumentFields(String clazzName)
			throws IOException {
	
		Class<?> c;
		try {
			c = Class.forName(clazzName);
			Field[] fields = c.getFields();
			for (int j = 0; j < fields.length; j++) {
				Field f = fields[j];
				Class<?> fieldClass = f.getClass();
				String fieldClassName = fieldClass.getName();
				if (!instrumentedClassLoader.hasCache(fieldClassName)) {
					instrumentClass(fieldClassName);
				}
			}
		} catch (ClassNotFoundException e) {
			tests4jLogger.onException(e);
		}
	}
	
	
	private void loadTestedClasses(String pkg)
			throws ClassNotFoundException, IOException {
		PackageDiscovery cd = new PackageDiscovery(pkg);
		loadTestedClasses(cd);
			
	}

	private void loadTestedClasses(PackageDiscovery cd)
			throws IOException, ClassNotFoundException {
		List<String> classNames = cd.getClassNames();
		for (String clazz: classNames) {
			loadClass(clazz);
		}
		List<PackageDiscovery> subCds = cd.getSubPackages();
		for (PackageDiscovery subCd: subCds) {
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

	protected I_CachedClassBytesClassLoader getCachedClassLoader() {
		return cachedClassLoader;
	}

	protected I_CachedClassBytesClassLoader getInstrumentedClassLoader() {
		return instrumentedClassLoader;
	}

	protected void setCachedClassLoader(I_CachedClassBytesClassLoader cachedClassLoader) {
		this.cachedClassLoader = cachedClassLoader;
	}

	protected void setInstrumentedClassLoader(
			I_CachedClassBytesClassLoader instrumentedClassLoader) {
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
