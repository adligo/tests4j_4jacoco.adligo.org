package org.adligo.tests4j_4jacoco.plugin.data.coverage;

import java.util.concurrent.atomic.AtomicBoolean;

import org.adligo.tests4j.models.shared.coverage.CoverageUnitContinerMutant;
import org.adligo.tests4j.models.shared.coverage.CoverageUnits;
import org.adligo.tests4j.models.shared.coverage.I_LineCoverage;
import org.adligo.tests4j.models.shared.coverage.I_SourceFileCoverage;
import org.adligo.tests4j.models.shared.coverage.LineCoverageMutant;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceFileCoverage;

public class LazySourceFileCoverage extends CoverageUnitContinerMutant implements I_SourceFileCoverage {
	private AtomicBoolean loadedLines = new AtomicBoolean(false);
	private ISourceFileCoverage delegate;
	private String className;
	
	public LazySourceFileCoverage(ISourceFileCoverage p) {
		delegate = p;
		className = delegate.getName();
		ICounter branchCounter = delegate.getBranchCounter();
		ICounter inCounter = delegate.getInstructionCounter();
		super.setCoverageUnits(new CoverageUnits(branchCounter.getTotalCount() + 
				inCounter.getTotalCount()));
		super.setCoveredCoverageUnits(new CoverageUnits(branchCounter.getCoveredCount() + 
				inCounter.getCoveredCount()));
	}
	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public int getLastLine() {
		// TODO Auto-generated method stub
		return delegate.getLastLine();
	}

	@Override
	public I_LineCoverage getLineCoverage(int p) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
