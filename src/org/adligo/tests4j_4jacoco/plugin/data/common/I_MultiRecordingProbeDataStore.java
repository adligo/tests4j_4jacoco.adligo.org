package org.adligo.tests4j_4jacoco.plugin.data.common;

import java.util.Map;

public interface I_MultiRecordingProbeDataStore extends I_MultiProbesStore {
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
	
	/**
	 * start/resume a recording for the scope
	 * @param scope
	 */
	public void startRecording();
	/**
	 *     
	 * @param mainScope
	 * @return the current coverage data either
   * for the entire recording (mainScope = true),
   * or for a ThreadGroup (combination of trial and test
   * thread locals).
	 */
	public I_ProbesDataStore getRecordedProbes(boolean mainScope);
}
