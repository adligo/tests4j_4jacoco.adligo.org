package org.adligo.tests4j_4jacoco.plugin.data.common;


/**
 * a immutable representation of probes
 * @author scott
 *
 */
public class Probes implements I_Probes {
	private final boolean[] probes;
	
	public Probes(I_Probes other) {
		probes = new boolean[other.size()];
		for (int i = 0; i < other.size(); i++) {
			probes[i] = other.get(i);
		}
	}
	
	public Probes(boolean [] pProbes) {
		probes = new boolean[pProbes.length];
		for (int i = 0; i < pProbes.length; i++) {
			probes[i] = pProbes[i];
		}
	}
	
	@Override
	public boolean get(int p) {
		if (p >= probes.length) {
			return false;
		}
		return probes[p];
	}

	@Override
	public int size() {
		return probes.length;
	}
}
