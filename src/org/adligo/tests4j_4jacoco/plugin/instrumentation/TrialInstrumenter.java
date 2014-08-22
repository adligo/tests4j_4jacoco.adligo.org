package org.adligo.tests4j_4jacoco.plugin.instrumentation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.adligo.tests4j.models.shared.dependency.I_ClassDependenciesLocal;
import org.adligo.tests4j.models.shared.dependency.I_ClassFilter;
import org.adligo.tests4j.models.shared.system.I_Tests4J_CoverageTrialInstrumentation;
import org.adligo.tests4j.models.shared.system.Tests4J_CoverageTrialInstrumentation;
import org.adligo.tests4j.models.shared.trials.I_AbstractTrial;
import org.adligo.tests4j.models.shared.trials.PackageScope;
import org.adligo.tests4j.models.shared.trials.SourceFileScope;
import org.adligo.tests4j.run.discovery.PackageDiscovery;
import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;
import org.adligo.tests4j.run.helpers.ThreadLogMessageBuilder;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;
import org.adligo.tests4j_4jacoco.plugin.common.I_ClassInstrumenter;
import org.adligo.tests4j_4jacoco.plugin.common.I_OrderedClassDependencies;
import org.adligo.tests4j_4jacoco.plugin.common.I_OrderedClassDiscovery;
import org.adligo.tests4j_4jacoco.plugin.common.I_TrialInstrumenter;

public class TrialInstrumenter implements I_TrialInstrumenter {
	private I_Tests4J_Log log;
	private I_ClassFilter classFilter;
	/**
	 * contains the instrumented classes with injected byte code
	 */
	private I_CachedClassBytesClassLoader instrumentedClassLoader;
	/**
	 * contains the regular class bytes cache, so reloading isn't necessary
	 * between threads
	 */
	private I_CachedClassBytesClassLoader cachedClassLoader;
	private I_OrderedClassDiscovery orderedClassDiscovery;
	private I_ClassInstrumenter classInstrumenter;
	private boolean writeOutInstrumentedClassFiles = false;
	private String instrumentedClassFileOutputFolder;
	
	public TrialInstrumenter() {}
	
	/* (non-Javadoc)
	 * @see org.adligo.tests4j_4jacoco.plugin.I_TrialInstrumenter#instrument(java.lang.Class)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public I_Tests4J_CoverageTrialInstrumentation instrument(Class<? extends I_AbstractTrial> trial) throws IOException {
		if (log.isLogEnabled(TrialInstrumenter.class)) {
			log.log(ThreadLogMessageBuilder.getThreadForLog() + " instrumenting trial " + trial);
		}
		SourceFileScope sourceScope =  trial.getAnnotation(SourceFileScope.class);
		I_ClassDependenciesLocal sourceClassDependencies = null;
		if (sourceScope != null) {
			Class<?> clazz = sourceScope.sourceClass();
			InstrumentedClassDependencies icd = instrumentClass(clazz);
			sourceClassDependencies = icd.getClassDependencies();
		}
		PackageScope packageScope = trial.getAnnotation(PackageScope.class);
		if (packageScope != null) {
			String packageName = packageScope.packageName();
			PackageDiscovery pd = new PackageDiscovery(packageName);
			instrumentPackageClasses(pd);
		}
		InstrumentedClassDependencies icd = instrumentClass(trial);
		return new Tests4J_CoverageTrialInstrumentation(
				(Class<? extends I_AbstractTrial>) icd.getInstrumentedClass(), sourceClassDependencies);
		
	}

	protected void instrumentPackageClasses(PackageDiscovery pd)
			throws IOException {
		try {
			List<String> classes =  pd.getClassNames();
			for (String name: classes) {
				if ( !classFilter.isFiltered(name)) {
					Class<?> c;
					c = Class.forName(name);
					instrumentClass(c);
				}
			}
			List<PackageDiscovery> subs =  pd.getSubPackages();
			for (PackageDiscovery sub: subs) {
				instrumentPackageClasses(sub);
			}
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

	/**
	 * @diagram_sync with InstrumentationOverview on 8/17/2014
	 * @param c
	 * @return
	 * @throws IOException
	 */
	private InstrumentedClassDependencies instrumentClass(Class<?> c) throws IOException {
		String className = c.getName();
		if (log.isLogEnabled(ClassInstrumenter.class)) {
			log.log("ClassInstrumenter instrumenting class " + className);
		}
		I_OrderedClassDependencies ocd = null;
		try {
			//@diagram_sync with DiscoveryOverview.seq on 8/17/2014
			ocd = orderedClassDiscovery.findOrLoad(c);
		
			for (String dep: ocd.getOrder()) {
				if ( !classFilter.isFiltered(dep)) {
					if ( !instrumentedClassLoader.hasCache(dep)) {
						if (log.isLogEnabled(ClassInstrumenter.class)) {
							log.log("ClassInstrumenter " + className + " instrumenting delegate " + dep);
						}
						InputStream bais = cachedClassLoader.getCachedBytesStream(dep);
						
						byte [] bytes = classInstrumenter.instrumentClass(bais, dep);
						//instrumentedClassLoader should close the input stream
						instrumentedClassLoader.addCache(new ByteArrayInputStream(bytes), dep);
						if (writeOutInstrumentedClassFiles) {
							File file = new File(instrumentedClassFileOutputFolder + 
									File.pathSeparator + dep + ".class");
							FileOutputStream fos = new FileOutputStream(file);
							writeFile(bytes, fos);
						}
					}
				}
			}
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
		Class<?> instrClass = instrumentedClassLoader.getCachedClass(c.getName());
		return new InstrumentedClassDependencies(instrClass, ocd.getClassDependencies());
	}
	
	/**
	 * this is protected so I can test the closing of the output stream
	 * @param bytes
	 * @param out
	 */
	protected void writeFile(byte[] bytes, OutputStream out) {
		try {
			out.write(bytes);
		} catch (IOException x) {
			log.onThrowable(x);
		} finally {
			try {
				out.close();
			} catch (IOException x) {
				log.onThrowable(x);
			}
		}
	}
	
	public I_Tests4J_Log getLog() {
		return log;
	}

	public I_ClassFilter getClassFilter() {
		return classFilter;
	}

	public void setLog(I_Tests4J_Log log) {
		this.log = log;
	}

	public void setClassFilter(I_ClassFilter classFilter) {
		this.classFilter = classFilter;
	}

	public boolean isWriteOutInstrumentedClassFiles() {
		return writeOutInstrumentedClassFiles;
	}

	public String getInstrumentedClassFileOutputFolder() {
		return instrumentedClassFileOutputFolder;
	}

	public void setWriteOutInstrumentedClassFiles(
			boolean writeOutInstrumentedClassFiles) {
		this.writeOutInstrumentedClassFiles = writeOutInstrumentedClassFiles;
	}

	public void setInstrumentedClassFileOutputFolder(
			String instrumentedClassFileOutputFolder) {
		this.instrumentedClassFileOutputFolder = instrumentedClassFileOutputFolder;
	}

	public I_CachedClassBytesClassLoader getInstrumentedClassLoader() {
		return instrumentedClassLoader;
	}

	public I_CachedClassBytesClassLoader getCachedClassLoader() {
		return cachedClassLoader;
	}

	public I_OrderedClassDiscovery getOrderedClassDiscovery() {
		return orderedClassDiscovery;
	}

	public I_ClassInstrumenter getClassInstrumenter() {
		return classInstrumenter;
	}

	public void setInstrumentedClassLoader(
			I_CachedClassBytesClassLoader instrumentedClassLoader) {
		this.instrumentedClassLoader = instrumentedClassLoader;
	}

	public void setCachedClassLoader(I_CachedClassBytesClassLoader cachedClassLoader) {
		this.cachedClassLoader = cachedClassLoader;
	}

	public void setOrderedClassDiscovery(
			I_OrderedClassDiscovery orderedClassDiscovery) {
		this.orderedClassDiscovery = orderedClassDiscovery;
	}

	public void setClassInstrumenter(I_ClassInstrumenter classBytesInstrumenter) {
		this.classInstrumenter = classBytesInstrumenter;
	}
}
