package org.adligo.tests4j_4jacoco.plugin.data.common;


/**
 * implementations of this interface represent
 * the jacoco probes of a specific class.
 * This is was extracted from ExecutionData.
 * 
 * @author scott
 *
 */
public interface I_ClassProbes {
	/**
	 * the name of the class
	 * @return
	 */
	public String getClassName();
	/**
	 * the id of the class used for 
	 * @return
	 */
	public long getClassId();
	/**
	 * the probes 
	 * @return
	 */
	public I_Probes getProbes();
}
