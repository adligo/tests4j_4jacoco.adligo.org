package org.adligo.tests4j_4jacoco.plugin.instrumentation.boolean_array;

import org.adligo.tests4j_4jacoco.plugin.instrumentation.MapInstrConstants;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.AbstractProbeInserter;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ObtainProbesStrategy;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class BooleanArrayProbeInserter extends AbstractProbeInserter {

	public BooleanArrayProbeInserter(final int access, final String desc, final MethodVisitor mv,
	final I_ObtainProbesStrategy arrayStrategy) {
		super(access, desc, mv, arrayStrategy);
	}


	public void insertProbe(final int id) {

		// For a probe we set the corresponding position in the boolean[] array
		// to true.

		mv.visitVarInsn(Opcodes.ALOAD, variable);

		// Stack[0]: [Z

		InstrSupport.push(mv, id);

		// Stack[1]: I
		// Stack[0]: [Z

		mv.visitInsn(Opcodes.ICONST_1);

		// Stack[2]: I
		// Stack[1]: I
		// Stack[0]: [Z

		mv.visitInsn(Opcodes.BASTORE);
	}

	@Override
	public void visitCode() {
		variable = newLocal(Type.getType(InstrSupport.DATAFIELD_DESC));
		accessorStackSize = arrayStrategy.createProbeDataAccessorCall(mv, variable);
		mv.visitCode();
	}

	@Override
	public void visitMaxs(final int maxStack, final int maxLocals) {
		// Max stack size of the probe code is 3 which can add to the
		// original stack size depending on the probe locations. The accessor
		// stack size is an absolute maximum, as the accessor code is inserted
		// at the very beginning of each method when the stack size is empty.
		final int increasedStack = Math.max(maxStack + 3, accessorStackSize);
		mv.visitMaxs(increasedStack, maxLocals + 1);
	}


	
}
