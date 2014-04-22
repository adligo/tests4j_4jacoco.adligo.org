package org.adligo.tests4j_4jacoco.plugin.instrumentation.asm5;

import org.adligo.tests4j_4jacoco.plugin.asm.AsmApiVersion;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.I_ClassProbesVisitor;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.I_MethodProbesVisitor;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.I_ProbeIdGenerator;
import org.jacoco.core.JaCoCo;
import org.jacoco.core.internal.flow.IFrame;
import org.jacoco.core.internal.flow.LabelFlowAnalyzer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AnalyzerAdapter;

/**
 * A {@link org.objectweb.asm.ClassVisitor} that calculates probes for every
 * method.
 */
public class Asm5ClassProbesAdapter extends ClassVisitor implements
	I_ProbeIdGenerator {

	private static final I_MethodProbesVisitor EMPTY_METHOD_PROBES_VISITOR;

	static {
		class Impl extends MethodVisitor implements I_MethodProbesVisitor {

			public Impl() {
				super(AsmApiVersion.VERSION);
			}

			@Override
			public void visitProbe(final int probeId) {
				// nothing to do
			}

			@Override
			public void visitJumpInsnWithProbe(final int opcode,
					final Label label, final int probeId, final IFrame frame) {
				// nothing to do
			}

			@Override
			public void visitInsnWithProbe(final int opcode, final int probeId) {
				// nothing to do
			}

			@Override
			public void visitTableSwitchInsnWithProbes(final int min,
					final int max, final Label dflt, final Label[] labels,
					final IFrame frame) {
				// nothing to do
			}

			@Override
			public void visitLookupSwitchInsnWithProbes(final Label dflt,
					final int[] keys, final Label[] labels, final IFrame frame) {
				// nothing to do
			}

			@Override
			public MethodVisitor getThis() {
				return this;
			}
		}
		EMPTY_METHOD_PROBES_VISITOR = new Impl();
	}

	private static class ProbeCounter implements I_ProbeIdGenerator {
		int count = 0;

		public int nextId() {
			return count++;
		}
	}

	private final I_ClassProbesVisitor cv;

	private final boolean trackFrames;

	private int counter = 0;

	private String name;

	private boolean interfaceType;

	/**
	 * Creates a new adapter that delegates to the given visitor.
	 * 
	 * @param cv
	 *            instance to delegate to
	 * @param trackFrames
	 *            if <code>true</code> stackmap frames are tracked and provided
	 */
	public Asm5ClassProbesAdapter(final I_ClassProbesVisitor cv,
			final boolean trackFrames) {
		super(JaCoCo.ASM_API_VERSION, cv.getThis());
		this.cv = cv;
		this.trackFrames = trackFrames;
	}

	@Override
	public void visit(final int version, final int access, final String name,
			final String signature, final String superName,
			final String[] interfaces) {
		this.name = name;
		this.interfaceType = (access & Opcodes.ACC_INTERFACE) != 0;
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public final MethodVisitor visitMethod(final int access, final String name,
			final String desc, final String signature, final String[] exceptions) {
		final I_MethodProbesVisitor methodProbes;
		final I_MethodProbesVisitor mv = cv.visitMethodForProbes(access, name, desc,
				signature, exceptions);
		if (mv == null) {
			// We need to visit the method in any case, otherwise probe ids
			// are not reproducible
			methodProbes = EMPTY_METHOD_PROBES_VISITOR;
		} else {
			methodProbes = mv;
		}
		return new Asm5MethodSanatizer(null, access, name, desc, signature,
				exceptions) {

			@Override
			public void visitEnd() {
				super.visitEnd();
				LabelFlowAnalyzer.markLabels(this);
				if (interfaceType) {
					final ProbeCounter probeCounter = new ProbeCounter();
					final Asm5MethodProbesAdapter adapter = new Asm5MethodProbesAdapter(
							EMPTY_METHOD_PROBES_VISITOR,
							probeCounter);
					// We do not use the accept() method as ASM resets labels
					// after every call to accept()
					instructions.accept(adapter);
					cv.visitTotalProbeCount(probeCounter.count);
				}
				final Asm5MethodProbesAdapter probesAdapter = new Asm5MethodProbesAdapter(
						methodProbes, Asm5ClassProbesAdapter.this);
				if (trackFrames) {
					final AnalyzerAdapter analyzer = new AnalyzerAdapter(
							Asm5ClassProbesAdapter.this.name, access, name, desc,
							probesAdapter);
					probesAdapter.setAnalyzer(analyzer);
					this.accept(analyzer);
				} else {
					this.accept(probesAdapter);
				}
			}
		};
	}

	@Override
	public void visitEnd() {
		if (!interfaceType) {
			cv.visitTotalProbeCount(counter);
		}
		super.visitEnd();
	}

	// === IProbeIdGenerator ===

	public int nextId() {
		return counter++;
	}

}
