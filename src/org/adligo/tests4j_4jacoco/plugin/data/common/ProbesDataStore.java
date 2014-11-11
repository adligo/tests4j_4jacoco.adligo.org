package org.adligo.tests4j_4jacoco.plugin.data.common;

import org.adligo.tests4j.models.shared.coverage.ClassProbes;
import org.adligo.tests4j.models.shared.coverage.I_ClassProbes;
import org.adligo.tests4j.models.shared.coverage.Probes;
import org.adligo.tests4j.models.shared.coverage.SourceFileCoverageBriefMutant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * immutable probe data store
 * @author scott
 *
 */
public class ProbesDataStore implements I_ProbesDataStore {
	private Map<Long, ClassProbes> classesToProbes_ = new HashMap<Long, ClassProbes>();
	private Map<String, ClassProbes> classNamesToProbes_ = new HashMap<String, ClassProbes>();
	
	public ProbesDataStore() {}
	
	public ProbesDataStore(I_ProbesDataStore other) {
		Map<Long, I_ClassProbes> otherClassesToProbes = other.getAllCoverage();
		Set<Entry<Long, I_ClassProbes>> all = otherClassesToProbes.entrySet();
		for (Entry<Long, I_ClassProbes> e: all) {
		  ClassProbes probes = new ClassProbes(e.getValue());
			classesToProbes_.put(e.getKey(), probes);
			classesToProbes_.put(e.getKey(), probes);
		}
	}

	@Override
	public Map<Long, I_ClassProbes> getAllCoverage() {
		Map<Long, I_ClassProbes> toRet = new HashMap<Long, I_ClassProbes>();
		Set<Entry<Long, ClassProbes>> all = classesToProbes_.entrySet();
		for (Entry<Long, ClassProbes> e: all) {
			toRet.put(e.getKey(), e.getValue());
		}
		return Collections.unmodifiableMap(toRet);
	}

	@SuppressWarnings("boxing")
  @Override
	public I_ClassProbes get(long classId) {
		return classesToProbes_.get(classId);
	}

	@Override
	public Set<String> getAllClassNames() {
		return Collections.unmodifiableSet(classNamesToProbes_.keySet());
	}

	@Override
	public boolean contains(String className) {
		return className.contains(className);
	}

	public SourceFileCoverageBriefMutant createBriefWithoutCUs(String sourceClassName) {
	  ClassProbes cp = classNamesToProbes_.get(sourceClassName);
    if (cp == null) {
      SourceFileCoverageBriefMutant toRet = new SourceFileCoverageBriefMutant();
      toRet.setClassName(sourceClassName);
      toRet.setProbes(new Probes(new boolean[] {}));
      toRet.setCoverageUnits(0);
      toRet.setCoveredCoverageUnits(0);
      return toRet;
    }
    SourceFileCoverageBriefMutant toRet = new SourceFileCoverageBriefMutant(cp);
    try {
      Class<?> c = Class.forName(sourceClassName);
      Class<?> [] innerClasses = c.getDeclaredClasses();
      List<ClassProbes> innerClassProbes = new ArrayList<ClassProbes>();
      for (int i = 0; i < innerClasses.length; i++) {
        ClassProbes icpm = classNamesToProbes_.get(sourceClassName);
        innerClassProbes.add(icpm);
      }
    } catch (ClassNotFoundException x) {
      throw new RuntimeException(x);
    }
    return toRet;
	}
}
