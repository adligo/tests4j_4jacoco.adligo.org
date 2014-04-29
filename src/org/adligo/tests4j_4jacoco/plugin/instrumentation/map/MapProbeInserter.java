package org.adligo.tests4j_4jacoco.plugin.instrumentation.map;

import org.adligo.tests4j_4jacoco.plugin.asm.BytecodeInjectionDebuger;
import org.adligo.tests4j_4jacoco.plugin.asm.StackHelper;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.AbstractProbeInserter;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ObtainProbesStrategy;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class MapProbeInserter extends AbstractProbeInserter {
	private int insertProbeStackSize;
	
	public MapProbeInserter(final int access, final String desc, final MethodVisitor mv,
	final I_ObtainProbesStrategy arrayStrategy) {
		super(access, desc, mv, arrayStrategy);
	}


	public void insertProbe(final int probeIndex) {
		StackHelper sh = new StackHelper();
		
		if (BytecodeInjectionDebuger.isEnabled()) {
			BytecodeInjectionDebuger.log(sh, mv, 
					"entering insertProbe " + probeIndex);
		}
		
		// For a probe we set the corresponding position in the Map
		// to true.
		mv.visitVarInsn(Opcodes.ALOAD, variable);
		sh.incrementStackSize();
		if (BytecodeInjectionDebuger.isEnabled()) {
			BytecodeInjectionDebuger.putInStackDebug(MapInstrConstants.DATAFIELD_CLAZZ);
		}
		
		// Stack[0]:Map
		
		mv.visitInsn(Opcodes.DUP);
		sh.incrementStackSize();
		if (BytecodeInjectionDebuger.isEnabled()) {
			BytecodeInjectionDebuger.dupStackDebug();
		}
		
		// Stack[1]:Map
		// Stack[0]:Map
		
		MapBytecodeHelper.callMapPut(sh, probeIndex, true, mv);
		// Stack[0]:Map

		mv.visitVarInsn(Opcodes.ASTORE, variable);
		sh.decrementStackSize();
		if (BytecodeInjectionDebuger.isEnabled()) {
			BytecodeInjectionDebuger.popOffStackDebug();
			BytecodeInjectionDebuger.log(sh, mv, " stackCheck ");
			
		}
		insertProbeStackSize = sh.getMaxStackSize();
		//nothing on the stack at this scope
	}

	@Override
	public void visitCode() {
		variable = newLocal(Type.getType(MapInstrConstants.DATAFIELD_DESC));
		accessorStackSize = arrayStrategy.createProbeDataAccessorCall(mv, variable);
		mv.visitCode();
	}


	@Override
	public void visitMaxs(final int maxStack, final int maxLocals) {
		// Max stack size of the probe code is 4 which can add to the
		// original stack size depending on the probe locations. The accessor
		// stack size is an absolute maximum, as the accessor code is inserted
		// at the very beginning of each method when the stack size is empty.
		final int increasedStack = Math.max(maxStack + insertProbeStackSize, accessorStackSize);
		mv.visitMaxs(increasedStack, maxLocals + 1);
	}
	
}
