package org.adligo.tests4j_4jacoco.plugin.instrumentation.asm5;

import org.adligo.tests4j_4jacoco.plugin.asm.AsmApiVersion;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.I_MethodProbesVisitor;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.I_ProbeIdGenerator;
import org.jacoco.core.internal.flow.IFrame;
import org.jacoco.core.internal.flow.LabelInfo;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AnalyzerAdapter;

public class Asm5MethodProbesAdapter extends MethodVisitor {

		private final I_MethodProbesVisitor probesVisitor;

		private final I_ProbeIdGenerator idGenerator;

		private AnalyzerAdapter analyzer;

		/**
		 * Create a new adapter instance.
		 * 
		 * @param probesVisitor
		 *            visitor to delegate to
		 * @param idGenerator
		 *            generator for unique probe ids
		 */
		public Asm5MethodProbesAdapter(final I_MethodProbesVisitor probesVisitor,
				
				final I_ProbeIdGenerator idGenerator) {
			super(AsmApiVersion.VERSION, probesVisitor.getThis());
			this.probesVisitor = probesVisitor;
			this.idGenerator = idGenerator;
		}

		/**
		 * If an analyzer is set {@link IFrame} handles are calculated and emitted
		 * to the probes methods.
		 * 
		 * @param analyzer
		 *            optional analyzer to set
		 */
		public void setAnalyzer(final AnalyzerAdapter analyzer) {
			this.analyzer = analyzer;
		}

		@Override
		public void visitLabel(final Label label) {
			if (LabelInfo.isMultiTarget(label) && LabelInfo.isSuccessor(label)) {
				probesVisitor.visitProbe(idGenerator.nextId());
			}
			mv.visitLabel(label);
		}

		@Override
		public void visitInsn(final int opcode) {
			switch (opcode) {
			case Opcodes.IRETURN:
			case Opcodes.LRETURN:
			case Opcodes.FRETURN:
			case Opcodes.DRETURN:
			case Opcodes.ARETURN:
			case Opcodes.RETURN:
			case Opcodes.ATHROW:
				probesVisitor.visitInsnWithProbe(opcode, idGenerator.nextId());
				break;
			default:
				mv.visitInsn(opcode);
				break;
			}
		}

		@Override
		public void visitJumpInsn(final int opcode, final Label label) {
			if (LabelInfo.isMultiTarget(label)) {
				probesVisitor.visitJumpInsnWithProbe(opcode, label,
						idGenerator.nextId(), frame(jumpPopCount(opcode)));
			} else {
				mv.visitJumpInsn(opcode, label);
			}
		}

		private int jumpPopCount(final int opcode) {
			switch (opcode) {
			case Opcodes.GOTO:
				return 0;
			case Opcodes.IFEQ:
			case Opcodes.IFNE:
			case Opcodes.IFLT:
			case Opcodes.IFGE:
			case Opcodes.IFGT:
			case Opcodes.IFLE:
			case Opcodes.IFNULL:
			case Opcodes.IFNONNULL:
				return 1;
			default: // IF_CMPxx and IF_ACMPxx
				return 2;
			}
		}

		@Override
		public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
				final Label[] labels) {
			if (markLabels(dflt, labels)) {
				probesVisitor.visitLookupSwitchInsnWithProbes(dflt, keys, labels,
						frame(1));
			} else {
				mv.visitLookupSwitchInsn(dflt, keys, labels);
			}
		}

		@Override
		public void visitTableSwitchInsn(final int min, final int max,
				final Label dflt, final Label... labels) {
			if (markLabels(dflt, labels)) {
				probesVisitor.visitTableSwitchInsnWithProbes(min, max, dflt,
						labels, frame(1));
			} else {
				mv.visitTableSwitchInsn(min, max, dflt, labels);
			}
		}

		private boolean markLabels(final Label dflt, final Label[] labels) {
			boolean probe = false;
			LabelInfo.resetDone(labels);
			if (LabelInfo.isMultiTarget(dflt)) {
				LabelInfo.setProbeId(dflt, idGenerator.nextId());
				probe = true;
			}
			LabelInfo.setDone(dflt);
			for (final Label l : labels) {
				if (LabelInfo.isMultiTarget(l) && !LabelInfo.isDone(l)) {
					LabelInfo.setProbeId(l, idGenerator.nextId());
					probe = true;
				}
				LabelInfo.setDone(l);
			}
			return probe;
		}

		private IFrame frame(final int popCount) {
			return Asm5FrameSnapshot.create(analyzer, popCount);
		}

	}