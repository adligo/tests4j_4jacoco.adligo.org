package org.adligo.tests4j_4jacoco.plugin.runtime;

import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;



/**
 * a interface for controlling the jacoco runtime
 * @author scott
 *
 */
public interface I_Runtime {
	/**
	 * starts/resumes recording for a scope. 
	 * @param scope
	 * @throws SecurityException
	 */
	public void startup() throws SecurityException;
	/**
	 * shutsdown for all scopes
	 */
	public void shutdown();
	/**
	 * returns the coverage data and clears it out of memory.
	 * 
	 * @return
	 */
	public I_ProbesDataStore end();
}
