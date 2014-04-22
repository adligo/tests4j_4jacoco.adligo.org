package org.adligo.tests4j_4jacoco.plugin.instrumentation;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public interface I_ClassProbesVisitor {

	/**
	 * like a cast
	 * @return
	 */
	public ClassVisitor getThis();
	
	/**
	 * When visiting a method we need a {@link MethodVisitor} to handle
	 * the probes of that method.
	 */
	public abstract I_MethodProbesVisitor visitMethodForProbes(int access, String name,
			String desc, String signature, String[] exceptions);

	/**
	 * Reports the total number of encountered probes. For classes this method
	 * is called just before {@link ClassVisitor#visitEnd()}. For interfaces
	 * this method is called before the first method (the static initializer) is
	 * emitted.
	 * 
	 * @param count
	 *            total number of probes
	 */
	public abstract void visitTotalProbeCount(int count);
}
