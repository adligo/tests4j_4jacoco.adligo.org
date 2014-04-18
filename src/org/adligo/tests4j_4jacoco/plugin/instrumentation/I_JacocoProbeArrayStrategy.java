package org.adligo.tests4j_4jacoco.plugin.instrumentation;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public interface I_JacocoProbeArrayStrategy {
	/**
	 * Creates code that stores the probe array instance in the given variable.
	 * 
	 * @param mv
	 *            visitor to create code
	 * @param variable
	 *            variable index to store the probe Map to
	 *            
	 * @return maximum stack size required by the generated code
	 */
	int storeInstance(MethodVisitor mv, int variable);

	/**
	 * Adds additional class members required by this strategy.
	 * 
	 * @param delegate
	 *            visitor to create fields and classes
	 */
	void addMembers(ClassVisitor delegate);

}
