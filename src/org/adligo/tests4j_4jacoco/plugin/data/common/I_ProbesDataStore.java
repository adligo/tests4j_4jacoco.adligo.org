package org.adligo.tests4j_4jacoco.plugin.data.common;

import java.util.Map;
import java.util.Set;

/**
 * this represents a specific all of the probes for a
 * specific recorder.
 * This was extracted from jacoco's ExecutionDataStore
 * @author scott
 *
 */
public interface I_ProbesDataStore {
	/**
	 * this is necessary to copy between immutable and mutable variants
	 * @return
	 */
	public Map<Long, I_ClassProbes> getAllCoverage();
	/**
	 * return the immutable class probes
	 * @param classId
	 * @return
	 */
	public I_ClassProbes get(long classId);
	/**
	 * this is necessary to copy between immutable and mutable variants
	 * @return
	 */
	public Set<String> getAllClassNames();
	/**
	 * return true if the className had probes
	 * @param className
	 * @return
	 */
	public boolean contains(String className);
}
