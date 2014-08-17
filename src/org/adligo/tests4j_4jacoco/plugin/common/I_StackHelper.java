package org.adligo.tests4j_4jacoco.plugin.common;

public interface I_StackHelper {

	public abstract void incrementStackSize();

	public abstract void incrementStackSize(int i);

	public abstract void decrementStackSize();

	public abstract void decrementStackSize(int p);

	public abstract String toString();

	public abstract int getMaxStackSize();

	public abstract int getCurrentStackSize();

}