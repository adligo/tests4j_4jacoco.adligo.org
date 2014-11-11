package org.adligo.tests4j_4jacoco.plugin.data.common;

import org.adligo.tests4j.models.shared.coverage.ClassProbesMutant;
import org.adligo.tests4j.models.shared.coverage.I_ClassProbes;
import org.adligo.tests4j.models.shared.coverage.I_ClassProbesMutant;
import org.adligo.tests4j.models.shared.coverage.Probes;
import org.adligo.tests4j.models.shared.coverage.SourceFileCoverageBriefMutant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ProbesDataStoreMutant implements I_ProbesDataStoreMutant {
	private Map<Long, ClassProbesMutant> classesToProbes_ = new HashMap<Long, ClassProbesMutant>();
	private Map<String,ClassProbesMutant> classNamesToProbes_ = new HashMap<String, ClassProbesMutant>();
	
	public ProbesDataStoreMutant() {}
	
	public ProbesDataStoreMutant(I_ProbesDataStore other) {
		Map<Long, I_ClassProbes> otherClassesToProbes = other.getAllCoverage();
		Set<Entry<Long, I_ClassProbes>> all = otherClassesToProbes.entrySet();
		for (Entry<Long, I_ClassProbes> e: all) {
			put(e.getKey(), e.getValue());
		}
	}
	@Override
	public Map<Long, I_ClassProbes> getAllCoverage() {
		Map<Long, I_ClassProbes> toRet = new HashMap<Long, I_ClassProbes>();
		Set<Entry<Long, ClassProbesMutant>> all = classesToProbes_.entrySet();
		for (Entry<Long, ClassProbesMutant> e: all) {
			toRet.put(e.getKey(), e.getValue());
		}
		return toRet;
	}
	@Override
	public I_ClassProbes get(long classId) {
		return classesToProbes_.get(classId);
	}
	@Override
	public Set<String> getAllClassNames() {
		return classNamesToProbes_.keySet();
	}
	
	@Override
	public boolean contains(String className) {
		return className.contains(className);
	}
	
	@Override
	public I_ClassProbesMutant getMutable(long classId) {
		return classesToProbes_.get(classId);
	}
	
	@Override
	public void put(long id, I_ClassProbes classProbes) {
	  ClassProbesMutant mut = new ClassProbesMutant(classProbes);
		classesToProbes_.put(id, mut);
		classNamesToProbes_.put(classProbes.getClassName(), mut);
	}
	
	@Override
	public void remove(long id) {
		I_ClassProbes cp = classesToProbes_.get(id);
		if (cp != null) {
			classNamesToProbes_.remove(cp.getClassName());
		}
		classesToProbes_.remove(id);
	}
	
	public void clear() {
	  classesToProbes_.clear();
	  classNamesToProbes_.clear();
	}

  @Override

  public SourceFileCoverageBriefMutant createBriefWithoutCUs(String sourceClassName) {
    SourceFileCoverageBriefMutant toRet = null;
    try {
      Class<?> c = Class.forName(sourceClassName);
      
      ClassProbesMutant cp = classNamesToProbes_.get(sourceClassName);
      if (cp == null) {
        if (c.isInterface()) {
          toRet = new SourceFileCoverageBriefMutant();
          toRet.setClassName(sourceClassName);
          toRet.setProbes(new Probes(new boolean[]{}));
          toRet.setCoverageUnits(0);
          toRet.setCoveredCoverageUnits(0);
          return toRet;
        } 
      }
      toRet = new SourceFileCoverageBriefMutant(cp);
      
      Class<?> [] innerClasses = c.getDeclaredClasses();
      List<ClassProbesMutant> innerClassProbes = new ArrayList<ClassProbesMutant>();
      for (int i = 0; i < innerClasses.length; i++) {
        ClassProbesMutant icpm = classNamesToProbes_.get(sourceClassName);
        innerClassProbes.add(icpm);
      }
    } catch (ClassNotFoundException x) {
      throw new RuntimeException(x);
    }
    return toRet;
  }
}
