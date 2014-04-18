package org.adligo.tests4j_4jacoco.plugin.instrumentation;

import org.jacoco.core.JaCoCo;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class JacocoDuplicateFrameEliminator extends MethodVisitor {

		private boolean instruction;

		public JacocoDuplicateFrameEliminator(final MethodVisitor mv) {
			super(JaCoCo.ASM_API_VERSION, mv);
			instruction = true;
		}

		@Override
		public void visitFrame(final int type, final int nLocal,
				final Object[] local, final int nStack, final Object[] stack) {
			if (instruction) {
				instruction = false;
				mv.visitFrame(type, nLocal, local, nStack, stack);
			}
		}

		@Override
		public void visitInsn(final int opcode) {
			instruction = true;
			mv.visitInsn(opcode);
		}

		@Override
		public void visitIntInsn(final int opcode, final int operand) {
			instruction = true;
			mv.visitIntInsn(opcode, operand);
		}

		@Override
		public void visitVarInsn(final int opcode, final int var) {
			instruction = true;
			mv.visitVarInsn(opcode, var);
		}

		@Override
		public void visitTypeInsn(final int opcode, final String type) {
			instruction = true;
			mv.visitTypeInsn(opcode, type);
		}

		@Override
		public void visitFieldInsn(final int opcode, final String owner,
				final String name, final String desc) {
			instruction = true;
			mv.visitFieldInsn(opcode, owner, name, desc);
		}

		@Override
		public void visitMethodInsn(final int opcode, final String owner,
				final String name, final String desc) {
			instruction = true;
			mv.visitMethodInsn(opcode, owner, name, desc);
		}

		@Override
		public void visitInvokeDynamicInsn(final String name, final String desc,
				final Handle bsm, final Object... bsmArgs) {
			instruction = true;
			mv.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
		}

		@Override
		public void visitJumpInsn(final int opcode, final Label label) {
			instruction = true;
			mv.visitJumpInsn(opcode, label);
		}

		@Override
		public void visitLdcInsn(final Object cst) {
			instruction = true;
			mv.visitLdcInsn(cst);
		}

		@Override
		public void visitIincInsn(final int var, final int increment) {
			instruction = true;
			mv.visitIincInsn(var, increment);
		}

		@Override
		public void visitTableSwitchInsn(final int min, final int max,
				final Label dflt, final Label... labels) {
			instruction = true;
			mv.visitTableSwitchInsn(min, max, dflt, labels);
		}

		@Override
		public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
				final Label[] labels) {
			instruction = true;
			mv.visitLookupSwitchInsn(dflt, keys, labels);
		}

		@Override
		public void visitMultiANewArrayInsn(final String desc, final int dims) {
			instruction = true;
			mv.visitMultiANewArrayInsn(desc, dims);
		}

	}
