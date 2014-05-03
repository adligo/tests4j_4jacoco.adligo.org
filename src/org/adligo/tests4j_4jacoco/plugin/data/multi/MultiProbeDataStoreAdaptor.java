package org.adligo.tests4j_4jacoco.plugin.data.multi;

import org.adligo.tests4j_4jacoco.plugin.data.common.AbstractRuntimeData;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_MultiRecordingProbeDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStoreAdaptor;

public class MultiProbeDataStoreAdaptor extends AbstractRuntimeData implements I_ProbesDataStoreAdaptor {
	private I_MultiRecordingProbeDataStore dataStore = new MultiProbeDataStore();

	@Override
	public void getProbes(Object[] args) {
		final Long classid = (Long) args[0];
		final String name = (String) args[1];
		final int probecount = ((Integer) args[2]).intValue();
		args[0] = dataStore.get(classid, name, probecount);
	}

	@Override
	public I_ProbesDataStore getCoverageData(String scope) {
		// TODO Auto-generated method stub
		return null;
	}


}
