package org.adligo.tests4j_4jacoco.plugin.data.multi;

import org.adligo.tests4j.models.shared.coverage.I_SourceFileCoverageBrief;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;
import org.adligo.tests4j_4jacoco.plugin.data.common.AbstractRuntimeData;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_MultiRecordingProbeDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStoreAdaptor;

import java.util.Iterator;

public class MultiProbeDataStoreAdaptor extends AbstractRuntimeData implements I_ProbesDataStoreAdaptor {
	private I_MultiRecordingProbeDataStore dataStore;
	private I_Tests4J_Log logger;
	
	
	public MultiProbeDataStoreAdaptor(MultiContext ctx) {
		logger = ctx.getLog();
		dataStore = new MultiProbeDataStore(ctx);
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
	public I_ProbesDataStore getRecordedProbes(boolean main) {
		return dataStore.getRecordedProbes(main);
	}

	@Override
	public void startup() {
		dataStore.startRecording();
	}
  @Override
  public I_SourceFileCoverageBrief getSourceFileProbes(String threadGroupName,
      String sourceFileClassName, Iterator<Long> classIds) {
    return dataStore.getSourceFileProbes(threadGroupName, sourceFileClassName, classIds);
  }




}
