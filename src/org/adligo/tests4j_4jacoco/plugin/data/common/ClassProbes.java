package org.adligo.tests4j_4jacoco.plugin.data.common;

public class ClassProbes implements I_ClassProbes {
	private I_Probes probes;
	private long classId;
	private String className;
	
	public ClassProbes(I_Probes pProbes, long pClassId, String pClassName) {
		probes = pProbes;
		classId = pClassId;
		className = pClassName;
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
