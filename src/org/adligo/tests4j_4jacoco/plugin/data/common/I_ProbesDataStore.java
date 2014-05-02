package org.adligo.tests4j_4jacoco.plugin.data.common;

import java.util.Collection;

/**
 * this represents a specific all of the probes for a
 * specific recorder.
 * This was extracted from jacoco's ExecutionDataStore
 * @author scott
 *
 */
public interface I_ProbesDataStore {
	public Collection<I_ClassProbes> getAllCoverage();
	public I_ClassProbes get(long classId);
	public boolean contains(String className);
}
