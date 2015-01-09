package org.adligo.tests4j_4jacoco.plugin;

import org.adligo.tests4j.models.shared.coverage.CoverageUnits;
import org.adligo.tests4j.models.shared.coverage.I_ClassProbes;
import org.adligo.tests4j.models.shared.coverage.I_CoverageUnits;
import org.adligo.tests4j.models.shared.coverage.I_PackageCoverageBrief;
import org.adligo.tests4j.models.shared.coverage.I_SourceFileCoverage;
import org.adligo.tests4j.models.shared.coverage.I_SourceFileCoverageBrief;
import org.adligo.tests4j.models.shared.coverage.PackageCoverageBrief;
import org.adligo.tests4j.models.shared.coverage.PackageCoverageBriefMutant;
import org.adligo.tests4j.models.shared.coverage.SourceFileCoverageBrief;
import org.adligo.tests4j.models.shared.coverage.SourceFileCoverageBriefMutant;
import org.adligo.tests4j.run.discovery.I_PackageDiscovery;
import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;
import org.adligo.tests4j.system.shared.api.I_Tests4J_CoverageRecorder;
import org.adligo.tests4j_4jacoco.plugin.common.I_CoveragePluginMemory;
import org.adligo.tests4j_4jacoco.plugin.common.I_Runtime;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.ProbesDataStoreMutant;
import org.adligo.tests4j_4jacoco.plugin.data.coverage.LazyPackageCoverageFactory;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.CallJacocoInit;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ClassInstrumentationMetadata;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ClassInstrumentationMetadataStoreMutant;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Recorder implements I_Tests4J_CoverageRecorder {
	protected I_Tests4J_Log log_;
	protected I_CoveragePluginMemory memory_;
	private boolean main_ = false;
	private I_Runtime runtime_;
	private String threadGroupName_;
	private String filter_;
	private boolean started_ = false;
	
	public Recorder(I_CoveragePluginMemory memory, I_Tests4J_Log log) {
		memory_ = memory;
		log_ = log;
		runtime_ = memory_.getRuntime();
		main_ = true;
	}
	
	public Recorder(I_CoveragePluginMemory pMemory, I_Tests4J_Log pLog, 
	    String threadGroupName, String filter) {
    memory_ = pMemory;
    log_ = pLog;
    runtime_ = memory_.getRuntime();
    threadGroupName_ = threadGroupName;
    filter_ = filter;
    runtime_.putThreadGroupFilter(threadGroupName, filter);
    runtime_.clearClassesCovered(threadGroupName);
  }

	@Override
	public void startRecording() {
		if (log_ != null) {
			if (log_.isLogEnabled(Recorder.class)) {
				log_.log("Recorder starting " + main_);
			}
		}
		try {
			runtime_.startup();
			started_ = true;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

  protected void finishInstrumentation() {
    I_CachedClassBytesClassLoader mcl = memory_.getInstrumentedClassLoader();
    List<String> allClasses = mcl.getAllCachedClasses();
    int progress = 0;
    double nextProgressLog = 10.0;
    for (String clazz: allClasses) {
    	progress++;
    	double dp = 0.0 + progress;
    	double tot = 0.0 + allClasses.size();
    	
    	if (dp/tot >= nextProgressLog) {
    	  if (log_.isLogEnabled(Recorder.class)) {
      		nextProgressLog = nextProgressLog + 10.0;
      		DecimalFormat df = new DecimalFormat("###.#%");
      		log_.log("tests4j_4jacoco " + df.format(dp/tot) + "% calling $jacocoInit()s");
    	  }
    	}
    	if (clazz.indexOf("$") == -1) {
    		Class<?> loadedClass = mcl.getCachedClass(clazz);
    		if (log_.isLogEnabled(CallJacocoInit.class)) {
    		  log_.log("Recorder calling $jacocoInit() on " + clazz);
    		}
    		CallJacocoInit.callJacocoInit(loadedClass);
    	}
    }
  }
	
	@Override
	public List<I_PackageCoverageBrief> endRecording(Set<String> classNames) {
		if (log_ != null) {
			if (log_.isLogEnabled(Recorder.class)) {
				log_.log("Ending Recording " + Thread.currentThread().getName());
			}
		}
		I_ProbesDataStore executionData = runtime_.end(main_);
		
		return LazyPackageCoverageFactory.create(executionData, memory_, classNames);
	}

	public static String getPackageDir(final String name) {
		final String resource = '/' + name.replace('.', '/');
		int lastDot = resource.lastIndexOf("/");
		return resource.substring(0, lastDot);
	}
	
	public boolean isMain() {
		return main_;
	}

  @Override
  public I_SourceFileCoverageBrief getSourceFileCoverage() {
    I_SourceFileCoverageBrief probes = runtime_.getSourceFileCoverage(threadGroupName_, filter_);
    ProbesDataStoreMutant mut = new ProbesDataStoreMutant();
    Set<String> classNames = new HashSet<String>();
    mut.put(probes.getClassId(), probes);
    List<I_ClassProbes> cps = probes.getClassProbes();
    for (I_ClassProbes cp: cps) {
      mut.put(cp.getClassId(), cp);
      classNames.add(cp.getClassName());
    }
    I_SourceFileCoverage sfc;
    try {
      sfc = LazyPackageCoverageFactory.createSourceFileCoverage(
          mut, memory_, filter_, classNames);
      I_CoverageUnits cus = sfc.getCoverageUnits();
      
      SourceFileCoverageBriefMutant sfpm = new SourceFileCoverageBriefMutant(probes);
      sfpm.setCoverageUnits(cus.get());
      I_CoverageUnits ccus = sfc.getCoveredCoverageUnits();
      sfpm.setCoveredCoverageUnits(ccus.get());
      return new SourceFileCoverageBrief(sfpm);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public I_PackageCoverageBrief getPackageCoverage() {
    return null;
  }

  @Override
  public List<I_PackageCoverageBrief> getAllCoverage(Set<String> trialPackages) {
    
    Set<String> topNames = memory_.getTopPackageNames();
    if (log_.isLogEnabled(Recorder.class)) {
      if (log_.isMainLog()) {
        log_.log("getAllCoverage with top pakage names " + topNames);
      }
    }
    Map<String,PackageCoverageBriefMutant> pkgBriefs = new HashMap<String,PackageCoverageBriefMutant>();
    Map<String,Map<String,SourceFileCoverageBriefMutant>> packagesToSourceClasses = 
        new HashMap<String,Map<String,SourceFileCoverageBriefMutant>>();
    
    Set<String> topNamesWithClasses = new HashSet<String>();
    for (String topName: topNames) {
      I_PackageDiscovery pd = memory_.getPackage(topName);
      
      PackageCoverageBriefMutant pm = new PackageCoverageBriefMutant();
      pm.setPackageName(topName);
      recurseIntoChildren(packagesToSourceClasses, pm, pd, topNamesWithClasses, pkgBriefs);
      pkgBriefs.put(topName, pm);
    }
    //quick fix for trials of references like  
    // org.adligo.tests4j_v1_tests.gwt_refs.v2_6.annotation
    topNamesWithClasses.removeAll(trialPackages);
    
    List<I_PackageCoverageBrief> toRet = new ArrayList<I_PackageCoverageBrief>();
    for (String pkg: topNamesWithClasses) {
      PackageCoverageBriefMutant pm = pkgBriefs.get(pkg);
      recurseChildPackages(pm, packagesToSourceClasses);
      PackageCoverageBrief pcb = new PackageCoverageBrief(pm);
      toRet.add(pcb);
      if (log_.isLogEnabled(Recorder.class)) {
        if (log_.isMainLog()) {
          log_.log("getAllCoverage returning " + pcb);
        }
      }
    }
    if (log_.isLogEnabled(Recorder.class)) {
      log_.log("returning package coverages " + toRet);
    }
    
    return toRet;
  }

  private void recurseIntoChildren(
      Map<String, Map<String, SourceFileCoverageBriefMutant>> packagesToSourceClasses,
      PackageCoverageBriefMutant parent, I_PackageDiscovery pd, Set<String> topNamesWithClasses,
      Map<String,PackageCoverageBriefMutant> pkgBriefs) {
    
    I_ClassInstrumentationMetadataStoreMutant store = memory_.getClassInstrumentationInfoStore();
    
    String packageName = parent.getPackageName();
    List<String> classNames = pd.getClassNames();
    Map<String,SourceFileCoverageBriefMutant> classBriefs = new HashMap<String, SourceFileCoverageBriefMutant>();
    for (String clazz: classNames) {
      I_ClassInstrumentationMetadata info = store.getClassInstrumentation(clazz);
      if (info == null) {
        if (log_.isLogEnabled(Recorder.class)) {
          log_.log("No instrumentation info found for class " + System.lineSeparator() +
            clazz);
        }
      } else {
        runtime_.ensureProbesInitialized(info);
        I_SourceFileCoverageBrief brief = runtime_.getSourceFileCoverage(clazz);
        classBriefs.put(clazz, new SourceFileCoverageBriefMutant(brief));
      }
    }
    if (classNames.size() >= 1) {
      boolean add = true;
      //check if this package already has a parent
      for (String pkgName: topNamesWithClasses) {
        if (packageName.indexOf(pkgName) == 0) {
          add = false;
        }
      }
      if (add) {
        topNamesWithClasses.add(packageName);
      }
    }
    packagesToSourceClasses.put(packageName, classBriefs);
    parent.setSourceFiles(classBriefs);
    
    List<I_PackageDiscovery> subs = pd.getSubPackages();
    for (I_PackageDiscovery sub: subs) {
      String subPackageName = sub.getPackageName();
      PackageCoverageBriefMutant child = new PackageCoverageBriefMutant();
      child.setPackageName(subPackageName);
      recurseIntoChildren(packagesToSourceClasses, child, sub, topNamesWithClasses,pkgBriefs);
      parent.addChild(child);
    }
    pkgBriefs.put(packageName, parent);
  }

  
  /**
   * must return the coverage units (covered[0], all[1])
   * 
   * @param pm
   * @param packagesToSourceClasses
   * @return
   */
  private CoverageUnits[] recurseChildPackages(PackageCoverageBriefMutant pm, 
      Map<String,Map<String,SourceFileCoverageBriefMutant>> packagesToSourceClasses) {
      
    String pkgName = pm.getPackageName();
    Map<String,SourceFileCoverageBriefMutant> sourceBriefs = packagesToSourceClasses.get(pkgName);
    pm.setSourceFiles(sourceBriefs);
    
    BigInteger covered = new BigInteger("0");
    BigInteger all = new BigInteger("0");
    
    List<PackageCoverageBriefMutant> children =  pm.getChildren();
    for (PackageCoverageBriefMutant child: children) {
      CoverageUnits[] results = recurseChildPackages(child, packagesToSourceClasses);
      covered = covered.add(results[0].getBig());
      all = all.add(results[1].getBig());
    }
    if (sourceBriefs != null) {
      Collection<SourceFileCoverageBriefMutant> sourceFiles = sourceBriefs.values();
      for (SourceFileCoverageBriefMutant source: sourceFiles) {
        int coveredSource = source.getCoveredCoverageUnits();
        covered = covered.add(new BigInteger("" + coveredSource));
        int allSource = source.getCoverageUnits();
        all = all.add(new BigInteger("" + allSource));
      }
    }
    CoverageUnits coveredUnits = new CoverageUnits(covered);
    CoverageUnits allUnits = new CoverageUnits(all);
    pm.setCoveredCoverageUnits(coveredUnits);
    pm.setCoverageUnits(allUnits);
    return new CoverageUnits[] {coveredUnits, allUnits};
  }

  public boolean isStarted() {
    return started_;
  }
}