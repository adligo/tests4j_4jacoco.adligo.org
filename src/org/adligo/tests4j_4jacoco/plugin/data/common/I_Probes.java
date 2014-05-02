package org.adligo.tests4j_4jacoco.plugin.data.common;

/**
 * this is just to make the boolean[] 
 * which jacoco uses immutable.
 * 
 * @author scott
 *
 */
public interface I_Probes {
	/**
	 * just a wrapper for probes[p]
	 * @param p
	 * @return
	 */
	public boolean get(int p);
	
	/**
	 * just a wrapper for probes.length
	 * @return
	 */
	public int size();
}
