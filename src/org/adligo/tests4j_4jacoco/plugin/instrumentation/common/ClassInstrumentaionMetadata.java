package org.adligo.tests4j_4jacoco.plugin.instrumentation.common;

import org.objectweb.asm.Type;

public class ClassInstrumentaionMetadata implements I_ClassInstrumentationMetadata {
  public static final String CLASS_INSTRUMENTAION_METADATA_REQUIRES_INFO = "ClassInstrumentaionMetadata requires info.";
  public static final String CLASS_INSTRUMENTAION_METADATA_REQUIRES_A_CLASS_NAME = "ClassInstrumentaionMetadata requires a className.";
  private long id_;
  private String className_;
  private String typeName_;
  private I_ClassInstrumentationInfo info_;
  
  public ClassInstrumentaionMetadata(long id, String className, I_ClassInstrumentationInfo info) {
    id_ = id;
    if (className == null) {
      throw new IllegalArgumentException(CLASS_INSTRUMENTAION_METADATA_REQUIRES_A_CLASS_NAME);
    }
    className_ = className;
    if (info == null) {
      throw new IllegalArgumentException(CLASS_INSTRUMENTAION_METADATA_REQUIRES_INFO);
    }
    info_ = info;
    try {
      typeName_ = Type.getInternalName(Class.forName(className_));
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public long getId() {
    return id_;
  }

  @Override
  public String getClassName() {
    return className_;
  }

  @Override
  public int getProbeCount() {
    return info_.getProbeCount();
  }

  @Override
  public String getClassTypeName() {
    return typeName_;
  }

}
