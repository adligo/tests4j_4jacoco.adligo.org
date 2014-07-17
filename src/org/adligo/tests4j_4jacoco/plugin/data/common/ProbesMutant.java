package org.adligo.tests4j_4jacoco.plugin.data.common;

public class ProbesMutant implements I_Probes {
	private final boolean[] probes;

	public ProbesMutant() {
		probes = new boolean[]{};
	}
	
	public ProbesMutant(boolean [] pProbes) {
		probes = new boolean[pProbes.length];
		for (int i = 0; i < pProbes.length; i++) {
			probes[i] = pProbes[i];
		}
	}
	
	public ProbesMutant(I_Probes other) {
		probes = new boolean[other.size()];
		for (int i = 0; i < other.size(); i++) {
			probes[i] = other.get(i);
		}
	}
	
	@Override
	public boolean get(int p) {
		if (p >= probes.length || p < 0) {
			return false;
		}
		return probes[p];
	}
	
	public void set(int i, boolean p) {
		if (i >= 0 && i < probes.length) {
			probes[i] = p;
		}
	}
	
	@Override
	public int size() {
		return probes.length;
	}
	
	public boolean[] getArray() {
		return probes;
	}
}
