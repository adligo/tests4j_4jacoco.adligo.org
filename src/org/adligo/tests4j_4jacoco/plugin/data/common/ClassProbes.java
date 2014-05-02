package org.adligo.tests4j_4jacoco.plugin.data.common;

public class ClassProbes implements I_ClassProbes {
	private I_Probes probes;
	private long classId;
	private String className;
	
	public ClassProbes(I_ClassProbes other) {
		probes = new Probes(other.getProbes());
		classId = other.getClassId();
		className = other.getClassName();
	}
	
	@Override
	public I_Probes getProbes() {
		return probes;
	}
	
	@Override
	public long getClassId() {
		return classId;
	}
	
	@Override
	public String getClassName() {
		return className;
	}
}
