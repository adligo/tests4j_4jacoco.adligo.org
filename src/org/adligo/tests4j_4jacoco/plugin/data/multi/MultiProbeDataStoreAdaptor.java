package org.adligo.tests4j_4jacoco.plugin.data.multi;

import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;
import org.adligo.tests4j_4jacoco.plugin.data.common.AbstractRuntimeData;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_MultiRecordingProbeDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStoreAdaptor;

public class MultiProbeDataStoreAdaptor extends AbstractRuntimeData implements I_ProbesDataStoreAdaptor {
	private I_MultiRecordingProbeDataStore dataStore;
	private I_Tests4J_Log logger;
	
	
	public MultiProbeDataStoreAdaptor(I_Tests4J_Log pLogger) {
		logger = pLogger;
		dataStore = new MultiProbeDataStore(logger);
	}
	@Override
	public void getProbes(Object[] args) {
		if (logger.isLogEnabled(MultiProbeDataStore.class)) {
			logger.log("MultiProbeDataStore.getProbes " + args);
		}
		Object obj0 = args[0];
		if (obj0 instanceof Long) {
			final Long classid = (Long) args[0];
			final String name = (String) args[1];
			final int probecount = ((Integer) args[2]).intValue();
			args[0] = dataStore.get(classid, name, probecount);
			if (logger.isLogEnabled(MultiProbeDataStore.class)) {
				logger.log("MultiProbeDataStore.getProbes after assignment " + args[0]);
			}
		}
	}

	@Override
	public I_ProbesDataStore endTracking(boolean main) {
		return dataStore.endRecording(main);
	}

	@Override
	public void startTracking() {
		dataStore.startRecording();
	}




}
