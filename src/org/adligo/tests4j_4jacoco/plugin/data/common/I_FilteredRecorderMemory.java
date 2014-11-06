package org.adligo.tests4j_4jacoco.plugin.data.common;

import org.adligo.tests4j.shared.output.I_Tests4J_Log;

public interface I_FilteredRecorderMemory {
  public I_Tests4J_Log getLog();
  public String getTrialThreadGroupName();
  
  public boolean isInScope(String className);
  public void addClassInScopeWithProbes(long classId);
}
