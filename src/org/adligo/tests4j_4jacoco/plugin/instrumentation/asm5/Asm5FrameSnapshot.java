package org.adligo.tests4j_4jacoco.plugin.instrumentation.asm5;

import java.util.ArrayList;
import java.util.List;

import org.jacoco.core.internal.flow.IFrame;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AnalyzerAdapter;

public class Asm5FrameSnapshot implements IFrame {

		private static final Asm5FrameSnapshot NOP = new Asm5FrameSnapshot(null, null);

		private final Object[] locals;
		private final Object[] stack;

		private Asm5FrameSnapshot(final Object[] locals, final Object[] stack) {
			this.locals = locals;
			this.stack = stack;
		}

		/**
		 * Create a IFrame instance based on the given analyzer.
		 * 
		 * @param analyzer
		 *            analyzer instance or <code>null</code>
		 * @param popCount
		 *            number of items to remove from the operand stack
		 * @return IFrame instance. In case the analyzer is <code>null</code> or
		 *         does not contain stackmap information a "NOP" IFrame is returned.
		 */
		static IFrame create(final AnalyzerAdapter analyzer, final int popCount) {
			if (analyzer == null || analyzer.locals == null) {
				return NOP;
			}
			@SuppressWarnings("unchecked")
			final List<Object> locals = analyzer.locals, stack = analyzer.stack;
			return new Asm5FrameSnapshot(reduce(locals, 0), reduce(stack, popCount));
		}

		/**
		 * Reduce double word types into a single slot as required
		 * {@link MethodVisitor#visitFrame(int, int, Object[], int, Object[])}
		 * method.
		 */
		private static Object[] reduce(final List<Object> source, final int popCount) {
			final List<Object> copy = new ArrayList<Object>(source);
			final int size = source.size() - popCount;
			copy.subList(size, source.size()).clear();
			for (int i = size; --i >= 0;) {
				final Object type = source.get(i);
				if (type == Opcodes.LONG || type == Opcodes.DOUBLE) {
					copy.remove(i + 1);
				}
			}
			return copy.toArray();
		}

		// === IFrame implementation ===

		public void accept(final MethodVisitor mv) {
			if (locals != null) {
				mv.visitFrame(Opcodes.F_NEW, locals.length, locals, stack.length,
						stack);
			}
		}

	}

