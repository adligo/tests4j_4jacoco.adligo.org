package org.adligo.tests4j_4jacoco.plugin.instrumentation;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public interface I_JacocoProbeArrayStrategy {
	/**
	 * Alters the byte code so that it stores the probe data instance
	 * returned in the given local variable slot.
	 * 
	 * @param mv
	 *            visitor to create code
	 * @param variable
	 *            variable index to store the probe data to
	 *            
	 * @return maximum stack size required by the generated code
	 */
	int createPutDataInLocal(MethodVisitor mv, int variable);

	/**
	 * alters the byte code so that it has a 
	 * static $jacocoData field
	 * 
	 * @param delegate
	 *            visitor to create fields and classes
	 */
	void createJacocoData(ClassVisitor delegate);

	/**
	 * alters the byte code so that it has a method
	 * static boolean [] jacocoInit() {}
	 * 
	 * where data type can be 
	 * 
	 * @param delegate
	 *            visitor to create fields and classes
	 */
	void createJacocoInit(ClassVisitor delegate);
}
