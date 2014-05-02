package org.adligo.tests4j_4jacoco.plugin.data.common;

public class ProbesMutant implements I_Probes {
	private boolean[] probes;

	public ProbesMutant(boolean [] pProbes) {
		probes = pProbes;
	}
	
	@Override
	public boolean get(int p) {
		if (p >= probes.length) {
			return false;
		}
		return probes[p];
	}
	
	public void set(int i, boolean p) {
		if (i >= probes.length || i < 0) {
			return;
		}
		probes[i] = p;
	}
	
	@Override
	public int size() {
		return probes.length;
	}
}
