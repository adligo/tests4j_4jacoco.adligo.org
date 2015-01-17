package org.adligo.tests4j_4jacoco.plugin.data.multi;

import org.adligo.tests4j.models.shared.coverage.ClassProbes;
import org.adligo.tests4j.models.shared.coverage.ClassProbesMutant;
import org.adligo.tests4j.models.shared.coverage.I_SourceFileCoverageBrief;
import org.adligo.tests4j.models.shared.coverage.Probes;
import org.adligo.tests4j.models.shared.coverage.ProbesMutant;
import org.adligo.tests4j.models.shared.coverage.SourceFileCoverageBrief;
import org.adligo.tests4j.models.shared.coverage.SourceFileCoverageBriefMutant;
import org.adligo.tests4j.run.common.ConcurrentQualifiedMap;
import org.adligo.tests4j.shared.common.ClassMethods;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;
import org.adligo.tests4j.system.shared.api.I_Tests4J_CoverageRecorder;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_MultiRecordingProbeDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.ProbesDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.ProbesDataStoreMutant;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ClassInstrumentationMetadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This class represents a in memory data store for probes
 * which can be used for multiple {@link I_Tests4J_CoverageRecorder}'s
 * at the same time.
 * 
 * @author scott
 *
 */
public class MultiProbeDataStore implements I_MultiRecordingProbeDataStore {
	private final ConcurrentQualifiedMap<Long, MultiProbesMap> classIds_;
	/**
	 * The regular java class names (not type names) to regular java class names;
	 */
	private final ConcurrentHashMap<String,CopyOnWriteArraySet<String>> parentChildClasses_;
	/**
	 * The regular java class names (not type names) to ids
	 */
	private final ConcurrentHashMap<String,Long> classNamesToIds_;
	private final MultiContext ctx_;
	private final I_Tests4J_Log log_;
	private final I_MultiProbesMapConverter threadLocalConverter_ = new I_MultiProbesMapConverter() {
    
    @Override
    public boolean[] getProbes(MultiProbesMap probes) {
      return probes.getThreadGroupProbes();
    }
  };
  private final I_MultiProbesMapConverter converter_ = new I_MultiProbesMapConverter() {
    
    @Override
    public boolean[] getProbes(MultiProbesMap probes) {
      return probes.getProbes();
    }
  };
  
	public MultiProbeDataStore(MultiContext ctx) {
		this(ctx, null);
	}
	
	public MultiProbeDataStore(MultiContext ctx, ConcurrentQualifiedMap<Long, MultiProbesMap> map) {
    log_ = ctx.getLog();
    ctx_ = ctx;
    classIds_ = ctx_.getMultiProbesMap();
    classNamesToIds_ = new ConcurrentHashMap<String, Long>();
    parentChildClasses_ = new ConcurrentHashMap<String, CopyOnWriteArraySet<String>>();
  }
	
	@Override
	public Map<Integer, Boolean> get(final Long id, final String name, final int probecount) {
		if (log_.isLogEnabled(MultiProbeDataStore.class)) {
			log_.log(log_.getThreadWithGroupNameMessage() + log_.lineSeparator() +
					"\tis getting probes for the following class;" + log_.lineSeparator() + 
					"\t" + name);
		}
		MultiProbesMap toRet = classIds_.get(id);
		if (toRet == null) {
			if (!classIds_.containsKey(id)) {
			  if (log_.isLogEnabled(MultiProbeDataStore.class)) {
		      log_.log(log_.getThreadWithGroupNameMessage() + log_.lineSeparator() +
		          "\tis initalizing probes for the follwing class;" + log_.lineSeparator() + 
		          "\t" + name + " " + id + log_.lineSeparator() + 
		          "\twith probes " + probecount);
		    }
			  ClassProbesMutant cpm = new ClassProbesMutant();
			  cpm.setClassId(id);
			  cpm.setClassName(name);
			  cpm.setProbes(new Probes(new boolean[probecount]));
			  Set<String> keys =  parentChildClasses_.keySet();
			  boolean found = false;
			  String regularName = ClassMethods.fromTypeDescription("L" + name + ";");

        if (log_.isLogEnabled(MultiProbeDataStore.class)) {
          log_.log(log_.getThreadWithGroupNameMessage() + log_.lineSeparator() +
              "\tis storing the following class;" + log_.lineSeparator() +
              "\t" + regularName + " " + id);
        }
        
			  for (String key: keys) {
			    if (regularName.indexOf(key) == 0) {
			      found = true;
			      Set<String> childClasses = parentChildClasses_.get(key);
			      childClasses.add(regularName);
			    }
			  }
			  if (!found) {
			    parentChildClasses_.put(name, new CopyOnWriteArraySet<String>());
			  }
			  
			  classNamesToIds_.putIfAbsent(regularName, id);
			  classIds_.putIfAbsent(id, 
				    new MultiProbesMap(cpm, ctx_));
			} 
		  //this may block until the id shows up in the map,
			//this could be from another thread
      toRet =  classIds_.obtain(id);
		} 
		return toRet;
	}

	@Override
	public void startRecording() {
	}

	@Override
	public synchronized I_ProbesDataStore getRecordedProbes(boolean main) {
		ProbesDataStoreMutant pdsm = new ProbesDataStoreMutant();
		
		//spin through a snapshot, of all threads
		Set<Entry<Long, MultiProbesMap>> entries =  new HashSet<Entry<Long, MultiProbesMap>>(
		    classIds_.entrySet());
		Iterator<Entry<Long, MultiProbesMap>> it =  entries.iterator();
		while (it.hasNext()) {
			Entry<Long, MultiProbesMap> entry = it.next();
			Long clazzId = entry.getKey();
			MultiProbesMap val = entry.getValue();
			
			boolean [] probeVals = null;
			if (main) {
				probeVals = val.getProbes();
			} else {
				probeVals = val.getThreadGroupProbes();
			}
			
			ClassProbesMutant cpm = new ClassProbesMutant();
			cpm.setClassId(clazzId);
			String classCovered = val.getClassCovered();

			cpm.setClassName(classCovered);
			cpm.setProbes(new Probes(probeVals));
			pdsm.put(clazzId, new ClassProbes(cpm));
			
		}

		return new ProbesDataStore(pdsm);
	}

	@SuppressWarnings("boxing")
  public I_SourceFileCoverageBrief getSourceFileProbes(String threadGroupName, 
	    String sourceFileClassName, Iterator<Long> classIds) {
	  
	  return getSourceFileBrief(sourceFileClassName, classIds,threadLocalConverter_);
	}

  public I_SourceFileCoverageBrief getSourceFileBrief(String sourceFileClassName,
      Iterator<Long> classIds, I_MultiProbesMapConverter converter) {
    String sourceFileName = ClassMethods.toResource(sourceFileClassName);
	  //remove first slash and .class
	  sourceFileName = sourceFileName.substring(1, sourceFileName.length() - 6);
	  SourceFileCoverageBriefMutant mut = new SourceFileCoverageBriefMutant();
    mut.setClassName(sourceFileClassName);
    if (classIds != null) {
      while (classIds.hasNext()) {
        Long l = classIds.next();
        MultiProbesMap multiProbes = classIds_.get(l);
        if (multiProbes != null) {
          String clazzCovered = multiProbes.getClassCovered();
          //double check the classCovered is part of the sourceFileClassName
          if (sourceFileName.indexOf(clazzCovered) == 0) {
            boolean [] probes = converter.getProbes(multiProbes);
            if (sourceFileName.equals(clazzCovered)) {
              ProbesMutant pm = new ProbesMutant(probes);
              mut.setProbes(pm);
              mut.setCoveredCoverageUnits(ProbesMutant.getCoveredCoverageUnits(probes));
              mut.setCoverageUnits(pm.size());
              mut.setClassId(l);
            } else {
              ClassProbesMutant cpm = new ClassProbesMutant();
              String clazzName = clazzCovered.replaceAll("/", ".");
              cpm.setClassName(clazzName);
              cpm.setClassId(l);
              cpm.setProbes(new ProbesMutant(probes));
              mut.setCoveredCoverageUnits(mut.getCoveredCoverageUnits() + ProbesMutant.getCoveredCoverageUnits(probes));
              mut.setCoverageUnits(mut.getCoverageUnits() + probes.length);
              mut.addClassProbe(cpm);
            }
          }
        }
      }
    } 
    if (mut.getProbes() == null) {
      Collection<MultiProbesMap> maps = classIds_.values();
      for (MultiProbesMap map: maps) {
        if (sourceFileName.equals(map.getClassCovered())) {
          mut.setProbes(new ProbesMutant(map.getProbes()));
          mut.setClassId(map.getClassId());
          break;
        }
      }
    }
    SourceFileCoverageBrief toRet = null;
    try {
      toRet = new SourceFileCoverageBrief(mut);
    } catch (Exception x) {
      throw new RuntimeException("There was a problem getting probes for class " + sourceFileClassName);
    }
    return toRet;
  }

  @SuppressWarnings("boxing")
  @Override
  public I_SourceFileCoverageBrief getSourceFileProbes(String sourceFileClassName) {
    Set<String> childClasses = parentChildClasses_.get(sourceFileClassName);
    
    List<Long> classIdList = new ArrayList<Long>();
    Long classId = classNamesToIds_.get(sourceFileClassName);
    if (classId == null) {
      if (classId == null) {
        throw new IllegalArgumentException("No classId found for class " + 
            sourceFileClassName + System.lineSeparator() +
            " was ensureProbesInitialized called?");
      }
    }
    classIdList.add(classId);
    
    if (childClasses != null) {
      for (String cc: childClasses) {
        classId = classNamesToIds_.get(cc);
        if (classId == null) {
          throw new IllegalArgumentException("No classId found for class " + 
              sourceFileClassName + System.lineSeparator() +
              " was ensureProbesInitialized called?");
        }
        classIdList.add(classId);
      }
    }
    MultiProbesMap mpm = classIds_.get(classId);
    String typeName = mpm.getClassCovered();
    Iterator<Long> classIds = classIdList.iterator();
    return getSourceFileBrief(typeName, classIds, converter_);
  }

  @SuppressWarnings("boxing")
  @Override
  public void ensureProbesInitialized(I_ClassInstrumentationMetadata info) {
    long classId = info.getId();
    
    if (!classIds_.containsKey(classId)) {
      String className = info.getClassTypeName();
      int probeCount = info.getProbeCount();
      get(classId, className, probeCount);
    }
  }
}
