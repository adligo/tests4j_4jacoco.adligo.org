package org.adligo.tests4j_4jacoco.plugin.asm;


public class StackHelper implements I_StackHelper {
	public static final String STACK_SIZE_CAN_T_BE_LESS_THAN_0 = "Stack Size can't be less than 0";
	private int maxStackSize = 0;
	private int currentStackSize = 0;
	
	public StackHelper() {}
	
	public StackHelper(I_StackHelper other) {
		maxStackSize = other.getMaxStackSize();
		currentStackSize = other.getCurrentStackSize();
	}
	
	/* (non-Javadoc)
	 * @see org.adligo.tests4j_4jacoco.plugin.asm.I_StackHelper#incrementStackSize()
	 */
	@Override
	public void incrementStackSize() {
		incrementStackSize(1);
	}
	
	/* (non-Javadoc)
	 * @see org.adligo.tests4j_4jacoco.plugin.asm.I_StackHelper#incrementStackSize(int)
	 */
	@Override
	public void incrementStackSize(int i) {
		currentStackSize = currentStackSize + i;
		if (currentStackSize >  maxStackSize) {
			maxStackSize = currentStackSize;
		}
	}
	/* (non-Javadoc)
	 * @see org.adligo.tests4j_4jacoco.plugin.asm.I_StackHelper#decrementStackSize()
	 */
	@Override
	public void decrementStackSize() {
		decrementStackSize(1);
	}
	
	/* (non-Javadoc)
	 * @see org.adligo.tests4j_4jacoco.plugin.asm.I_StackHelper#decrementStackSize(int)
	 */
	@Override
	public void decrementStackSize(int p) {
		currentStackSize = currentStackSize - p;
		if (currentStackSize <= -1) {
			throw new IllegalStateException(STACK_SIZE_CAN_T_BE_LESS_THAN_0);
		}
	}
	/* (non-Javadoc)
	 * @see org.adligo.tests4j_4jacoco.plugin.asm.I_StackHelper#toString()
	 */
	@Override
	public String toString() {
		return "AsmStackHelper [" + currentStackSize + "/" + maxStackSize + "]";
	}

	/* (non-Javadoc)
	 * @see org.adligo.tests4j_4jacoco.plugin.asm.I_StackHelper#getMaxStackSize()
	 */
	@Override
	public int getMaxStackSize() {
		return maxStackSize;
	}

	/* (non-Javadoc)
	 * @see org.adligo.tests4j_4jacoco.plugin.asm.I_StackHelper#getCurrentStackSize()
	 */
	@Override
	public int getCurrentStackSize() {
		return currentStackSize;
	}
}
