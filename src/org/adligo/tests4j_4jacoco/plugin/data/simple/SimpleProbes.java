package org.adligo.tests4j_4jacoco.plugin.data.simple;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class SimpleProbes  {
	
	public static boolean[] toArray(Map<Integer,Boolean> probes) {
		boolean [] toRet = new boolean[probes.size()];
		Set<Entry<Integer,Boolean>> entrySet = probes.entrySet();
		
		for (Entry<Integer, Boolean> entry: entrySet) {
			Integer id = entry.getKey();
			Boolean value = entry.getValue();
			toRet[id] = value;
		}
		return toRet;
	}
	
	public static Map<Integer,Boolean> toArray(boolean [] probes) {
		Map<Integer,Boolean> toRet = new HashMap<Integer, Boolean>();
		for (int i = 0; i < probes.length; i++) {
			toRet.put(i, probes[i]);
		}
		return toRet;
	}
}
