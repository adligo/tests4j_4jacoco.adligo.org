package org.adligo.tests4j_4jacoco.plugin.data.multi;

import org.adligo.tests4j_4jacoco.plugin.data.common.AbstractRuntimeData;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ExecutionDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_MapExecutionDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_RuntimeData;

public class MultiRuntimeData extends AbstractRuntimeData implements I_RuntimeData {
	private I_MapExecutionDataStore dataStore = new MultiDataStore();

	@Override
	public void getProbes(Object[] args) {
		final Long classid = (Long) args[0];
		final String name = (String) args[1];
		final int probecount = ((Integer) args[2]).intValue();
		args[0] = dataStore.get(classid, name, probecount);
	}

	@Override
	public I_ExecutionDataStore getCoverageData(String scope) {
		// TODO Auto-generated method stub
		return null;
	}


}
