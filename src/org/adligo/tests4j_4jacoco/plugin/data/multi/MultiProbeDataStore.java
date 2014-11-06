package org.adligo.tests4j_4jacoco.plugin.data.multi;

import org.adligo.tests4j.models.shared.coverage.ClassProbes;
import org.adligo.tests4j.models.shared.coverage.ClassProbesMutant;
import org.adligo.tests4j.models.shared.coverage.Probes;
import org.adligo.tests4j.run.common.ConcurrentQualifiedMap;
import org.adligo.tests4j.run.helpers.ThreadLogMessageBuilder;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;
import org.adligo.tests4j.system.shared.api.I_Tests4J_CoverageRecorder;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_MultiRecordingProbeDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.ProbesDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.ProbesDataStoreMutant;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents a in memory data store for probes
 * which can be used for multiple {@link I_Tests4J_CoverageRecorder}'s
 * at the same time.
 * 
 * @author scott
 *
 */
public class MultiProbeDataStore implements I_MultiRecordingProbeDataStore {
	private final ConcurrentQualifiedMap<Long, MultiProbesMap> classIds_;

	private final I_Tests4J_Log log_;
	
	public MultiProbeDataStore(I_Tests4J_Log p) {
		this(p, null);
	}
	
	public MultiProbeDataStore(I_Tests4J_Log p, ConcurrentQualifiedMap<Long, MultiProbesMap> map) {
    log_ = p;
    if (map != null) {
      classIds_ = map;
    } else {
      classIds_ = new ConcurrentQualifiedMap<Long, MultiProbesMap>(
          new ConcurrentHashMap<Long, MultiProbesMap>());
    }
  }
	
	@Override
	public Map<Integer, Boolean> get(final Long id, final String name, final int probecount) {
		if (log_.isLogEnabled(MultiProbeDataStore.class)) {
			log_.log(ThreadLogMessageBuilder.getThreadWithGroupNameForLog() +
					"\n is getting probes for class " + name);
		}
		MultiProbesMap toRet = classIds_.get(id);
		if (toRet == null) {
			if (!classIds_.containsKey(id)) {
			  classIds_.putIfAbsent(id, 
				    new MultiProbesMap(name, probecount, log_));
			} 
		  //this may block until the id shows up in the map,
			//this could be from another thread
      toRet =  classIds_.obtain(id);
		} 
		return toRet;
	}

	@Override
	public void startRecording() {
	}

	@Override
	public synchronized I_ProbesDataStore getRecordedProbes(boolean main) {
		ProbesDataStoreMutant pdsm = new ProbesDataStoreMutant();
		
		//spin through a snapshot, of all threads
		Set<Entry<Long, MultiProbesMap>> entries =  new HashSet<Entry<Long, MultiProbesMap>>(
		    classIds_.entrySet());
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
			
		}

		return new ProbesDataStore(pdsm);
	}

}
