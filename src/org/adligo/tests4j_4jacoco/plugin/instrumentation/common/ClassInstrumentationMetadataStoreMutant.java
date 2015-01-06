package org.adligo.tests4j_4jacoco.plugin.instrumentation.common;

import java.util.concurrent.ConcurrentHashMap;

public class ClassInstrumentationMetadataStoreMutant implements I_ClassInstrumentationMetadataStoreMutant {
  private ConcurrentHashMap<String, I_ClassInstrumentationMetadata> classNamesToInfo_ =
      new ConcurrentHashMap<String, I_ClassInstrumentationMetadata>();
      
  /* (non-Javadoc)
   * @see org.adligo.tests4j_4jacoco.plugin.data.common.I_ClassInstrumentationBriefsStore#add(org.adligo.tests4j_4jacoco.plugin.data.common.I_ClassInstrumentationBrief)
   */
  @Override
  public void add(I_ClassInstrumentationMetadata brief) {
    classNamesToInfo_.put(brief.getClassName(), brief);
  }
  
  /* (non-Javadoc)
   * @see org.adligo.tests4j_4jacoco.plugin.data.common.I_ClassInstrumentationBriefsStore#getClassInstrumentation(java.lang.String)
   */
  @Override
  public I_ClassInstrumentationMetadata getClassInstrumentation(String name) {
    return classNamesToInfo_.get(name);
  }
  
}
