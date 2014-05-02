package org.adligo.tests4j_4jacoco.plugin.data.common;

import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;

/**
 * this is called IRuntimeData in jacoco,
 * I changed the name to match better with what I think it is intended for.
 * This is to adapt the I_ProbeDataStore (aka jacoco's IExecutionDataStore)
 * to the jacoco runtime, most notibly getProbes(Object []); 
 * 
 * @author scott
 *
 */
public interface I_ProbesDataStoreAdaptor {
	
	/**
	 * Sets a session identifier for this runtime. The identifier is used when
	 * execution data is collected. If no identifier is explicitly set a
	 * identifier is generated from the host name and a random number. This
	 * method can be called at any time.
	 * 
	 * @see #collect(IExecutionDataVisitor, ISessionInfoVisitor, boolean)
	 * @param id
	 *            new session identifier
	 */
	public void setSessionId(final String id);

	/**
	 * Get the current a session identifier for this runtime.
	 * 
	 * @see #setSessionId(String)
	 * @return current session identifier
	 */
	public String getSessionId();

	/**
	 * Retrieves the execution probe array for a given class. The passed
	 * {@link Object} array instance is used for parameters and the return value
	 * as follows. Call parameters:
	 * 
	 * <ul>
	 * <li>args[0]: class id ({@link Long})
	 * <li>args[1]: vm class name ({@link String})
	 * <li>args[2]: probe count ({@link Integer})
	 * </ul>
	 * 
	 * Return value may be one of:
	 * 
	 * <ul>
	 * <li>args[0]: probe array (<code>boolean[]</code>)
	 * </ul>
	 *
	 *	<ul>
	 * 	<li>args[0]: Map backed by a custom Map impl so the 
	 *           mutations to the probes can be filtered for sub recordings.
	 * 	</ul>
	 * 
	 * @param args
	 *            parameter array of length 3
	 */
	public void getProbes(final Object[] args);
	
	public I_ProbesDataStore getCoverageData(String scope);
}
