package org.adligo.tests4j_4jacoco.plugin.data;

public class FrameInfoMutant implements I_FrameInfo {
	private int type;
	private int localSize;
	private Object[] local;
	private int stackSize;
	private Object[] stack;
	
	public FrameInfoMutant() {}
	
	public FrameInfoMutant(I_FrameInfo info) {
		type = info.getType();
		localSize = info.getLocalSize();
		local = info.getLocal();
		stackSize = info.getStackSize();
		stack = info.getStack();
	}
	
	/* (non-Javadoc)
	 * @see org.adligo.tests4j_4jacoco.plugin.data.I_FrameInfo#getType()
	 */
	@Override
	public int getType() {
		return type;
	}
	/* (non-Javadoc)
	 * @see org.adligo.tests4j_4jacoco.plugin.data.I_FrameInfo#getnLocal()
	 */
	@Override
	public int getLocalSize() {
		return localSize;
	}
	/* (non-Javadoc)
	 * @see org.adligo.tests4j_4jacoco.plugin.data.I_FrameInfo#getLocal()
	 */
	@Override
	public Object[] getLocal() {
		return local;
	}
	/* (non-Javadoc)
	 * @see org.adligo.tests4j_4jacoco.plugin.data.I_FrameInfo#getnStack()
	 */
	@Override
	public int getStackSize() {
		return stackSize;
	}
	/* (non-Javadoc)
	 * @see org.adligo.tests4j_4jacoco.plugin.data.I_FrameInfo#getStack()
	 */
	@Override
	public Object[] getStack() {
		return stack;
	}
	public void setType(int type) {
		this.type = type;
	}
	public void setLocalSize(int nLocal) {
		this.localSize = nLocal;
	}
	public void setLocal(Object[] local) {
		this.local = local;
	}
	public void setStackSize(int nStack) {
		this.stackSize = nStack;
	}
	public void setStack(Object[] stack) {
		this.stack = stack;
	}
	
}
