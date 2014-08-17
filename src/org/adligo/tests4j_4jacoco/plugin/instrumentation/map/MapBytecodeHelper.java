package org.adligo.tests4j_4jacoco.plugin.instrumentation.map;

import org.adligo.tests4j_4jacoco.plugin.asm.BytecodeInjectionDebuger;
import org.adligo.tests4j_4jacoco.plugin.common.I_StackHelper;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


public class MapBytecodeHelper {

	/**
	 * note the Map must be on top of the current stack
	 * when this method is called;
	 * 
	 * @param i
	 * @param p
	 * @param mv
	 * @return stack size
	 */
	public static void callMapPut(final I_StackHelper sh, final int i, 
			final boolean p, final MethodVisitor mv) {
		// Stack[+0]: Map
		
		
		mv.visitLdcInsn(i);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf",
				"(I)Ljava/lang/Integer;", false);
		if (BytecodeInjectionDebuger.isEnabled()) {
			BytecodeInjectionDebuger.putInStackDebug("" + i);
		}
		sh.incrementStackSize();
		
		// Stack[+1]: id
		// Stack[+0]: Map
		
		mv.visitLdcInsn("" + p);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf",
				"(Ljava/lang/String;)Ljava/lang/Boolean;", false);
		if (BytecodeInjectionDebuger.isEnabled()) {
			BytecodeInjectionDebuger.putInStackDebug("" + p);
		}
		sh.incrementStackSize();
		
		// Stack[+2]: false
		// Stack[+1]: id
		// Stack[+0]: Map
		if (BytecodeInjectionDebuger.isEnabled()) {
			BytecodeInjectionDebuger.log(sh, mv, 
					"putting map value " + i + " " + p);
		}
		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, MapInstrConstants.DATAFIELD_DESC, 
				"put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
		if (BytecodeInjectionDebuger.isEnabled()) {
			BytecodeInjectionDebuger.popOffStackDebug(3);
		}
		mv.visitInsn(Opcodes.POP);
		sh.decrementStackSize(3);
		//stack empty
	}
	
	/**
	 * puts the Map on top of the stack
	 * @param mv
	 * @param className
	 */
	public static void moveMapToStack(final I_StackHelper sh, MethodVisitor mv, String className) {
		// Load the value of the static data field:
		mv.visitFieldInsn(Opcodes.GETSTATIC, className,
				//InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC);
				InstrSupport.DATAFIELD_NAME, MapInstrConstants.DATAFIELD_CLAZZ);
		if (BytecodeInjectionDebuger.isEnabled()) {
			BytecodeInjectionDebuger.putInStackDebug(MapInstrConstants.DATAFIELD_CLAZZ);
		}
		sh.incrementStackSize();
		
		// Stack[0]: Map
	}
	
	/**
	 * put the map in the staic field
	 * @param mv
	 * @param className
	 * @return the maxium stack size
	 */
	public static void moveMapToField(final I_StackHelper sh, MethodVisitor mv, String className) {
		// Stack[0]: Map

		mv.visitInsn(Opcodes.DUP);
		if (BytecodeInjectionDebuger.isEnabled()) {
			BytecodeInjectionDebuger.dupStackDebug();
		}
		sh.incrementStackSize();
		// Stack[1]: Map
		// Stack[0]: Map
		
		// Load the value of the static data field:
		mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
				//InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC);
				InstrSupport.DATAFIELD_NAME, MapInstrConstants.DATAFIELD_CLAZZ);
		if (BytecodeInjectionDebuger.isEnabled()) {
			BytecodeInjectionDebuger.popOffStackDebug();
		}
		sh.decrementStackSize();
		// Stack[0]: Map
	}
	
}
