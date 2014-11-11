package org.adligo.tests4j_4jacoco.plugin.data.coverage;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceFileCoverage;

public class InterfaceSourceFileCoverage implements ISourceFileCoverage {
  public static final InterfaceSourceFileCoverage INSTANCE = new InterfaceSourceFileCoverage();
  
  private InterfaceSourceFileCoverage() {}
  @Override
  public int getFirstLine() {
    return 0;
  }

  @Override
  public int getLastLine() {
    return 0;
  }

  @Override
  public ILine getLine(int arg0) {
    return null;
  }

  @Override
  public ICounter getBranchCounter() {
    return null;
  }

  @Override
  public ICounter getClassCounter() {
    return null;
  }

  @Override
  public ICounter getComplexityCounter() {
    return null;
  }

  @Override
  public ICounter getCounter(CounterEntity arg0) {
    return null;
  }

  @Override
  public ElementType getElementType() {
    return null;
  }

  @Override
  public ICounter getInstructionCounter() {
    return null;
  }

  @Override
  public ICounter getLineCounter() {
    return null;
  }

  @Override
  public ICounter getMethodCounter() {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public ICoverageNode getPlainCopy() {
    return null;
  }

  @Override
  public String getPackageName() {
    return null;
  }

}
