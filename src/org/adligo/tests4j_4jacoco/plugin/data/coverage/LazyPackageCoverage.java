package org.adligo.tests4j_4jacoco.plugin.data.coverage;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.adligo.tests4j.models.shared.coverage.CoverageUnitContinerMutant;
import org.adligo.tests4j.models.shared.coverage.CoverageUnits;
import org.adligo.tests4j.models.shared.coverage.I_CoverageUnits;
import org.adligo.tests4j.models.shared.coverage.I_PackageCoverage;
import org.adligo.tests4j.models.shared.coverage.I_SourceFileCoverage;
import org.adligo.tests4j_4jacoco.plugin.analysis.common.CoverageAnalyzer;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.ISourceFileCoverage;

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
	private AtomicBoolean loadedAllSourceFiles = new AtomicBoolean(false);
	private String packageName;
	private List<LazyPackageCoverage> children = new ArrayList<LazyPackageCoverage>();
	private Set<String> classNames = new HashSet<String>();
	private I_ProbesDataStore probeData;
	private Map<String,I_SourceFileCoverage> sourceCoverage = new HashMap<String, I_SourceFileCoverage>();
	private CoverageUnitContinerMutant countTotals = new CoverageUnitContinerMutant();
	private CoverageUnitContinerMutant counts = new CoverageUnitContinerMutant();
	private ClassLoader classLoader;
	
	public LazyPackageCoverage(LazyPackageCoverageInput input) {
		packageName = input.getPackageName();
		probeData = input.getProbeData();
		classLoader = input.getClassLoader();
		
		Set<String> subPackages = new HashSet<String>();
		
		for (String className: input.getClassNames()) {
			int lastDot = className.lastIndexOf(".");
			String pkgName = className.substring(0, lastDot);
			if (packageName.equals(pkgName)) {
				String classShortName = className.substring(lastDot+1, className.length());
				classNames.add(classShortName);
			} else if (pkgName.contains(packageName)) {
				String sub = pkgName.substring(packageName.length() + 1, pkgName.length());
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
	
	private  Map<String,I_SourceFileCoverage> getOrLoadSourceFileCoverage() {
		if (loadedAllSourceFiles.get()) {
			return sourceCoverage;
		} else {
			int cus = 0;
			int covered_cus = 0;
			
			CoverageBuilder coverageBuilder = new CoverageBuilder();
			CoverageAnalyzer analyzer = new CoverageAnalyzer(probeData, coverageBuilder);
			
			for (String fileName: classNames) {
				String fullName = packageName  + "." + fileName;
				try {
					analyzer.analyzeClass(classLoader.getResourceAsStream(fullName), fullName);
				} catch (IOException x) {
					x.printStackTrace();
				}
			}
			Collection<ISourceFileCoverage> sourceCoverages = coverageBuilder.getSourceFiles();
			for (ISourceFileCoverage sfc: sourceCoverages) {
				String shortName = sfc.getName();
				int lastDot = shortName.lastIndexOf(".");
				shortName = shortName.substring(0, lastDot);
				
				LazySourceFileCoverage lsfc = new LazySourceFileCoverage(sfc);
				cus = cus + lsfc.getCoverageUnits().get();
				covered_cus = covered_cus + lsfc.getCoveredCoverageUnits().get();
				
				sourceCoverage.put(shortName, lsfc);
			}
			
			counts.setCoverageUnits(new CoverageUnits(cus));
			counts.setCoveredCoverageUnits(new CoverageUnits(covered_cus));
			int total_cus = cus;
			int total_covered_cus = covered_cus;
			
			for (LazyPackageCoverage child: children) {
				child.getOrLoadSourceFileCoverage();
				total_cus = total_cus + child.getCoverageUnits().get();
				total_covered_cus = total_covered_cus + child.getCoveredCoverageUnits().get();
			}
			countTotals.setCoverageUnits(new CoverageUnits(total_cus));
			countTotals.setCoveredCoverageUnits(new CoverageUnits(total_covered_cus));
		}
		return sourceCoverage;
	}
	
	@Override
	public I_CoverageUnits getCoverageUnits() {
		getOrLoadSourceFileCoverage();
		return counts.getCoverageUnits();
	}

	@Override
	public I_CoverageUnits getCoveredCoverageUnits() {
		getOrLoadSourceFileCoverage();
		return counts.getCoveredCoverageUnits();
	}

	@Override
	public BigDecimal getPercentageCovered() {
		getOrLoadSourceFileCoverage();
		return counts.getPercentageCovered();
	}

	@Override
	public double getPercentageCoveredDouble() {
		getOrLoadSourceFileCoverage();
		return counts.getPercentageCoveredDouble();
	}

	@Override
	public String getPackageName() {
		return packageName;
	}

	@Override
	public I_SourceFileCoverage getCoverage(String sourceFileName) {
		getOrLoadSourceFileCoverage();
		return sourceCoverage.get(sourceFileName);
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
