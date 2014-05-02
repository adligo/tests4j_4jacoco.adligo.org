package org.adligo.tests4j_4jacoco.plugin.data.common;

public class ClassProbesMutant implements I_ClassProbesMutant {

	private ProbesMutant probes;
	private long classId;
	private String className;
	
	public ClassProbesMutant() {}
	
	public ClassProbesMutant(I_ClassProbes other) {
		probes = new ProbesMutant(other.getProbes());
		classId = other.getClassId();
		className = other.getClassName();
	}

	public ProbesMutant getProbes() {
		return probes;
	}

	public long getClassId() {
		return classId;
	}

	public String getClassName() {
		return className;
	}

	public void setProbes(I_Probes p) {
		this.probes = new ProbesMutant(p);
	}

	public void setClassId(long classId) {
		this.classId = classId;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	@Override
	public boolean[] getProbesArray() {
		return probes.getArray();
	}
}
