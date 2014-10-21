package org.adligo.tests4j_4jacoco.plugin.instrumentation;

import org.adligo.tests4j.models.shared.association.I_ClassAssociationsLocal;

public class InstrumentedClassDependencies {
	private Class<?> instrumentedClass;
	private I_ClassAssociationsLocal classDependencies;
	
	public InstrumentedClassDependencies(Class<?> instrumentedClassIn, 
			I_ClassAssociationsLocal classDependenciesIn) {
		instrumentedClass = instrumentedClassIn;
		classDependencies = classDependenciesIn;
	}
	
	public Class<?> getInstrumentedClass() {
		return instrumentedClass;
	}
	public I_ClassAssociationsLocal getClassDependencies() {
		return classDependencies;
	}
}
