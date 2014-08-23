package org.adligo.tests4j_4jacoco.plugin.instrumentation;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class TrialInstrumenterSharedMemory {
	private ConcurrentHashMap<String,AtomicBoolean> packagesStarted = 
			new ConcurrentHashMap<String,AtomicBoolean>();

	
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
	
}
