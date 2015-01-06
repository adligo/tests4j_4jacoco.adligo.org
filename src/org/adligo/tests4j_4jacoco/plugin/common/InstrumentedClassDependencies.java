package org.adligo.tests4j_4jacoco.plugin.common;

import org.adligo.tests4j.models.shared.association.I_ClassAssociationsLocal;

public class InstrumentedClassDependencies implements I_InstrumentedClassDependencies {
	private Class<?> instrumentedClass;
	private I_ClassAssociationsLocal classDependencies;
	
	public InstrumentedClassDependencies(Class<?> instrumentedClassIn, 
			I_ClassAssociationsLocal classDependenciesIn) {
		instrumentedClass = instrumentedClassIn;
		classDependencies = classDependenciesIn;
	}
	
	/* (non-Javadoc)
   * @see org.adligo.tests4j_4jacoco.plugin.instrumentation.I_InstrumentedClassDependencies#getInstrumentedClass()
   */
	@Override
  public Class<?> getInstrumentedClass() {
		return instrumentedClass;
	}
	/* (non-Javadoc)
   * @see org.adligo.tests4j_4jacoco.plugin.instrumentation.I_InstrumentedClassDependencies#getClassDependencies()
   */
	@Override
  public I_ClassAssociationsLocal getClassDependencies() {
		return classDependencies;
	}
}
