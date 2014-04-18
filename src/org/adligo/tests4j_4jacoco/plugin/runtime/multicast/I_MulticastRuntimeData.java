package org.adligo.tests4j_4jacoco.plugin.runtime.multicast;

import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;

public interface I_MulticastRuntimeData {
	
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
	 * Collects the current execution data and writes it to the given
	 * {@link IExecutionDataVisitor} object.
	 * 
	 * @param executionDataVisitor
	 *            handler to write coverage data to
	 * @param sessionInfoVisitor
	 *            handler to write session information to
	 * @param reset
	 *            if <code>true</code> the current coverage information is also
	 *            cleared
	 */
	public void collect(final I_MulticastExecutionDataVisitor executionDataVisitor,
			final ISessionInfoVisitor sessionInfoVisitor, final boolean reset);

	/**
	 * Resets all coverage information.
	 */
	public void reset();

	/**
	 * Returns the coverage data for the class with the given identifier. If
	 * there is no data available under the given id a new entry is created.
	 * This is a synchronized access to the underlying store.
	 * 
	 * @param id
	 *            class identifier
	 * @param name
	 *            VM name of the class
	 * @param probecount
	 *            probe data length
	 * @return execution data
	 */
	public MulticastExecutionData getExecutionData(final Long id, final String name,
			final int probecount);

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
	 * Return value:
	 * 
	 * <ul>
	 * <li>args[0]: probe array (<code>boolean[]</code>)
	 * </ul>
	 * 
	 * @param args
	 *            parameter array of length 3
	 */
	public void getProbes(final Object[] args);
}
