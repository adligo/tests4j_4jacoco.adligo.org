package org.adligo.tests4j_4jacoco.plugin.common;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * implementations of this interface alter the java byte code of 
 * a class so that the probe data is available for manipulation
 * at runtime.
 * 
 * @author scott
 *
 */
public interface I_ObtainProbesStrategy {
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
	int createProbeDataAccessorCall(MethodVisitor mv, int variable);

	/**
	 * if this strategy has a static $jacocoData member
	 * @return
	 */
	boolean hasJacocoData();
	/**
	 * alters the byte code so that it has a 
	 * static $jacocoData field
	 * 
	 * @param delegate
	 *            visitor to create fields and classes
	 */
	void createJacocoData(ClassVisitor delegate);

	/**
	 * if this strategy has a static jacocoInit() method
	 * @return
	 */
	boolean hasJacocoInit();
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
