package org.adligo.tests4j_4jacoco.plugin.data.common;

import org.adligo.tests4j.models.shared.coverage.ClassProbes;
import org.adligo.tests4j.models.shared.coverage.I_ClassProbes;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * immutable probe data store
 * @author scott
 *
 */
public class ProbesDataStore implements I_ProbesDataStore {
	private Map<Long, ClassProbes> classesToProbes = new HashMap<Long, ClassProbes>();
	private Set<String> classNames = new HashSet<String>();
	
	public ProbesDataStore() {}
	
	public ProbesDataStore(I_ProbesDataStore other) {
		Map<Long, I_ClassProbes> otherClassesToProbes = other.getAllCoverage();
		Set<Entry<Long, I_ClassProbes>> all = otherClassesToProbes.entrySet();
		for (Entry<Long, I_ClassProbes> e: all) {
			classesToProbes.put(e.getKey(), new ClassProbes(e.getValue()));
		}
		classNames.addAll(other.getAllClassNames());
	}

	@Override
	public Map<Long, I_ClassProbes> getAllCoverage() {
		Map<Long, I_ClassProbes> toRet = new HashMap<Long, I_ClassProbes>();
		Set<Entry<Long, ClassProbes>> all = classesToProbes.entrySet();
		for (Entry<Long, ClassProbes> e: all) {
			toRet.put(e.getKey(), e.getValue());
		}
		return Collections.unmodifiableMap(toRet);
	}

	@Override
	public I_ClassProbes get(long classId) {
		return classesToProbes.get(classId);
	}

	@Override
	public Set<String> getAllClassNames() {
		return Collections.unmodifiableSet(classNames);
	}

	@Override
	public boolean contains(String className) {
		return className.contains(className);
	}
}
