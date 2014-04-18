package org.adligo.tests4j_4jacoco.plugin.asm;

import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * The Jacoco DataFetcher is the part of the LoggingRuntime
 * which requests data from the Jacoco instrumented code
 * to the RuntimeData instance.
 * 
 * This was extracted mostly for comments and method extraction;
 * originally in generateArgumentArray with the same name in LoggingRuntime.
 * 
 * @author scott
 *
 */
public class ClassCoverageDataParamFactory {
	private final MethodVisitor methodVisitor;
	
	public ClassCoverageDataParamFactory(MethodVisitor p) {
		methodVisitor = p;
	}
	/**
	 * Generates code that creates the argument array for the
	 * {@link #getProbes(Object[])} method. The array instance is left on the
	 * operand stack. The generated code requires a stack size of 5.
	 * 
	 * @param classid
	 *            class identifier
	 * @param classname
	 *            VM class name
	 * @param probecount
	 *            probe count for this class
	 * @param methodVisitor
	 *            visitor to emit generated code
	 */
	public void create(final long classid,
			final String classname, final int probecount) {
		
		methodVisitor.visitInsn(Opcodes.ICONST_3);
		//Stack[0] = int 3
		
		//pull 3 off the stack and create a new Object [3]
		methodVisitor.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");

		putClassIdInObjectArray(classid);
		putClassNameInObjectArray(classname);
		putProbeCountInObjectArray(probecount);
		
		//Stack[0] = int 3
	}

	/**
	 * put the probe count in Object[3]
	 * @param probecount
	 * @param methodVisitor
	 */
	private void putProbeCountInObjectArray(final int probecount) {
		//Stack[0] is a Object[3]
		
		//add a reference to the Object[] on the stack 
		methodVisitor.visitInsn(Opcodes.DUP);
		//put the number 2 on the stack
		methodVisitor.visitInsn(Opcodes.ICONST_2);
		//put a int for the probecount on the stack
		InstrSupport.push(methodVisitor, probecount);
		methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer",
				"valueOf", "(I)Ljava/lang/Integer;");
		
		//Stack[3] is a Integer for the probecount 
		//Stack[2] is a int 2
		//Stack[1] is a dup of the Stack[0] Object[3]
		//Stack[0] is a Object[3]
		
		//copy the probecount to Object[2] from the stack
		methodVisitor.visitInsn(Opcodes.AASTORE);
		
		//Stack[0] is a Object[3]
	}

	/**
	 * copy the class name to Object[2]
	 * 
	 * @param classname
	 * @param methodVisitor
	 */
	private void putClassNameInObjectArray(final String classname) {
		//Stack[0] is a Object[3]

		//add a reference to the Object[] on the stack 
		methodVisitor.visitInsn(Opcodes.DUP);
		//put the number 1 on the stack
		methodVisitor.visitInsn(Opcodes.ICONST_1);
		//put a String for the classname on the stack
		methodVisitor.visitLdcInsn(classname);
		
		//Stack[3] is a String for the classname 
		//Stack[2] is a int 1
		//Stack[1] is a dup of the Stack[0] Object[3]
		//Stack[0] is a Object[3]
		
		//copy the classname to Object[1] from the stack
		methodVisitor.visitInsn(Opcodes.AASTORE);
		
		//Stack[0] is a Object[3]
	}

	/**
	 * put classid in the Object[0]
	 * @param classid
	 * @param methodVisitor
	 */
	private void putClassIdInObjectArray(final long classid) {
		//Stack[0] is a Object[3]
		
		//add a reference to the Object[] on the stack 
		methodVisitor.visitInsn(Opcodes.DUP);
		//put the number 0 on the stack
		methodVisitor.visitInsn(Opcodes.ICONST_0);
		//put a Long for the class id on the stack
		methodVisitor.visitLdcInsn(Long.valueOf(classid));
		methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf",
				"(J)Ljava/lang/Long;");
		
		//Stack[3] is a Long for the classid 
		//Stack[2] is a int 0
		//Stack[1] is a dup of stack[0]
		//Stack[0] is a Object[3]
		
		//copy the classid to Object[0] from the stack
		methodVisitor.visitInsn(Opcodes.AASTORE);
		
		//Stack[0] is a Object[3]
	}
}
