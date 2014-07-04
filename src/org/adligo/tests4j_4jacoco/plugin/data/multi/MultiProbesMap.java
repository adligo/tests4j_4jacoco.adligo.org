package org.adligo.tests4j_4jacoco.plugin.data.multi;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.adligo.tests4j_4jacoco.plugin.data.common.I_CoverageRecoderStates;

/**
 * a wrapper around probe data for different recorders.
 * mutates the boolean [][] passed in with put and remove only.
 * clear removes the reference to the probeData, for quicker 
 * garbage collection.
 * 
 * @author scott
 *
 */
public class MultiProbesMap implements Map<Integer, Boolean>{
	/**
	 * each entry in the list pertains to a different recorder
	 */
	private Map<String,boolean[]> scopesToProbes = new ConcurrentHashMap<String, boolean[]>();
	private I_CoverageRecoderStates states;
	private String clazzCovered;
	private int probeCount;
	
	public MultiProbesMap(I_CoverageRecoderStates pStates, String pClazzToCover, int pProbeCount) {
		states = pStates;
		if (pStates == null) {
			throw new NullPointerException("pStates can't be null!");
		}
		clazzCovered = pClazzToCover;
		if (pClazzToCover == null) {
			throw new NullPointerException("pClazzToCover can't be null!");
		}
		probeCount = pProbeCount;
	}

	@Override
	public int size() {
		throw new IllegalStateException("Method not implemented");
	}

	@Override
	public boolean isEmpty() {
		throw new IllegalStateException("Method not implemented");
	}

	@Override
	public boolean containsKey(Object key) {
		throw new IllegalStateException("Method not implemented");
	}

	@Override
	public boolean containsValue(Object value) {
		throw new IllegalStateException("Method not implemented");
	}

	@Override
	public Boolean get(Object key) {
		throw new IllegalStateException("Method not implemented");
	}

	@Override
	public synchronized Boolean put(Integer key, Boolean value) {
		if (key == null || value == null) {
			return false;
		}
		if (!value) {
			return false;
		}
		int keyInt = key.intValue();
		if (keyInt < 0) {
			return false;
		}
		List<String> activeScopes = states.getCurrentRecordingScopes();
		for (String scope: activeScopes) {
			boolean [] probes = scopesToProbes.get(scope);
			if (!scopesToProbes.containsKey(scope)) {
				synchronized (scopesToProbes) {
					if (!scopesToProbes.containsKey(scope)) {
						probes = new boolean[probeCount];
						for (int i = 0; i < probes.length; i++) {
							probes[i] = false;
						}
						scopesToProbes.put(scope, probes);
					} else {
						probes = scopesToProbes.get(scope);
					}
				}
			}
			
			if (keyInt < probes.length) {
				probes[keyInt] = value;
			}
		}
		return true;
	}

	@Override
	public Boolean remove(Object key) {
		throw new IllegalStateException("Method not implemented");
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends Boolean> m) {
		throw new IllegalStateException("Method not implemented");
	}

	@Override
	public void clear() {
		throw new IllegalStateException("Method not implemented");
	}

	@Override
	public Set<Integer> keySet() {
		throw new IllegalStateException("Method not implemented");
	}

	@Override
	public Collection<Boolean> values() {
		throw new IllegalStateException("Method not implemented");
	}

	@Override
	public Set<Entry<Integer, Boolean>> entrySet() {
		throw new IllegalStateException("Method not implemented");
	}
	
	public synchronized void releaseRecording(String scope) {
		scopesToProbes.remove(scope);
	}

	public String getClazzCovered() {
		return clazzCovered;
	}
	
	public synchronized boolean[] getProbes(String scope) {
		boolean [] toRet = scopesToProbes.get(scope);
		if (toRet == null) {
			toRet = new boolean[probeCount];
		}
		return toRet;
	}
}
