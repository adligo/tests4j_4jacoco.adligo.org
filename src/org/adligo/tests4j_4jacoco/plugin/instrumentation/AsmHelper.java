package org.adligo.tests4j_4jacoco.plugin.instrumentation;

import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


public class AsmHelper {

	public static void callMapPut(int i, boolean p, MethodVisitor mv) {
		// Stack[0]: Map
		
		
		mv.visitLdcInsn(i);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf",
				"(I)Ljava/lang/Integer;");
		
		mv.visitLdcInsn("" + p);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf",
				"(Ljava/lang/String;)Ljava/lang/Boolean;");
		
		// Stack[2]: false
		// Stack[1]: id
		// Stack[0]: Map
				
		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, JacocoInstrConstants.DATAFIELD_DESC, 
				"put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
	}
	
	public static void moveMapToStack(MethodVisitor mv, String className) {
		// Load the value of the static data field:
		mv.visitFieldInsn(Opcodes.GETSTATIC, className,
				//InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC);
				InstrSupport.DATAFIELD_NAME, JacocoInstrConstants.DATAFIELD_CLAZZ);
		
		// Stack[0]: Map
	}
	
	public static void moveMapToField(MethodVisitor mv, String className) {
		// Load the value of the static data field:
		mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
				//InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC);
				InstrSupport.DATAFIELD_NAME, JacocoInstrConstants.DATAFIELD_CLAZZ);
		//nostack
	}
	
	public static void createMap(MethodVisitor mv) {
		mv.visitTypeInsn(Opcodes.NEW, JacocoInstrConstants.DATAFIELD_DESC);
	}
}
