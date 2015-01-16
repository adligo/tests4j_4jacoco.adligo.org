package org.adligo.tests4j_4jacoco.plugin.instrumentation;

import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;
import org.adligo.tests4j.run.helpers.I_ClassFilter;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;
import org.adligo.tests4j_4jacoco.plugin.common.I_ClassInstrumenter;
import org.adligo.tests4j_4jacoco.plugin.common.I_InstrumentedClassDependencies;
import org.adligo.tests4j_4jacoco.plugin.common.I_OrderedClassDependencies;
import org.adligo.tests4j_4jacoco.plugin.common.I_OrderedClassDiscovery;
import org.adligo.tests4j_4jacoco.plugin.common.InstrumentedClassDependencies;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ClassAndDependenciesInstrumenter  {
  protected I_Tests4J_Log log_;
	protected I_ClassFilter classFilter;
	/**
	 * contains the instrumented classes with injected byte code
	 */
	protected I_CachedClassBytesClassLoader instrumentedClassLoader;
	/**
	 * contains the regular class bytes cache, so reloading isn't necessary
	 * between threads
	 */
	protected I_CachedClassBytesClassLoader cachedClassLoader;
	protected I_OrderedClassDiscovery orderedClassDiscovery;
	protected I_ClassInstrumenter classInstrumenter;
	protected boolean writeOutInstrumentedClassFiles = false;
	protected String instrumentedClassFileOutputFolder;
	protected AtomicInteger todo = new AtomicInteger();
	protected AtomicInteger done = new AtomicInteger();
	protected AtomicBoolean classStart = new AtomicBoolean(false);
	protected ClassInstrumenterSharedMemory memory_;
	
	public ClassAndDependenciesInstrumenter(ClassInstrumenterSharedMemory memory) {
	  memory_ = memory;
	}
	
	/**
	 * @param c
	 * @return
	 * @throws IOException
	 */
	public I_InstrumentedClassDependencies instrumentClass(Class<?> c) throws IOException {
		String className = c.getName();
		I_InstrumentedClassDependencies cached = memory_.getDependencies(className);
		if (cached != null) {
		  return cached;
		}
		if (log_.isLogEnabled(ClassAndDependenciesInstrumenter.class)) {
			log_.log(ClassAndDependenciesInstrumenter.class.getSimpleName() + 
					" instrumenting class " + className);
		}
		I_OrderedClassDependencies ocd = null;
		List<String> order = null;
		String whichDep = null;
		try {
			//@diagram_sync with DiscoveryOverview.seq on 8/17/2014
			ocd = orderedClassDiscovery.findOrLoad(c);
			order = ocd.getOrder();
			if (classStart.get()) {
				todo.addAndGet(order.size());
			}
			for (String dep: order) {
			  whichDep = dep;
				if ( !classFilter.isFiltered(dep)) {
					if ( !instrumentedClassLoader.hasCache(dep)) {
						if (log_.isLogEnabled(ClassAndDependenciesInstrumenter.class)) {
						  if (dep.equals(c.getName())) {
						    log_.log(ClassAndDependenciesInstrumenter.class.getSimpleName() + 
                    " " + c.getSimpleName() + " instrumenting self");
						  } else {
  							log_.log(ClassAndDependenciesInstrumenter.class.getSimpleName() + 
  									" " + c.getSimpleName() + " instrumenting delegate " + dep);
						  }
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
		  /*
		  StringBuilder ob = new StringBuilder();
		  if (order != null) {
		    for(String dep: order) {
		      ob.append(dep);
		      ob.append(System.lineSeparator());
		    }
		  }
		  */
			throw new IOException("problem in instrumentClass " + System.lineSeparator() +
			    c.getName() + " for dependency " + whichDep,e);
		}
		Class<?> instrClass = instrumentedClassLoader.getCachedClass(c.getName());
		InstrumentedClassDependencies deps = new InstrumentedClassDependencies(instrClass, ocd.getClassDependencies());
		memory_.putIfAbsent(className, deps);
		return deps;
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
			log_.onThrowable(x);
		} finally {
			try {
				out.close();
			} catch (IOException x) {
				log_.onThrowable(x);
			}
		}
	}
	
	public I_Tests4J_Log getLog() {
		return log_;
	}

	public I_ClassFilter getClassFilter() {
		return classFilter;
	}

	public void setLog(I_Tests4J_Log log) {
		this.log_ = log;
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
