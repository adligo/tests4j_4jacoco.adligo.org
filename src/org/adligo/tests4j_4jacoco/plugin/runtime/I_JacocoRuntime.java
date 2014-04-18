package org.adligo.tests4j_4jacoco.plugin.runtime;

import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;


public interface I_JacocoRuntime extends IExecutionDataAccessorGenerator {
	public void startup() throws SecurityException;
	/*
	public void startRecording(String scope);
	public I_JacocoRuntimeData stopRecording(String scope);
	*/
	public I_JacocoRuntimeData shutdown();
}
