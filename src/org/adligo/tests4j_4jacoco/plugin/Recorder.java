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
import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;
import org.adligo.tests4j.shared.common.ClassMethods;
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
import java.util.StringTokenizer;

public class Recorder implements I_Tests4J_CoverageRecorder {
	protected I_Tests4J_Log log_;
	protected I_CoveragePluginMemory memory_;
	private boolean main_ = false;
	private I_Runtime runtime_;
	private boolean jacocoInitOnFirstRecording_ = true;
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
  public List<I_PackageCoverageBrief> getAllCoverage() {
    I_CachedClassBytesClassLoader classLoader = memory_.getCachedClassLoader();
    List<String> classesCopy = new ArrayList<String>(classLoader.getAllCachedClasses());
    
    Set<String> sourceFiles = memory_.getAllSourceFileTrials();
    classesCopy.addAll(sourceFiles);
    
    Map<String,PackageCoverageBriefMutant> pkgBriefs = new HashMap<String,PackageCoverageBriefMutant>();
    Set<String> allPackages = new HashSet<String>();
    Map<String,Map<String,SourceFileCoverageBriefMutant>> packagesToSourceClasses = 
        new HashMap<String,Map<String,SourceFileCoverageBriefMutant>>();
    
    I_ClassInstrumentationMetadataStoreMutant store = memory_.getClassInstrumentationInfoStore();
    
    Iterator<String> it = classesCopy.iterator();
    while (it.hasNext()) {
      String className = it.next();
      if (memory_.isFiltered(className)) {
        it.remove();
      } else {
        I_ClassInstrumentationMetadata info = store.getClassInstrumentation(className);
        if (info == null) {
          throw new IllegalStateException("No instrumentation info found for class " + System.lineSeparator() +
              className);
        }
        runtime_.ensureProbesInitialized(info);
        String packageName = ClassMethods.getPackageName(className);
        
        if (memory_.isResultPackage(packageName)) {
          PackageCoverageBriefMutant bm = pkgBriefs.get(packageName);
          if (bm == null) {
            allPackages.add(packageName);
            bm = new PackageCoverageBriefMutant();
            bm.setPackageName(packageName);
            pkgBriefs.put(packageName, bm);
            
          } 
        }
      }
    }
    //note all classes must have ensureProbesInitialized 
    //before we can get the source file coverage,
    //since ensureProbesInitialized may discover parent child inner class relationships
    for (String className: classesCopy) {
      String packageName = ClassMethods.getPackageName(className);
      PackageCoverageBriefMutant bm = pkgBriefs.get(packageName);
      Map<String,SourceFileCoverageBriefMutant> classBriefs = packagesToSourceClasses.get(packageName);
      if (classBriefs == null) {
        classBriefs = new HashMap<String,SourceFileCoverageBriefMutant>();
      }
      I_SourceFileCoverageBrief brief = runtime_.getSourceFileCoverage(className);
      classBriefs.put(className, new SourceFileCoverageBriefMutant(brief));
      packagesToSourceClasses.put(packageName, classBriefs);
    }
    //create a PackageDiscovery for each, merges in the children
    Iterator<String> pkgs = allPackages.iterator();
    while (pkgs.hasNext()) {
      Set<String> copy = new HashSet<String>(allPackages);
      String pkg = pkgs.next();
      copy.remove(pkg);
      boolean remove = false;
      for (String op: copy) {
        int index = op.indexOf(pkg);
        if (index == 0) {
          remove = true;
          normalizeCoverageRelations(pkgBriefs, pkg, op);
        }
        index = pkg.indexOf(op);
        if (index == 0) {
          remove = true;
          normalizeCoverageRelations(pkgBriefs, op, pkg);
        }
      }
      if (remove) {
        pkgs.remove();
      }
    }
    
    List<I_PackageCoverageBrief> toRet = new ArrayList<I_PackageCoverageBrief>();
    for (String pkg: allPackages) {
      PackageCoverageBriefMutant pm = pkgBriefs.get(pkg);
      recurseChildPackages(pm, packagesToSourceClasses);
      toRet.add(new PackageCoverageBrief(pm));
    }
    if (log_.isLogEnabled(Recorder.class)) {
      log_.log("returning package coverages " + toRet);
    }
    return toRet;
  }

  public void normalizeCoverageRelations(Map<String, PackageCoverageBriefMutant> pkgBriefs,
      String pkg, String op) {
    PackageCoverageBriefMutant top = pkgBriefs.get(pkg);
    String nextPackages = op.substring(pkg.length() + 1, op.length());
    StringTokenizer names = new StringTokenizer(nextPackages, ".");
    StringBuilder soFar = new StringBuilder();
    soFar.append(pkg);
    PackageCoverageBriefMutant parent = top;
    while (names.hasMoreTokens()) {
      String next = names.nextToken();
      soFar.append("." + next);
      String name = soFar.toString();
      
      List<PackageCoverageBriefMutant> children = parent.getChildren();
      boolean found = false;
      //search the current instance
      for (PackageCoverageBriefMutant child: children) {
        if (name.equals(child.getPackageName())) {
          parent = child;
          found = true;
          break;
        }
      }
      
      if (!found) {
      //search the cache instance
        PackageCoverageBriefMutant pm = pkgBriefs.get(name);
        if (pm != null) {
          parent.addChild(pm);
          found = true;
          break;
        } else {
          pm = new PackageCoverageBriefMutant();
          pm.setPackageName(pkg);
          parent.addChild(pm);
          pkgBriefs.put(pkg, pm);
          parent = pm;
        }
      }
    }
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