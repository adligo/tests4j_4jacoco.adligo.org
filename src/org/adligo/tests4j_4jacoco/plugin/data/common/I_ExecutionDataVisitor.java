package org.adligo.tests4j_4jacoco.plugin.data.common;


/**
 * Interface for data output of collected execution data. This interface is
 * meant to be implemented by parties that want to retrieve data from the
 * coverage runtime.
 */
public interface I_ExecutionDataVisitor {

	/**
	 * Provides execution data for a class.
	 * 
	 * @param data
	 *            execution data for a class
	 */
	public void visitClassExecution(I_ClassCoverage data);

}
