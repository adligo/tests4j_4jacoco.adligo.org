package org.adligo.tests4j_4jacoco.plugin.runtime;

import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;



/**
 * a interface for controlling the jacoco runtime
 * @author scott
 *
 */
public interface I_Runtime {
	public void startup() throws SecurityException;
	public void shutdown();
	public I_ProbesDataStore getCoverageData(String scope);
}
