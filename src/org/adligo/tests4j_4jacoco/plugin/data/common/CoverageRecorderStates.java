package org.adligo.tests4j_4jacoco.plugin.data.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.adligo.tests4j.models.shared.system.I_CoveragePlugin;

/**
 * this class represents the state 
 * one of  (recording or not recording) of various recorders
 * obtained from a I_CoveragePlugin.
 * @author scott
 *
 */
public class CoverageRecorderStates implements I_CoverageRecoderStates {
	/**
	 * key String is the scope passed to @see {@link I_CoveragePlugin#createRecorder(String)}
	 * value AtomicBoolean is the recorder state, 
	 * 		true is recording 
	 *      false is not recording 
	 */
	private final Map<String, AtomicBoolean> recorders = 
			new ConcurrentHashMap<String, AtomicBoolean>();
			
	public void setRecording(String scope, boolean on) {
		AtomicBoolean onOff = recorders.get(scope);
		if (onOff == null) {
			synchronized (recorders) {
				if (!recorders.containsKey(scope)) {
					recorders.put(scope, new AtomicBoolean(on));
				}
			}
		} else {
			onOff.set(on);
		}
	}
	
	public  boolean isRecording(String scope) {
		AtomicBoolean onOff = recorders.get(scope);
		if (onOff != null) {
			return onOff.get();
		}
		return false;
	}
	
	public  List<String> getCurrentRecordingScopes() {
		List<String> toRet = new ArrayList<String>();
		
		Set<Entry<String, AtomicBoolean>> entries = recorders.entrySet();
		Iterator<Entry<String, AtomicBoolean>> it =  entries.iterator();
		while (it.hasNext()) {
			Entry<String, AtomicBoolean> entry = it.next();
			AtomicBoolean ab =  entry.getValue();
			if (ab.get()) {
				toRet.add(entry.getKey());
			}
		}
		return toRet;
	}

}
