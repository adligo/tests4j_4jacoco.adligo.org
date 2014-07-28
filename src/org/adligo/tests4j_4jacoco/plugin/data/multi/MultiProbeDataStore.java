package org.adligo.tests4j_4jacoco.plugin.data.multi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.adligo.tests4j.models.shared.system.I_Tests4J_CoverageRecorder;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;
import org.adligo.tests4j.run.helpers.ThreadLogMessageBuilder;
import org.adligo.tests4j_4jacoco.plugin.data.common.ClassProbes;
import org.adligo.tests4j_4jacoco.plugin.data.common.ClassProbesMutant;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_MultiRecordingProbeDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.Probes;
import org.adligo.tests4j_4jacoco.plugin.data.common.ProbesDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.ProbesDataStoreMutant;

/**
 * This class represents a in memory data store for probes
 * which can be used for multiple {@link I_Tests4J_CoverageRecorder}'s
 * at the same time.
 * 
 * @author scott
 *
 */
public class MultiProbeDataStore implements I_MultiRecordingProbeDataStore {
	private final ConcurrentHashMap<Long,MultiProbesMap> classIdsToMulti = 
			new ConcurrentHashMap<Long,MultiProbesMap>();
	private final ConcurrentMapValueAvailableNotifier<Long, MultiProbesMap> classIds = 
			new ConcurrentMapValueAvailableNotifier<Long, MultiProbesMap>(classIdsToMulti);

	private final I_Tests4J_Log reporter;
	
	public MultiProbeDataStore(I_Tests4J_Log p) {
		reporter = p;
	}
	
	@Override
	public Map<Integer, Boolean> get(final Long id, final String name, final int probecount) {
		if (reporter.isLogEnabled(MultiProbeDataStore.class)) {
			reporter.log(ThreadLogMessageBuilder.getThreadWithGroupNameForLog() +
					"\n is getting probes for class " + name);
		}
		MultiProbesMap toRet = classIdsToMulti.get(id);
		if (toRet == null) {
			if (!classIds.containsKey(id)) {
				classIds.put(id, new I_ValueCreator<MultiProbesMap>() {
					
					@Override
					public MultiProbesMap create() {
						return new MultiProbesMap(name, probecount, reporter);
					}
				});
				
				toRet =  classIdsToMulti.get(id);
				if (toRet == null) {
					//block until the id shows up
					classIds.await(id);
					toRet = classIdsToMulti.get(id);
				}
			} 
		} 
		if (toRet == null) {
			toRet =  classIdsToMulti.get(id);
		}
		
		return toRet;
	}

	@Override
	public void startRecording() {
	}

	@Override
	public synchronized I_ProbesDataStore endRecording(boolean main) {
		ProbesDataStoreMutant pdsm = new ProbesDataStoreMutant();
		
		//spin through a snapshot, of all threads
		Set<Entry<Long, MultiProbesMap>> entries =  new HashMap<Long, MultiProbesMap>(classIdsToMulti).entrySet();
		Iterator<Entry<Long, MultiProbesMap>> it =  entries.iterator();
		while (it.hasNext()) {
			Entry<Long, MultiProbesMap> entry = it.next();
			Long clazzId = entry.getKey();
			MultiProbesMap val = entry.getValue();
			
			boolean [] probeVals = null;
			if (main) {
				probeVals = val.getProbes();
			} else {
				probeVals = val.getThreadGroupProbes();
			}
			
			ClassProbesMutant cpm = new ClassProbesMutant();
			cpm.setClassId(clazzId);
			String classCovered = val.getClazzCovered();

			cpm.setClassName(classCovered);
			cpm.setProbes(new Probes(probeVals));
			pdsm.put(clazzId, new ClassProbes(cpm));
			
			val.releaseRecording();
			
		}
		return new ProbesDataStore(pdsm);
	}

}
