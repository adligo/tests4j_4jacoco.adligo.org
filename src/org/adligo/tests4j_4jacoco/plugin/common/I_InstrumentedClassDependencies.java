package org.adligo.tests4j_4jacoco.plugin.common;

import org.adligo.tests4j.models.shared.association.I_ClassAssociationsLocal;

public interface I_InstrumentedClassDependencies {

  public abstract Class<?> getInstrumentedClass();

  public abstract I_ClassAssociationsLocal getClassDependencies();

}