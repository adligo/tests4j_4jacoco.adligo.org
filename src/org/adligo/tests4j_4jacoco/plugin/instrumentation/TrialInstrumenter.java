package org.adligo.tests4j_4jacoco.plugin.instrumentation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.adligo.tests4j.models.shared.association.I_ClassAssociationsLocal;
import org.adligo.tests4j.models.shared.association.I_ClassFilter;
import org.adligo.tests4j.run.discovery.PackageDiscovery;
import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;
import org.adligo.tests4j.shared.asserts.reference.AllowedReferences;
import org.adligo.tests4j.shared.asserts.reference.I_ReferenceGroup;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;
import org.adligo.tests4j.system.shared.api.I_Tests4J_CoverageTrialInstrumentation;
import org.adligo.tests4j.system.shared.api.Tests4J_CoverageTrialInstrumentation;
import org.adligo.tests4j.system.shared.trials.AdditionalInstrumentation;
import org.adligo.tests4j.system.shared.trials.I_AbstractTrial;
import org.adligo.tests4j.system.shared.trials.PackageScope;
import org.adligo.tests4j.system.shared.trials.SourceFileScope;
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
	private TrialInstrumenterSharedMemory memory;
	private AtomicInteger todo = new AtomicInteger();
	private AtomicInteger done = new AtomicInteger();
	private AtomicBoolean classStart = new AtomicBoolean(false);
	
	public TrialInstrumenter(TrialInstrumenterSharedMemory memoryIn) {
		memory = memoryIn;
	}
	
	/* (non-Javadoc)
	 * @see org.adligo.tests4j_4jacoco.plugin.I_TrialInstrumenter#instrument(java.lang.Class)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public I_Tests4J_CoverageTrialInstrumentation instrument(Class<? extends I_AbstractTrial> trial) throws IOException {
		if (log.isLogEnabled(TrialInstrumenter.class)) {
			log.log(this.getClass().getSimpleName() + 
					log.getCurrentThreadName() + log.getLineSeperator() +
					" instrumenting trial " + trial);
		}
		/**
		 * always to additional instrumentation first
		 * so that trials can have their own
		 * package dependency tree
		 */
		AdditionalInstrumentation additional = trial.getAnnotation(AdditionalInstrumentation.class);
		if (additional != null) {
			String pkgs = additional.javaPackages();
			StringTokenizer st = new StringTokenizer(pkgs, ",");
			while (st.hasMoreElements()) {
				String pkg = st.nextToken();
				checkPackageInstrumented(pkg);
			}
		}
		
		SourceFileScope sourceScope =  trial.getAnnotation(SourceFileScope.class);
		I_ClassAssociationsLocal sourceClassDependencies = null;
		Class<?> sourceClass = null;;
		String packageName = null;
		if (sourceScope != null) {
			sourceClass = sourceScope.sourceClass();
			InstrumentedClassDependencies icd = instrumentClass(sourceClass);
			sourceClassDependencies = icd.getClassDependencies();
			//get allowed dependencies
			AllowedReferences ad = trial.getAnnotation(AllowedReferences.class);
			if (ad != null) {
				Class<? extends I_ReferenceGroup>[] grps = ad.groups();
				for (Class<? extends I_ReferenceGroup> grp: grps) {
					instrumentClass(grp);
				}
			}
			
		} else {
			PackageScope packageScope = trial.getAnnotation(PackageScope.class);
			
			if (packageScope != null) {
				packageName = packageScope.packageName();
			}
		}	
		
		checkPackageInstrumented(packageName);
		
		classStart.set(true);
		InstrumentedClassDependencies icd = instrumentClass(trial);
		return new Tests4J_CoverageTrialInstrumentation(
				(Class<? extends I_AbstractTrial>) icd.getInstrumentedClass(), sourceClassDependencies);
		
	}

	public void checkPackageInstrumented(String packageName) throws IOException {
		if (packageName != null) {
			if (log.isLogEnabled(TrialInstrumenter.class)) {
				log.log(this.getClass().getSimpleName() +  " instrument package " + packageName);
			}
			if ( !memory.hasStarted(packageName)) {
				//only one thread needs to do each package,
				//as they link up later before the start of the test
				memory.start(packageName);
				PackageDiscovery pd = new PackageDiscovery(packageName);
				todo.addAndGet(pd.getClassCount());
				instrumentPackageClasses(pd);
				memory.finish(packageName);
			}
		}
	}

	protected void instrumentPackageClasses(PackageDiscovery pd)
			throws IOException {
		if (log.isLogEnabled(TrialInstrumenter.class)) {
			log.log(this.getClass().getSimpleName() +  " instrumentPackageClasses " + pd.getPackageName());
		}
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
		if (log.isLogEnabled(TrialInstrumenter.class)) {
			log.log(this.getClass().getSimpleName() + 
					" instrumenting class " + className);
		}
		I_OrderedClassDependencies ocd = null;
		try {
			//@diagram_sync with DiscoveryOverview.seq on 8/17/2014
			ocd = orderedClassDiscovery.findOrLoad(c);
			List<String> order = ocd.getOrder();
			if (classStart.get()) {
				todo.addAndGet(order.size());
			}
			for (String dep: order) {
				
				if ( !classFilter.isFiltered(dep)) {
					if ( !instrumentedClassLoader.hasCache(dep)) {
						if (log.isLogEnabled(TrialInstrumenter.class)) {
							log.log(this.getClass().getSimpleName() + 
									" " + c.getSimpleName() + " instrumenting delegate " + dep);
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
				done.addAndGet(1);
			}
		} catch (Exception e) {
			throw new IOException("problem in instrumentClass " + c.getName() ,e);
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

	@Override
	public double getPctDone() {
		double todoD = todo.get();
		double doneD = done.get();
		double toRet = doneD/todoD * 100;
		if ( !classStart.get()) {
			//halve it
			toRet = toRet/2;
		}
		return 0;
	}
}
