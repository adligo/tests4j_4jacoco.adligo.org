package org.adligo.tests4j_4jacoco.plugin.data.common;

import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;

public abstract class AbstractRuntimeData implements I_ProbesDataStoreAdaptor {
	

	private String sessionId;

	/**
	 * Creates a new runtime.
	 */
	public AbstractRuntimeData() {
		sessionId = "<none>";
	}

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
	public void setSessionId(final String id) {
		sessionId = id;
	}

	/**
	 * Get the current a session identifier for this runtime.
	 * 
	 * @see #setSessionId(String)
	 * @return current session identifier
	 */
	public String getSessionId() {
		return sessionId;
	}



}
