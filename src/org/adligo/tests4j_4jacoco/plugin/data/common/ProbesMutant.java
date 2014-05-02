package org.adligo.tests4j_4jacoco.plugin.data.common;

public class ProbesMutant implements I_Probes {
	private boolean[] probes;

	public ProbesMutant() {
		probes = new boolean[]{};
	}
	
	public ProbesMutant(boolean [] pProbes) {
		probes = pProbes;
	}
	
	public ProbesMutant(I_Probes other) {
		probes = new boolean[other.size()];
		for (int i = 0; i < other.size(); i++) {
			probes[i] = other.get(i);
		}
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
	
	public boolean[] getArray() {
		return probes;
	}
}
