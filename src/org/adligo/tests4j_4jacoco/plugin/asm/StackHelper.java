package org.adligo.tests4j_4jacoco.plugin.asm;


public class StackHelper {
	public static final String STACK_SIZE_CAN_T_BE_LESS_THAN_0 = "Stack Size can't be less than 0";
	private int maxStackSize = 0;
	private int currentStackSize = 0;
	
	public void incrementStackSize() {
		incrementStackSize(1);
	}
	
	public void incrementStackSize(int i) {
		currentStackSize = currentStackSize + i;
		if (currentStackSize >  maxStackSize) {
			maxStackSize = currentStackSize;
		}
	}
	public void decrementStackSize() {
		decrementStackSize(1);
	}
	
	public void decrementStackSize(int p) {
		currentStackSize = currentStackSize - p;
		if (currentStackSize <= -1) {
			throw new IllegalStateException(STACK_SIZE_CAN_T_BE_LESS_THAN_0);
		}
	}
	public String toString() {
		return "AsmStackHelper [" + currentStackSize + "/" + maxStackSize + "]";
	}

	public int getMaxStackSize() {
		return maxStackSize;
	}

	public int getCurrentStackSize() {
		return currentStackSize;
	}
}
