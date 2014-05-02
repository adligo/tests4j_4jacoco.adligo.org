package org.adligo.tests4j_4jacoco.plugin.data.multi;

import java.util.Map;

import org.adligo.tests4j.models.shared.system.I_CoverageRecorder;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ClassCoverage;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_MapExecutionDataStore;

/**
 * This class represents a in memory data store for probes
 * which can be used for multiple {@link I_CoverageRecorder}'s
 * at the same time.
 * 
 * @author scott
 *
 */
public class MultiDataStore implements I_MapExecutionDataStore {

	@Override
	public Map<Integer, Boolean> get(Long id, String name, int probecount) {
		// TODO Auto-generated method stub
		return null;
	}

}
