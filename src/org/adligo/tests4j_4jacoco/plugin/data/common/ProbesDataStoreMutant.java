package org.adligo.tests4j_4jacoco.plugin.data.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ProbesDataStoreMutant implements I_ProbesDataStoreMutant {
	private Map<Long, ClassProbes> classesToProbes = new HashMap<Long, ClassProbes>();
	private Set<String> classNames = new HashSet<String>();
	@Override
	public Map<Long, I_ClassProbes> getAllCoverage() {
		Map<Long, I_ClassProbes> toRet = new HashMap<Long, I_ClassProbes>();
		Set<Entry<Long, ClassProbes>> all = classesToProbes.entrySet();
		for (Entry<Long, ClassProbes> e: all) {
			toRet.put(e.getKey(), e.getValue());
		}
		return toRet;
	}
	@Override
	public I_ClassProbes get(long classId) {
		return classesToProbes.get(classId);
	}
	@Override
	public Set<String> getAllClassNames() {
		return classNames;
	}
	
	@Override
	public boolean contains(String className) {
		return className.contains(className);
	}
	
	@Override
	public I_ClassProbesMutant getMutable(long classId) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
