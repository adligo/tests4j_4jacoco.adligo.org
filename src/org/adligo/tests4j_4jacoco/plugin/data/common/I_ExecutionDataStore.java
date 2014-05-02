package org.adligo.tests4j_4jacoco.plugin.data.common;

public interface I_ExecutionDataStore {
	public I_ClassCoverage get(long classId);
	public boolean contains(String className);
}
