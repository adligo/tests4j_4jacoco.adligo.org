package org.adligo.tests4j_4jacoco.plugin.instrumentation.common;

import org.adligo.tests4j_4jacoco.plugin.common.I_ProbeDataAccessorFactory;

/**
 * Information about the class getting instrumented
 * @author scott
 *
 */
public interface I_ClassInstrumentationInfo {
	/**
	 * the id of the class, used by jacoco to find it's probe data
	 * @see I_ProbeDataAccessorFactory
	 * @return
	 */
	public long getId();
	/**
	 * the name of the class
	 * @return
	 */
	public String getClassName();
	/**
	 * the number of probes in the class
	 * @return
	 */
	public int getProbeCount();
	
	/**
	 * if the frame code should be called
	 * @return
	 */
	public boolean isWithFrames();
}
