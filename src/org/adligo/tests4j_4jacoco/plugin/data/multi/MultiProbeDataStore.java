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
	private CoverageRecorderStates coverageRecorderStates = 
			new CoverageRecorderStates();
	private Map<RecorderProbesId,boolean[]> probes = 
			new ConcurrentHashMap<RecorderProbesId, boolean[]>();
			
	@Override
	public synchronized Map<Integer, Boolean> get(Long id, String name, int probecount) {
		List<String> currentRecordingScopes = coverageRecorderStates.getCurrentRecordingScopes();
		boolean[][] probeData = new boolean[currentRecordingScopes.size()][];
		
		for (int i = 0; i < currentRecordingScopes.size(); i++) {
			String scope = currentRecordingScopes.get(i);
			RecorderProbesId recId = new RecorderProbesId(id, scope, name);
			boolean [] recProbes = probes.get(recId);
			if (recProbes == null) {
				synchronized (probes) {
					//note this looks like the double check locking anti pattern
					// but since it checks for non nulls in the map, I believe
					// it will work correctly, since it obvoids the 
					// issue with double check locking
					recProbes = probes.get(recId);
					if (recProbes == null) {
						recProbes = new boolean[probecount];
						probes.put(recId, recProbes);
					}
				}
			}
			probeData[i] = recProbes;
		}
		return new MultiProbesMap(probeData);
	}

	@Override
	public void startRecording(String scope) {
		coverageRecorderStates.setRecording(scope, true);
	}

	@Override
	public void pauseRecording(String scope) {
		coverageRecorderStates.setRecording(scope, false);
	}

	@Override
	public synchronized I_ProbesDataStore endRecording(String scope) {
		coverageRecorderStates.setRecording(scope, false);
		ProbesDataStoreMutant pdsm = new ProbesDataStoreMutant();
		Set<Entry<RecorderProbesId, boolean[]>> entries =  probes.entrySet();
		Iterator<Entry<RecorderProbesId, boolean[]>> it =  entries.iterator();
		while (it.hasNext()) {
			Entry<RecorderProbesId, boolean[]> entry = it.next();
			RecorderProbesId rpid = entry.getKey();
			if (scope.equals(rpid.getScope())) {
				it.remove();
				boolean [] probeVals = entry.getValue();
				ClassProbesMutant cpm = new ClassProbesMutant();
				cpm.setClassId(rpid.getClassId());
				cpm.setClassName(rpid.getClassName());
				cpm.setProbes(new Probes(probeVals));
				pdsm.put(rpid.getClassId(), new ClassProbes(cpm));
			}
		}
		return new ProbesDataStore(pdsm);
	}

}
