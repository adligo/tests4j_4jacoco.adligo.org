package org.adligo.tests4j_4jacoco.plugin.data.common;

import java.util.Arrays;

/**
 * a immutable representation of probes
 * @author scott
 *
 */
public class Probes implements I_Probes {
	private boolean[] probes;
	
	public Probes(boolean [] pProbes) {
		probes = Arrays.copyOf(pProbes, pProbes.length);
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
