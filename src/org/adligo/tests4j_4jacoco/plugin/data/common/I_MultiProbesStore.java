package org.adligo.tests4j_4jacoco.plugin.data.common;

import org.adligo.tests4j.models.shared.coverage.I_SourceFileCoverageBrief;

import java.util.Iterator;

public interface I_MultiProbesStore {

  /**
   * 
   * @param threadGroupName
   * @param sourceFileClassName
   * @param classIds
   * @return
   */
  public I_SourceFileCoverageBrief getSourceFileProbes(String threadGroupName, 
      String sourceFileClassName, Iterator<Long> classIds);
}
