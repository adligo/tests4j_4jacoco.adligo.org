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
import org.adligo.tests4j.models.shared.coverage.SourceFileCoverageMutant;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;
import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;
import org.adligo.tests4j_4jacoco.plugin.analysis.common.CoverageAnalyzer;
import org.adligo.tests4j_4jacoco.plugin.common.I_CoveragePluginMemory;
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
	private I_CachedClassBytesClassLoader classLoader;
	private I_Tests4J_Log log;
	
	public LazyPackageCoverage(LazyPackageCoverageInput input, I_CoveragePluginMemory memory) {
		packageName = input.getPackageName();
		probeData = input.getProbeData();
		classLoader = memory.getCachedClassLoader();
		
		log = memory.getLog();
		
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
			children.add(new LazyPackageCoverage(input, memory));
		}
	}
	
	private I_SourceFileCoverage getOrLoadSourceFileCoverage(String classSimpleName) {
		I_SourceFileCoverage toRet = sourceCoverage.get(classSimpleName);
		if (toRet != null) {
			return toRet;
		}
		CoverageBuilder coverageBuilder = new CoverageBuilder();
		CoverageAnalyzer analyzer = new CoverageAnalyzer(probeData, coverageBuilder);
		
		String fullName = packageName  + "." + classSimpleName;
		
		try {	
			Class<?> c =  Class.forName(fullName);
			if (c.isInterface()) {
				SourceFileCoverageMutant sfcm = new SourceFileCoverageMutant();
				sfcm.setClassName(classSimpleName);
				sfcm.setCoverageUnits(new CoverageUnits(0));
				sfcm.setCoveredCoverageUnits(new CoverageUnits(0));
				sourceCoverage.put(classSimpleName, sfcm);
				return sfcm;
			}
			analyzer.analyzeClass(classLoader.getCachedBytesStream(fullName), fullName);
		} catch (ClassNotFoundException x) {
			log.onThrowable(x);
			return null;
		} catch (IOException x) {
			log.onThrowable(x);
			return null;
		}
		
		Collection<ISourceFileCoverage> sourceCoverages = coverageBuilder.getSourceFiles();
		if (sourceCoverages.size() != 1) {
			throw new RuntimeException("problem loading coverage for " + fullName);
		}
		ISourceFileCoverage sfc = sourceCoverages.iterator().next();
		String shortName = sfc.getName();
		int lastDot = shortName.lastIndexOf(".");
		shortName = shortName.substring(0, lastDot);
		
		LazySourceFileCoverage lsfc = new LazySourceFileCoverage(sfc);
		sourceCoverage.put(shortName, lsfc);
		return lsfc;
		
	}
	private  Map<String,I_SourceFileCoverage> getOrLoadAllCoverage() {
		if (loadedAllSourceFiles.get()) {
			return sourceCoverage;
		} else {
			int cus = 0;
			int covered_cus = 0;
			
			CoverageBuilder coverageBuilder = new CoverageBuilder();
			CoverageAnalyzer analyzer = new CoverageAnalyzer(probeData, coverageBuilder);
			
			for (String fileName: classNames) {
				if (!sourceCoverage.containsKey(fileName)) {
					String fullName = packageName  + "." + fileName;
					try {	
						analyzer.analyzeClass(classLoader.getCachedBytesStream(fullName), fullName);
					} catch (IOException x) {
						log.onThrowable(x);
					}
				}
			}
			Collection<ISourceFileCoverage> sourceCoverages = coverageBuilder.getSourceFiles();
			for (ISourceFileCoverage sfc: sourceCoverages) {
				String shortName = sfc.getName();
				int lastDot = shortName.lastIndexOf(".");
				shortName = shortName.substring(0, lastDot);
				
				LazySourceFileCoverage lsfc = new LazySourceFileCoverage(sfc);
				sourceCoverage.put(shortName, lsfc);
			}
			Collection<I_SourceFileCoverage> entries = sourceCoverage.values();
			for (I_SourceFileCoverage sfc: entries) {
				cus = cus + sfc.getCoverageUnits().get();
				covered_cus = covered_cus + sfc.getCoveredCoverageUnits().get();
			}
			counts.setCoverageUnits(new CoverageUnits(cus));
			counts.setCoveredCoverageUnits(new CoverageUnits(covered_cus));
			int total_cus = cus;
			int total_covered_cus = covered_cus;
			
			for (LazyPackageCoverage child: children) {
				child.getOrLoadAllCoverage();
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
		getOrLoadAllCoverage();
		return counts.getCoverageUnits();
	}

	@Override
	public I_CoverageUnits getCoveredCoverageUnits() {
		getOrLoadAllCoverage();
		return counts.getCoveredCoverageUnits();
	}

	@Override
	public BigDecimal getPercentageCovered() {
		getOrLoadAllCoverage();
		return counts.getPercentageCovered();
	}

	@Override
	public double getPercentageCoveredDouble() {
		getOrLoadAllCoverage();
		return counts.getPercentageCoveredDouble();
	}

	@Override
	public String getPackageName() {
		return packageName;
	}

	@Override
	public I_SourceFileCoverage getCoverage(String sourceFileName) {
		return getOrLoadSourceFileCoverage(sourceFileName);
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
