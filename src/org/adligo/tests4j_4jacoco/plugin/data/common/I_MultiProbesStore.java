package org.adligo.tests4j_4jacoco.plugin.data.common;

import org.adligo.tests4j.models.shared.coverage.I_SourceFileCoverageBrief;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ClassInstrumentationMetadata;

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
  
  /**
   * 
   * @param threadGroupName
   * @param sourceFileClassName
   * @param classIds
   * @return
   */
  public I_SourceFileCoverageBrief getSourceFileProbes(String sourceFileClassName);
  
  /**
   * This method makes sure the probes are initialized for 
   * the class, and must be called for all classes
   * which I_SourceFileCoverageBrief are eventually expected.
   * This makes sure that all parent child classes
   * have been setup, even though some of those classes
   * were not actually touched by the execution.
  * @param className
  */
  public void ensureProbesInitialized(I_ClassInstrumentationMetadata info);
}
