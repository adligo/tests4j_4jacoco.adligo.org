package org.adligo.tests4j_4jacoco.plugin.data.multi;

import org.adligo.tests4j.run.common.ConcurrentQualifiedMap;
import org.adligo.tests4j.shared.i18n.I_Tests4J_Constants;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;
import org.adligo.tests4j_4jacoco.plugin.common.I_Runtime;

import java.util.concurrent.ConcurrentHashMap;

public class MultiContext {
  private I_Tests4J_Log log_;
  private I_Tests4J_Constants constants_;
  private I_Runtime runtime_;
  private final ConcurrentQualifiedMap<Long, MultiProbesMap> classIds_ = 
      new ConcurrentQualifiedMap<Long,MultiProbesMap>(new ConcurrentHashMap<Long, MultiProbesMap>());
  
  public MultiContext(I_Tests4J_Constants constants, I_Tests4J_Log log, I_Runtime runtime) {
    constants_ = constants;
    log_ = log;
    runtime_ = runtime;
  }
  
  public I_Tests4J_Log getLog() {
    return log_;
  }
  public I_Runtime getRuntime() {
    return runtime_;
  }
  public I_Tests4J_Constants getConstants() {
    return constants_;
  }

  public ConcurrentQualifiedMap<Long, MultiProbesMap> getMultiProbesMap() {
    return classIds_;
  }
  
}
