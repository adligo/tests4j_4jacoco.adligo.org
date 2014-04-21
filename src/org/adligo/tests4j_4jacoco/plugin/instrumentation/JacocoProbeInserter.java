package org.adligo.tests4j_4jacoco.plugin.instrumentation;

import org.adligo.tests4j_4jacoco.plugin.asm.AsmMapHelper;
import org.jacoco.core.JaCoCo;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class JacocoProbeInserter extends MethodVisitor implements I_JacocoProbeInserter {

	private final I_JacocoProbeArrayStrategy arrayStrategy;

	/** Position of the inserted variable. 
	 *  So this is the Map's local variable location for $jacocoData
	 */
	private final int variable;

	/** Maximum stack usage of the code to access the probe array. */
	private int accessorStackSize;

	/**
	 * Creates a new {@link ProbeInserter}.
	 * 
	 * @param access
	 *            access flags of the adapted method.
	 * @param desc
	 *            the method's descriptor
	 * @param mv
	 *            the method visitor to which this adapter delegates calls
	 * @param arrayStrategy
	 *            callback to create the code that retrieves the reference to
	 *            the probe array
	 */
	JacocoProbeInserter(final int access, final String desc, final MethodVisitor mv,
			final I_JacocoProbeArrayStrategy arrayStrategy) {
		super(JaCoCo.ASM_API_VERSION, mv);
		this.arrayStrategy = arrayStrategy;
		int pos = (Opcodes.ACC_STATIC & access) == 0 ? 1 : 0;
		for (final Type t : Type.getArgumentTypes(desc)) {
			pos += t.getSize();
		}
		variable = pos;
	}

	/**
	 * probeIndex ok I renamed this from id (id to what)
	 *     I think it is the probeIndex that we are setting to true;
	 *     
	 * TODO ok I think this is where the booleans go
	 * need to call put on the map
	 */
	public void insertProbe(final int probeIndex) {
		// For a probe we set the corresponding position in the Map
		// to true.
		mv.visitVarInsn(Opcodes.ALOAD, variable);
		
		// Stack[0]:Map
		
		AsmMapHelper.callMapPut(probeIndex, true, mv);
		// Stack[0]:Map

		mv.visitVarInsn(Opcodes.ASTORE, variable);
		
		//nothing on the stack at this scope
	}

	@Override
	public void visitCode() {
		accessorStackSize = arrayStrategy.storeInstance(mv, variable);
		mv.visitCode();
	}

	@Override
	public final void visitVarInsn(final int opcode, final int var) {
		mv.visitVarInsn(opcode, map(var));
	}

	@Override
	public final void visitIincInsn(final int var, final int increment) {
		mv.visitIincInsn(map(var), increment);
	}

	@Override
	public final void visitLocalVariable(final String name, final String desc,
			final String signature, final Label start, final Label end,
			final int index) {
		mv.visitLocalVariable(name, desc, signature, start, end, map(index));
	}

	@Override
	public void visitMaxs(final int maxStack, final int maxLocals) {
		// Max stack size of the probe code is 4 which can add to the
		// original stack size depending on the probe locations. The accessor
		// stack size is an absolute maximum, as the accessor code is inserted
		// at the very beginning of each method when the stack size is empty.
		final int increasedStack = Math.max(maxStack + 4, accessorStackSize);
		mv.visitMaxs(increasedStack, maxLocals + 1);
	}

	private int map(final int var) {
		if (var < variable) {
			return var;
		} else {
			return var + 1;
		}
	}

	@Override
	public final void visitFrame(final int type, final int nLocal,
			final Object[] local, final int nStack, final Object[] stack) {

		if (type != Opcodes.F_NEW) { // uncompressed frame
			throw new IllegalArgumentException(
					"ClassReader.accept() should be called with EXPAND_FRAMES flag");
		}

		final Object[] newLocal = new Object[Math.max(nLocal, variable) + 1];
		int idx = 0; // Arrays index for existing locals
		int newIdx = 0; // Array index for new locals
		int pos = 0; // Current variable position
		while (idx < nLocal || pos <= variable) {
			if (pos == variable) {
				//newLocal[newIdx++] = InstrSupport.DATAFIELD_DESC;
				newLocal[newIdx++] = MapInstrConstants.DATAFIELD_DESC;
				pos++;
			} else {
				if (idx < nLocal) {
					final Object t = local[idx++];
					newLocal[newIdx++] = t;
					pos++;
					if (t == Opcodes.LONG || t == Opcodes.DOUBLE) {
						pos++;
					}
				} else {
					// Fill unused slots with TOP
					newLocal[newIdx++] = Opcodes.TOP;
					pos++;
				}
			}
		}
		mv.visitFrame(type, newIdx, newLocal, nStack, stack);
	}

}