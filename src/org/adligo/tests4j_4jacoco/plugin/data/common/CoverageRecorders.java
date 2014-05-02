package org.adligo.tests4j_4jacoco.plugin.data.common;

import java.util.Map;
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
public class CoverageRecorders {
	/**
	 * key String is the scope passed to @see {@link I_CoveragePlugin#createRecorder(String)}
	 * value AtomicBoolean is the recorder state, 
	 * 		true is recording 
	 *      false is not recording 
	 */
	private final Map<String, AtomicBoolean> recorders = 
			new ConcurrentHashMap<>();
			
	public void add(String scope, boolean on) {
		AtomicBoolean onOff = recorders.get(scope);
		if (onOff == null) {
			recorders.put(scope, new AtomicBoolean(on));
		}
	}
	
	public boolean isRecording(String scope) {
		AtomicBoolean onOff = recorders.get(scope);
		if (onOff != null) {
			return onOff.get();
		}
		return false;
	}
	
}
