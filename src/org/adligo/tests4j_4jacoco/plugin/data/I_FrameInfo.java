package org.adligo.tests4j_4jacoco.plugin.data;

public interface I_FrameInfo {

	public abstract int getType();

	public abstract int getLocalSize();

	public abstract Object[] getLocal();

	public abstract int getStackSize();

	public abstract Object[] getStack();

}