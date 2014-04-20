package org.adligo.tests4j_4jacoco.plugin.runtime;



/**
 * a interface for controlling the jacoco runtime
 * @author scott
 *
 */
public interface I_JacocoRuntime {
	public void setup(I_JacocoRuntimeData p);
	
	
	public void startup() throws SecurityException;
	/*
	public void startRecording(String scope);
	public I_JacocoRuntimeData stopRecording(String scope);
	*/
	public I_JacocoRuntimeData shutdown();
}
