package org.adligo.tests4j_4jacoco.plugin.data.multi;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.adligo.tests4j.models.shared.system.I_CoverageRecorder;
import org.adligo.tests4j_4jacoco.plugin.data.common.ClassProbes;
import org.adligo.tests4j_4jacoco.plugin.data.common.ClassProbesMutant;
import org.adligo.tests4j_4jacoco.plugin.data.common.CoverageRecorderStates;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_MultiRecordingProbeDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.Probes;
import org.adligo.tests4j_4jacoco.plugin.data.common.ProbesDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.ProbesDataStoreMutant;

/**
 * This class represents a in memory data store for probes
 * which can be used for multiple {@link I_CoverageRecorder}'s
 * at the same time.
 * 
 * @author scott
 *
 */
public class MultiProbeDataStore implements I_MultiRecordingProbeDataStore {
	private CoverageRecorderStates states = 
			new CoverageRecorderStates();
	private Map<Long,MultiProbesMap> classIdsToMulti = 
			new ConcurrentHashMap<Long,MultiProbesMap>();
			
	@Override
	public Map<Integer, Boolean> get(Long id, String name, int probecount) {
		MultiProbesMap toRet = classIdsToMulti.get(id);
		if (toRet == null) {
			synchronized(classIdsToMulti) {
				toRet = classIdsToMulti.get(id);
				if (toRet == null) {
					toRet = new MultiProbesMap(states, name, probecount);
					classIdsToMulti.put(id, toRet);
				}
			}
		}
		return toRet;
	}

	@Override
	public void startRecording(String scope) {
		states.setRecording(scope, true);
	}

	@Override
	public void pauseRecording(String scope) {
		states.setRecording(scope, false);
	}

	@Override
	public synchronized I_ProbesDataStore endRecording(String scope) {
		states.setRecording(scope, false);
		ProbesDataStoreMutant pdsm = new ProbesDataStoreMutant();
		Set<Entry<Long, MultiProbesMap>> entries =  classIdsToMulti.entrySet();
		Iterator<Entry<Long, MultiProbesMap>> it =  entries.iterator();
		while (it.hasNext()) {
			Entry<Long, MultiProbesMap> entry = it.next();
			Long clazzId = entry.getKey();
			MultiProbesMap val = entry.getValue();
			
			boolean [] probeVals = val.getProbes(scope);
			
			ClassProbesMutant cpm = new ClassProbesMutant();
			cpm.setClassId(clazzId);
			cpm.setClassName(val.getClazzCovered());
			cpm.setProbes(new Probes(probeVals));
			pdsm.put(clazzId, new ClassProbes(cpm));
			
			val.releaseRecording(scope);
			
		}
		return new ProbesDataStore(pdsm);
	}

}
