package org.adligo.tests4j_4jacoco.plugin.data.multi;

import org.adligo.tests4j_4jacoco.plugin.data.common.AbstractRuntimeData;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_MultiRecordingProbeDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStoreAdaptor;

public class MultiProbeDataStoreAdaptor extends AbstractRuntimeData implements I_ProbesDataStoreAdaptor {
	private I_MultiRecordingProbeDataStore dataStore = new MultiProbeDataStore();

	@Override
	public void getProbes(Object[] args) {
		Object obj0 = args[0];
		if (obj0 instanceof Long) {
			final Long classid = (Long) args[0];
			final String name = (String) args[1];
			final int probecount = ((Integer) args[2]).intValue();
			args[0] = dataStore.get(classid, name, probecount);
		}
	}

	@Override
	public I_ProbesDataStore endTracking(String scope) {
		return dataStore.endRecording(scope);
	}

	@Override
	public void startTracking(String scope) {
		dataStore.startRecording(scope);
	}




}
