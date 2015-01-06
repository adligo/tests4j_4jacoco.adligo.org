package org.adligo.tests4j_4jacoco.plugin.instrumentation;

import org.adligo.tests4j_4jacoco.plugin.common.I_InstrumentedClassDependencies;
import org.adligo.tests4j_4jacoco.plugin.common.InstrumentedClassDependencies;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClassInstrumenterSharedMemory {
	private ConcurrentHashMap<String,AtomicBoolean> packagesStarted = 
			new ConcurrentHashMap<String,AtomicBoolean>();
	private ConcurrentHashMap<String, InstrumentedClassDependencies> classDeps = 
	    new ConcurrentHashMap<String, InstrumentedClassDependencies>();
	
	public boolean hasStarted(String packageName) {
		if (packagesStarted.containsKey(packageName)) {
			return true;
		}
		return false;
	}
	
	public void start(String packageName) {
		packagesStarted.putIfAbsent(packageName, new AtomicBoolean(false));
	}
	
	public void finish(String packageName) {
		AtomicBoolean block = packagesStarted.get(packageName);
		block.set(true);
	}
	
	public void putIfAbsent(String clazzName, InstrumentedClassDependencies deps) {
	  classDeps.putIfAbsent(clazzName, deps);
	}
	public I_InstrumentedClassDependencies getDependencies(String clazzName) {
    return classDeps.get(clazzName);
  }
}
