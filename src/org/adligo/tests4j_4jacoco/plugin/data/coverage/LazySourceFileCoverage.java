package org.adligo.tests4j_4jacoco.plugin.data.coverage;

import org.adligo.tests4j.models.shared.coverage.CoverageUnitContinerMutant;
import org.adligo.tests4j.models.shared.coverage.CoverageUnits;
import org.adligo.tests4j.models.shared.coverage.I_LineCoverage;
import org.adligo.tests4j.models.shared.coverage.I_SourceFileCoverage;
import org.adligo.tests4j.models.shared.coverage.I_SourceFileCoverageBriefMutant;
import org.adligo.tests4j.models.shared.coverage.SourceFileCoverageBrief;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ISourceFileCoverage;

import java.util.concurrent.atomic.AtomicBoolean;

public class LazySourceFileCoverage extends CoverageUnitContinerMutant implements I_SourceFileCoverage {
	private AtomicBoolean loadedLines = new AtomicBoolean(false);
	private ISourceFileCoverage delegate;
	private SourceFileCoverageBrief brief_;
	
	 @SuppressWarnings("boxing")
  public LazySourceFileCoverage(ISourceFileCoverage p, I_SourceFileCoverageBriefMutant brief) {
	   delegate = p;
	   
	   ICounter branchCounter = delegate.getBranchCounter();
	   ICounter inCounter = delegate.getInstructionCounter();
	   Integer coveredCount;
	   Integer count;
	   if (branchCounter != null && inCounter != null) {
  	   coveredCount =  branchCounter.getCoveredCount() + 
           inCounter.getCoveredCount();
  	   count = branchCounter.getTotalCount() + 
           inCounter.getTotalCount();
	   } else {
	     count = 0;
	     coveredCount = 0;
	   }
	   super.setCoveredCoverageUnits(new CoverageUnits(coveredCount));
	   super.setCoverageUnits(new CoverageUnits(count));
	   
	   brief.setCoverageUnits(count);
	   brief.setCoveredCoverageUnits(coveredCount);
	   brief_ = new SourceFileCoverageBrief(brief);
	 }
	 
	@Override
	public String getClassName() {
		return brief_.getClassName();
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
	
	public SourceFileCoverageBrief toBrief() {
	 return brief_;
	}
}
