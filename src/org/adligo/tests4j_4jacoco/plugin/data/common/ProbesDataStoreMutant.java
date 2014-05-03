package org.adligo.tests4j_4jacoco.plugin.data.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ProbesDataStoreMutant implements I_ProbesDataStoreMutant {
	private Map<Long, ClassProbesMutant> classesToProbes = new HashMap<Long, ClassProbesMutant>();
	private Set<String> classNames = new HashSet<String>();
	
	public ProbesDataStoreMutant() {}
	
	public ProbesDataStoreMutant(I_ProbesDataStore other) {
		Map<Long, I_ClassProbes> otherClassesToProbes = other.getAllCoverage();
		Set<Entry<Long, I_ClassProbes>> all = otherClassesToProbes.entrySet();
		for (Entry<Long, I_ClassProbes> e: all) {
			put(e.getKey(), e.getValue());
		}
		classNames.addAll(other.getAllClassNames());
	}
	@Override
	public Map<Long, I_ClassProbes> getAllCoverage() {
		Map<Long, I_ClassProbes> toRet = new HashMap<Long, I_ClassProbes>();
		Set<Entry<Long, ClassProbesMutant>> all = classesToProbes.entrySet();
		for (Entry<Long, ClassProbesMutant> e: all) {
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
		return classesToProbes.get(classId);
	}
	
	@Override
	public void put(long id, I_ClassProbes classProbes) {
		classesToProbes.put(id, new ClassProbesMutant(classProbes));
		classNames.add(classProbes.getClassName());
	}
	
	@Override
	public void remove(long id) {
		I_ClassProbes cp = classesToProbes.get(id);
		if (cp != null) {
			classNames.remove(cp.getClassName());
		}
		classesToProbes.remove(id);
	}
	
}
