package org.adligo.tests4j_4jacoco.plugin.data.coverage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.adligo.tests4j.models.shared.coverage.I_PackageCoverage;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;

public class LazyPackageCoverageFactory {

	public static List<I_PackageCoverage> create(I_ProbesDataStore data) {
		List<I_PackageCoverage> toRet = new ArrayList<I_PackageCoverage>();
		
		Set<String> classNames = data.getAllClassNames();
		Set<String> allPackages  = new HashSet<String>();
		
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
		//ok allPackages now only has top packages
		return toRet;
	}
}
