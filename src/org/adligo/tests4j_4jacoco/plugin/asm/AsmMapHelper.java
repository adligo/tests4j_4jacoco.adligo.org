package org.adligo.tests4j_4jacoco.plugin.asm;

import org.adligo.tests4j_4jacoco.plugin.instrumentation.MapInstrConstants;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


public class AsmMapHelper {

	/**
	 * note the Map must be on top of the current stack
	 * when this method is called;
	 * 
	 * @param i
	 * @param p
	 * @param mv
	 * @return stack size
	 */
	public static int callMapPut(int i, boolean p, MethodVisitor mv) {
		// Stack[0]: Map
		
		mv.visitInsn(Opcodes.DUP);

		// Stack[1]: Map
		// Stack[0]: Map
		
		mv.visitLdcInsn(i);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf",
				"(I)Ljava/lang/Integer;", false);
		
		// Stack[2]: id
		// Stack[1]: Map
		// Stack[0]: Map
		
		mv.visitLdcInsn("" + p);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf",
				"(Ljava/lang/String;)Ljava/lang/Boolean;", false);
		
		// Stack[3]: false
		// Stack[2]: id
		// Stack[1]: Map
		// Stack[0]: Map
				
		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, MapInstrConstants.DATAFIELD_DESC, 
				"put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
		
		// Stack[0]: Map
		
		return 4;
	}
	
	/**
	 * puts the Map on top of the stack
	 * @param mv
	 * @param className
	 */
	public static void moveMapToStack(MethodVisitor mv, String className) {
		// Load the value of the static data field:
		mv.visitFieldInsn(Opcodes.GETSTATIC, className,
				//InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC);
				InstrSupport.DATAFIELD_NAME, MapInstrConstants.DATAFIELD_CLAZZ);
	}
	
	/**
	 * put the map in the staic field
	 * @param mv
	 * @param className
	 * @return the maxium stack size
	 */
	public static int moveMapToField(MethodVisitor mv, String className) {
		// Stack[0]: Map

		mv.visitInsn(Opcodes.DUP);

		// Stack[1]: Map
		// Stack[0]: Map
		
		// Load the value of the static data field:
		mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
				//InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC);
				InstrSupport.DATAFIELD_NAME, MapInstrConstants.DATAFIELD_CLAZZ);
		
		// Stack[0]: Map
		return 2;
	}
	
	public static void createMap(MethodVisitor mv) {
		mv.visitTypeInsn(Opcodes.NEW, MapInstrConstants.DATAFIELD_DESC);
	}
}
