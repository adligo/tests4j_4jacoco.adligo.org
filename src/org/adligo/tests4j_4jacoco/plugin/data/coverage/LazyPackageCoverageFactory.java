package org.adligo.tests4j_4jacoco.plugin.data.coverage;

import org.adligo.tests4j.models.shared.coverage.CoverageUnits;
import org.adligo.tests4j.models.shared.coverage.I_PackageCoverageBrief;
import org.adligo.tests4j.models.shared.coverage.I_SourceFileCoverage;
import org.adligo.tests4j.models.shared.coverage.I_SourceFileCoverageBrief;
import org.adligo.tests4j.models.shared.coverage.SourceFileCoverageMutant;
import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;
import org.adligo.tests4j_4jacoco.plugin.common.I_CoveragePluginMemory;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LazyPackageCoverageFactory {

	public static List<I_PackageCoverageBrief> create(I_ProbesDataStore data, 
			I_CoveragePluginMemory memory, Set<String> testedClasses) {
		List<I_PackageCoverageBrief> toRet = new ArrayList<I_PackageCoverageBrief>();
		
		
		I_CachedClassBytesClassLoader classLoader = memory.getInstrumentedClassLoader();
		List<String> classNames = classLoader.getAllCachedClasses();
		I_Tests4J_Log log = memory.getLog();
		
		List<String> allClassNames = new ArrayList<String>(classNames);
		allClassNames.addAll(testedClasses);
		
		Set<String> pkgs =  getAllPackages(allClassNames, log);
		Set<String> topPackages = getTopPackages(pkgs, log);
		for (String pkg: topPackages) {
			LazyPackageCoverageInput input = new LazyPackageCoverageInput();
			List<String> clazzes = getClasses(pkg, allClassNames);
			input.setClassNames(clazzes);
			input.setProbeData(data);
			input.setPackageName(pkg);
			LazyPackageCoverage toAdd = new LazyPackageCoverage(input, memory);
			toRet.add(toAdd);
		}
		return toRet;
	}

	/**
	 * 
	 * @param data
	 * @param memory
	 * @param testedClass classes/inner classes for the same source outer class
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static I_SourceFileCoverage createSourceFileCoverage(I_ProbesDataStore data, 
      I_CoveragePluginMemory memory, String testedClass, Collection<String> innerClasses) throws ClassNotFoundException {
   
    I_CachedClassBytesClassLoader classLoader = memory.getInstrumentedClassLoader();
    List<String> classNames = classLoader.getAllCachedClasses();
    
    List<String> allClassNames = new ArrayList<String>(classNames);
    allClassNames.add(testedClass);
    for (String clazz: innerClasses) {
      allClassNames.add(clazz);
    }
    Class<?> clazz = Class.forName(testedClass);
    String packageName = clazz.getPackage().getName();
    
    
    LazyPackageCoverageInput input = new LazyPackageCoverageInput();
    input.setClassNames(allClassNames);
    input.setProbeData(data);
    input.setPackageName(packageName);
    LazyPackageCoverage lpc = new LazyPackageCoverage(input, memory);
    
    
    I_SourceFileCoverageBrief sfc = lpc.getCoverage(clazz.getSimpleName());
    int coverageUnits = sfc.getCoverageUnits();
    int covered = sfc.getCoveredCoverageUnits();
    for (String clazzName: innerClasses) {
      clazz = Class.forName(clazzName);
      sfc = lpc.getCoverage(clazz.getSimpleName());
      coverageUnits += sfc.getCoverageUnits();
      covered += sfc.getCoveredCoverageUnits();
    }
    SourceFileCoverageMutant toRet = new SourceFileCoverageMutant();
    toRet.setClassName(testedClass);
    toRet.setCoverageUnits(new CoverageUnits(coverageUnits));
    toRet.setCoveredCoverageUnits(new CoverageUnits(covered));
    return toRet;
  }
	
	public static Set<String> getAllPackages(List<String> classNames,
			I_Tests4J_Log log) {
		Set<String> allPackages  = new HashSet<String>();
		for (String className: classNames) {
			if (log.isLogEnabled(LazyPackageCoverageFactory.class)) {
				log.log(LazyPackageCoverageFactory.class.getSimpleName() + " " + className);
			}
			int lastDot = className.lastIndexOf(".");
			String packageName = className.substring(0, lastDot);
			allPackages.add(packageName);
		}
		return allPackages;
	}
	
	public static Set<String> getTopPackages(Collection<String> allPackages,
			I_Tests4J_Log log) {
		Set<String> toRet  = new HashSet<String>();
		Set<String> clone  = new HashSet<String>(allPackages);
		for (String pkgName: clone) {
			int count = 0;
			for (String others: allPackages) {
				if (pkgName.contains(others)) {
					count++;
				}
			}
			if (count == 1) {
				toRet.add(pkgName);
			}
		}
		return toRet;
	}
	
	public static List<String> getClasses(String topPackageName, Collection<String> classNames) {
		List<String> toRet  = new ArrayList<String>();
		for (String className: classNames) {
			if (className.indexOf(topPackageName) == 0) {
				toRet.add(className);
			}
		}
		return toRet;
	}
}
