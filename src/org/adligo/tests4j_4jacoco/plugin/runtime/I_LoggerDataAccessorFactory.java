package org.adligo.tests4j_4jacoco.plugin.runtime;

/**
 * Implementations provide a way to pass around coverage data
 * using the java.util.logging interfaces.
 * 
 * @author scott
 *
 */
public interface I_LoggerDataAccessorFactory extends I_DataAccessorFactory {

	/**
	 * @return the string to put in the 
	 * java.util.Logging message
	 * so that the runtime can filter the log message.
	 */
	public String getKey();
	
	/**
	 * @return the string that pertains to the java.util.Logging
	 * channel used as a notifier to pass around coverage data
	 * (boolean[], java.util.Map, or some other data type)
	 */
	public String getChannel();
}
