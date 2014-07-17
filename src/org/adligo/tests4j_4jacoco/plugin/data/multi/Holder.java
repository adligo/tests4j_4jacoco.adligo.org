package org.adligo.tests4j_4jacoco.plugin.data.multi;

public class Holder<T> {
	private T held;
	
	public synchronized T getHeld() {
		return held;
	}

	public synchronized void setHeld(T held) {
		this.held = held;
	}
}
