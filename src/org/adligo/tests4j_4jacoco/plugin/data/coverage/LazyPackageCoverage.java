package org.adligo.tests4j_4jacoco.plugin.data.coverage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.adligo.tests4j.models.shared.coverage.I_CoverageUnits;
import org.adligo.tests4j.models.shared.coverage.I_PackageCoverage;
import org.adligo.tests4j.models.shared.coverage.I_SourceFileCoverage;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;

/**
 * Note this is lazy mostly to;
 * limit memory (everything is still I_ProbesDataStore under the covers,
 *     and is only converted for a short time to the verbose tests4j api.
 *      
 * optimize thread usage.
 *     The conversion would be done in the main thread which handles reporting,
 *     so it doesn't cause the test times to increase much.
 *     
 * @author scott
 *
 */
public class LazyPackageCoverage implements I_PackageCoverage {
	private boolean loadedAllSourceFiles = false;
	private String packageName;
	private List<LazyPackageCoverage> children = new ArrayList<LazyPackageCoverage>();
	private Set<String> classNames = new HashSet<String>();
	private I_ProbesDataStore probeData;
	
	public LazyPackageCoverage(LazyPackageCoverageInput input) {
		packageName = input.getPackageName();
		probeData = input.getProbeData();
		
		Set<String> subPackages = new HashSet<String>();
		
		for (String className: input.getClassNames()) {
			int lastDot = className.lastIndexOf(".");
			String pkgName = className.substring(0, lastDot);
			if (packageName.equals(pkgName)) {
				String classShortName = className.substring(lastDot+1, className.length());
				classNames.add(classShortName);
			} else if (pkgName.contains(packageName)) {
				String sub = pkgName.substring(packageName.length(), pkgName.length());
				if (!sub.contains(".")) {
					//its a sub package
					subPackages.add(pkgName);
				}
			}
		}
		
		for (String subPkgName: subPackages) {
			input.setPackageName(subPkgName);
			children.add(new LazyPackageCoverage(input));
		}
	}
	
	@Override
	public I_CoverageUnits getCoverageUnits() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public I_CoverageUnits getCoveredCoverageUnits() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigDecimal getPercentageCovered() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getPercentageCoveredDouble() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getPackageName() {
		return packageName;
	}

	@Override
	public I_SourceFileCoverage getCoverage(String sourceFileName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getSourceFileNames() {
		return classNames;
	}

	@Override
	public List<I_PackageCoverage> getChildPackageCoverage() {
		List<I_PackageCoverage> toRet = new ArrayList<I_PackageCoverage>();
		toRet.addAll(children);
		return toRet;
	}

	@Override
	public boolean hasChildPackageCoverage() {
		if (children.size() >= 1) {
			return true;
		}
		return false;
	}

	@Override
	public I_CoverageUnits getTotalCoverageUnits() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public I_CoverageUnits getTotalCoveredCoverageUnits() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigDecimal getTotalPercentageCovered() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((packageName == null) ? 0 : packageName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LazyPackageCoverage other = (LazyPackageCoverage) obj;
		if (packageName == null) {
			if (other.packageName != null)
				return false;
		} else if (!packageName.equals(other.packageName))
			return false;
		return true;
	}

}
