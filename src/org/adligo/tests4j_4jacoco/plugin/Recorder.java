package org.adligo.tests4j_4jacoco.plugin;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.List;

import org.adligo.tests4j.models.shared.coverage.I_PackageCoverage;
import org.adligo.tests4j.models.shared.system.I_Tests4J_CoverageRecorder;
import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;
import org.adligo.tests4j_4jacoco.plugin.common.I_CoveragePluginMemory;
import org.adligo.tests4j_4jacoco.plugin.common.I_Runtime;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.coverage.LazyPackageCoverageFactory;

public class Recorder implements I_Tests4J_CoverageRecorder {
	protected I_Tests4J_Log reporter;
	protected I_CoveragePluginMemory memory;
	private boolean root;
	private I_Runtime runtime;
	private boolean jacocoInitOnFirstRecording = true;
	
	public Recorder(I_CoveragePluginMemory pMemory, I_Tests4J_Log pLog) {
		memory = pMemory;
		reporter = pLog;
		runtime = memory.getRuntime();
	}
	

	@Override
	public void startRecording() {
		if (reporter != null) {
			if (reporter.isLogEnabled(Recorder.class)) {
				reporter.log("Recorder starting ");
			}
		}
		try {
			runtime.startup();
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
		
		if (jacocoInitOnFirstRecording) {
			if (root) {
				I_CachedClassBytesClassLoader mcl = memory.getInstrumentedClassLoader();
				List<String> allClasses = mcl.getAllCachedClasses();
				int progress = 0;
				double nextProgressLog = 10.0;
				for (String clazz: allClasses) {
					progress++;
					double dp = 0.0 + progress;
					double tot = 0.0 + allClasses.size();
					
					if (dp/tot >= nextProgressLog) {
						nextProgressLog = nextProgressLog + 10.0;
						DecimalFormat df = new DecimalFormat("###.#%");
						reporter.log("tests4j_4jacoco " + df.format(dp/tot) + "% calling $jacocoInit()s");
					}
					if (clazz.indexOf("$") == -1) {
						Class<?> loadedClass = mcl.getCachedClass(clazz);
						if (loadedClass != null) {
							try {
								Method jacocoInit = loadedClass.getMethod("$jacocoInit", new Class[] {});
								
								if (jacocoInit != null) {
									jacocoInit.invoke(loadedClass, new Object[] {});
								}
							} catch (NoSuchMethodException x) {
								//interfaces don't have it
							} catch (Throwable e) {
								throw new RuntimeException("Class " + clazz + " failed to call $jacocoInit.", e);
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public List<I_PackageCoverage> endRecording() {
		if (reporter != null) {
			if (reporter.isLogEnabled(Recorder.class)) {
				reporter.log("Ending Recording ");
			}
		}
		/*
		final ExecutionDataStore executionData = new ExecutionDataStore();
		final SessionInfoStore sessionInfos = new SessionInfoStore();
		data.collect(executionData, sessionInfos, false);
		*/
		I_ProbesDataStore executionData = runtime.end(root);
		
		return LazyPackageCoverageFactory.create(executionData, memory);
	}


	public static String getPackageDir(final String name) {
		final String resource = '/' + name.replace('.', '/');
		int lastDot = resource.lastIndexOf("/");
		return resource.substring(0, lastDot);
	}
	



	public boolean isRoot() {
		return root;
	}

	public void setRoot(boolean root) {
		this.root = root;
	}

	
}