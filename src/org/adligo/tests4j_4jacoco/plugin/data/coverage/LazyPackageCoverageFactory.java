package org.adligo.tests4j_4jacoco.plugin.data.coverage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.adligo.tests4j.models.shared.coverage.I_PackageCoverage;
import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;
import org.adligo.tests4j_4jacoco.plugin.common.I_CoveragePluginMemory;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;

public class LazyPackageCoverageFactory {

	public static List<I_PackageCoverage> create(I_ProbesDataStore data, I_CoveragePluginMemory memory) {
		List<I_PackageCoverage> toRet = new ArrayList<I_PackageCoverage>();
		
		
		I_CachedClassBytesClassLoader classLoader = memory.getInstrumentedClassLoader();
		List<String> classNames = classLoader.getAllCachedClasses();
		I_Tests4J_Log log = memory.getLog();
		
		Set<String> allPackages = getAllPackages(classNames, log);
		Set<String> topPackages = getTopPackages(allPackages, log);
		for (String pkg: topPackages) {
			LazyPackageCoverageInput input = new LazyPackageCoverageInput();
			List<String> clazzes = getClasses(pkg, classNames, log);
			input.setClassNames(clazzes);
			input.setProbeData(data);
			input.setPackageName(pkg);
			LazyPackageCoverage toAdd = new LazyPackageCoverage(input, memory);
			toRet.add(toAdd);
		}
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
	
	public static List<String> getClasses(String topPackageName, Collection<String> classNames, 
			I_Tests4J_Log log) {
		List<String> toRet  = new ArrayList<String>();
		for (String className: classNames) {
			if (className.indexOf(topPackageName) == 0) {
				toRet.add(className);
			}
		}
		return toRet;
	}
}
