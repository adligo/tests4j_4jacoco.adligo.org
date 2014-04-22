package org.adligo.tests4j_4jacoco.plugin.data;

import java.util.Arrays;

public class FrameInfo implements I_FrameInfo {
	private FrameInfoMutant other;
	
	public FrameInfo(I_FrameInfo p) {
		other = new FrameInfoMutant(p);
	}

	public int getType() {
		return other.getType();
	}

	public int getLocalSize() {
		return other.getLocalSize();
	}

	public Object[] getLocal() {
		Object [] ol = other.getLocal();
		if (ol == null) {
			return null;
		}
		return Arrays.copyOf(ol, ol.length);
	}

	public int getStackSize() {
		return other.getStackSize();
	}

	public Object[] getStack() {
		Object [] ol = other.getStack();
		if (ol == null) {
			return null;
		}
		return Arrays.copyOf(ol, ol.length);
	}
}
