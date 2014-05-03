package org.adligo.tests4j_4jacoco.plugin.data.multi;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

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
	private boolean [][] probeData;
	
	public MultiProbesMap(boolean[][] pProbeData) {
		probeData = pProbeData;
		if (pProbeData == null) {
			throw new NullPointerException("pProbeData can't be null!");
		}
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
	public Boolean put(Integer key, Boolean value) {
		if (key == null || value == null) {
			return false;
		}
		int keyInt = key.intValue();
		if (keyInt < 0) {
			return false;
		}
		for (int i = 0; i < probeData.length; i++) {
			boolean [] probes = probeData[i];
			if (keyInt < probes.length) {
				probes[keyInt] = value;
			}
		}
		return true;
	}

	@Override
	public Boolean remove(Object key) {
		if ( !(key instanceof Integer)) {
			return false;
		}
		int keyInt = ((Integer) key).intValue();
		if (keyInt < 0) {
			return false;
		}
		for (int i = 0; i < probeData.length; i++) {
			boolean [] probes = probeData[i];
			if (keyInt < probes.length) {
				probes[keyInt] = false;
			}
		}
		return true;
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends Boolean> m) {
		throw new IllegalStateException("Method not implemented");
	}

	@Override
	public void clear() {
		probeData = null;
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
}
