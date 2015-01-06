package org.adligo.tests4j_4jacoco.plugin.instrumentation.common;

public interface I_ClassInstrumentationMetadata {
  /**
   * the id from jacoco
   * @return
   */
  public long getId();
  /**
   * The regular java class name.
   * @return
   */
  public String getClassName();
  
  /**
   * the byte code type name of the class.
   * @return
   */
  public String getClassTypeName();
  /**
   * the number of probes in the class
   * @return
   */
  public int getProbeCount();
}
