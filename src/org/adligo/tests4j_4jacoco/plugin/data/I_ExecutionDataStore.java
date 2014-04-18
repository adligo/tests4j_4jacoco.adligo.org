package org.adligo.tests4j_4jacoco.plugin.data;

public interface I_ExecutionDataStore {
	public I_ExecutionClassData get(long classId);
	public boolean contains(String className);
}
