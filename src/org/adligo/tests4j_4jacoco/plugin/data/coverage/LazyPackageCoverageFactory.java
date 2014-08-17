package org.adligo.tests4j_4jacoco.plugin.data.coverage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.adligo.tests4j.models.shared.coverage.I_PackageCoverage;
import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;
import org.adligo.tests4j_4jacoco.plugin.common.I_CoveragePluginMemory;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;

public class LazyPackageCoverageFactory {

	public static List<I_PackageCoverage> create(I_ProbesDataStore data, I_CoveragePluginMemory memory) {
		List<I_PackageCoverage> toRet = new ArrayList<I_PackageCoverage>();
		
		Set<String> allPackages  = new HashSet<String>();
		I_CachedClassBytesClassLoader classLoader = memory.getCachedClassLoader();
		List<String> classNames = classLoader.getAllCachedClasses();
		
		for (String className: classNames) {
			int lastDot = className.lastIndexOf(".");
			String packageName = className.substring(0, lastDot);
			allPackages.add(packageName);
		}
		
		Set<String> allPackagesClone = new HashSet<String>(allPackages);
		Iterator<String> ap = allPackages.iterator();
		while (ap.hasNext()) {
			String pkg = ap.next();
			for (String otherPkg: allPackagesClone) {
				if (!pkg.equals(otherPkg)) {
					if (pkg.contains(otherPkg)) {
						ap.remove();
						break;
					}
				}
			}
		}
		for (String pkg: allPackages) {
			LazyPackageCoverageInput input = new LazyPackageCoverageInput();
			input.setClassNames(classNames);
			input.setProbeData(data);
			input.setPackageName(pkg);
			LazyPackageCoverage toAdd = new LazyPackageCoverage(input, memory);
			toRet.add(toAdd);
		}
		return toRet;
	}
}
