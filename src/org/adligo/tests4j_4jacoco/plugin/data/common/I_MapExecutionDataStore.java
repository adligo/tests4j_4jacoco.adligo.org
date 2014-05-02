package org.adligo.tests4j_4jacoco.plugin.data.common;

import java.util.Map;

public interface I_MapExecutionDataStore {
	/**
	 * obtain a Map which represents the current state of the probes
	 *    Note the Map may be backed by data for multiple recorders
	 * @param id
	 * @param name
	 * @param probecount
	 * @return
	 */
	public Map<Integer, Boolean> get(final Long id, final String name,
			final int probecount);
}
