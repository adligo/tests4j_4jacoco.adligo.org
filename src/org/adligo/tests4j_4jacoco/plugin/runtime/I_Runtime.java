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
	public void startup(String scope) throws SecurityException;
	/**
	 * pauses for a particular scope
	 * @param scope
	 */
	public void pause(String scope);
	/**
	 * shutsdown for all scopes
	 */
	public void shutdown();
	/**
	 * clears memory for the scope 
	 * and returns the coverage data.
	 * 
	 * @param scope
	 * @return
	 */
	public I_ProbesDataStore end(String scope);
}
