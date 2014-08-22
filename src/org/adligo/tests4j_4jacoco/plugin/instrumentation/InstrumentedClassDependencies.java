package org.adligo.tests4j_4jacoco.plugin.instrumentation;

import org.adligo.tests4j.models.shared.dependency.I_ClassDependenciesLocal;

public class InstrumentedClassDependencies {
	private Class<?> instrumentedClass;
	private I_ClassDependenciesLocal classDependencies;
	
	public InstrumentedClassDependencies(Class<?> instrumentedClassIn, 
			I_ClassDependenciesLocal classDependenciesIn) {
		instrumentedClass = instrumentedClassIn;
		classDependencies = classDependenciesIn;
	}
	
	public Class<?> getInstrumentedClass() {
		return instrumentedClass;
	}
	public I_ClassDependenciesLocal getClassDependencies() {
		return classDependencies;
	}
}
