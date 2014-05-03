package org.adligo.tests4j_4jacoco.plugin.data.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *  a temp class
 *  to prove that swapping out the boolean[]
 *  with a Map will work!
 *  
 * @author scott
 *
 */
public class SimpleProbesMap implements Map<Integer, Boolean> {
	private boolean [] probeData;
	
	public SimpleProbesMap(int size) {
		probeData = new boolean[size];
		for (int i = 0; i < size; i++) {
			probeData[i] = false;
		}
	}
	
	public SimpleProbesMap(boolean [] input) {
		probeData = input;
	}
	
	public SimpleProbesMap(Map<Integer, Boolean> input) {
		putAll(input);
	}

	@Override
	public int size() {
		return probeData.length;
	}

	@Override
	public boolean isEmpty() {
		if (probeData.length == 0) {
			return true;
		}
		return false;
	}

	@Override
	public boolean containsKey(Object key) {
		try {
			int keyInt = (Integer) key;
			if (keyInt >= 0 && keyInt < probeData.length) {
				return true;
			}
		} catch (ClassCastException x) {
			//do nothing
		}
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		try {
			boolean valBool = (Boolean) value;
			for (int i = 0; i < probeData.length; i++) {
				boolean b = probeData[i];
				if (b == valBool) {
					return true;
				}
			}
		} catch (ClassCastException x) {
			//do nothing
		}
		return false;
	}

	@Override
	public Boolean get(Object key) {
		try {
			int keyInt = (Integer) key;
			if (keyInt >= 0 && keyInt < probeData.length) {
				return probeData[keyInt];
			}
		} catch (ClassCastException x) {
			//do nothing
		}
		return null;
	}

	@Override
	public Boolean put(Integer key, Boolean value) {
		probeData[key] = value;
		return true;
	}

	@Override
	public Boolean remove(Object key) {
		int keyInt = (Integer) key;
		probeData[keyInt] = false;
		return true;
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends Boolean> m) {
		boolean [] probesData = new boolean[m.size()];
		Set<?> entrySet = m.entrySet();
		
		for (Object entryObj: entrySet) {
			//was having a compile issue in eclipse ???
			@SuppressWarnings("unchecked")
			Entry<Integer,Boolean> entry = (Entry<Integer,Boolean>) entryObj;
			Integer id = entry.getKey();
			Boolean value = entry.getValue();
			probesData[id] = value;
		}
	}

	@Override
	public void clear() {
		//do nothing in this impl, as the data here is persistent
		//and clear is called to help garbage collection
		// in the multi recorder
	}

	@Override
	public Set<Integer> keySet() {
		Set<Integer> toRet = new HashSet<Integer>();
		for (int i = 0; i < probeData.length; i++) {
			toRet.add(i);
		}
		return toRet;
	}

	@Override
	public Collection<Boolean> values() {
		List<Boolean> toRet = new ArrayList<Boolean>();
		for (int i = 0; i < probeData.length; i++) {
			toRet.add(probeData[i]);
		}
		return toRet;
	}

	@Override
	public Set<Entry<Integer, Boolean>> entrySet() {
		Map<Integer,Boolean> toRet = new HashMap<Integer, Boolean>();
		for (int i = 0; i < probeData.length; i++) {
			toRet.put(i, probeData[i]);
		}
		return toRet.entrySet();
	}
	
	/**
	 * creates a copy of the probeData boolean[]
	 * for encapsulation.
	 * @return
	 */
	public boolean[] toArray() {
		return probeData;
	}
	
	/**
	 * sets everything to false
	 */
	public void reset() {
		for (int i = 0; i < probeData.length; i++) {
			probeData[i] = false;
		}
	}
}
