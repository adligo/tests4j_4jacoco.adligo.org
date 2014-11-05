package org.adligo.tests4j_4jacoco.plugin.analysis.common;

import org.adligo.tests4j.models.shared.coverage.I_Probes;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.MethodAnalyzer;
import org.jacoco.core.internal.analysis.StringPool;
import org.jacoco.core.internal.flow.ClassProbesVisitor;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

public class ClassProbesAnalyzer extends ClassProbesVisitor {

	private final long classid;
	private final boolean noMatch;
	private final I_Probes probes;
	private final StringPool stringPool;

	private ClassCoverageImpl coverage;

	/**
	 * Creates a new analyzer that builds coverage data for a class.
	 * 
	 * @param classid
	 *            id of the class
	 * @param noMatch
	 *            <code>true</code> if class id does not match with execution
	 *            data
	 * @param probes
	 *            execution data for this class or <code>null</code>
	 * @param stringPool
	 *            shared pool to minimize the number of {@link String} instances
	 */
	public ClassProbesAnalyzer(final long classid, final boolean noMatch,
			final I_Probes probes, final StringPool stringPool) {
		this.classid = classid;
		this.noMatch = noMatch;
		this.probes = probes;
		this.stringPool = stringPool;
	}

	/**
	 * Returns the coverage data for this class after this visitor has been
	 * processed.
	 * 
	 * @return coverage data for this class
	 */
	public ClassCoverageImpl getCoverage() {
		return coverage;
	}

	@Override
	public void visit(final int version, final int access, final String name,
			final String signature, final String superName,
			final String[] interfaces) {
		this.coverage = new ClassCoverageImpl(stringPool.get(name), classid,
				noMatch, stringPool.get(signature), stringPool.get(superName),
				stringPool.get(interfaces));
	}

	@Override
	public void visitSource(final String source, final String debug) {
		this.coverage.setSourceFileName(stringPool.get(source));
	}

	@Override
	public MethodProbesVisitor visitMethod(final int access, final String name,
			final String desc, final String signature, final String[] exceptions) {

		InstrSupport.assertNotInstrumented(name, coverage.getName());

		// TODO: Use filter hook
		if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
			return null;
		}

		return new MethodProbesAnalyzer(stringPool.get(name), stringPool.get(desc),
				stringPool.get(signature), probes) {
			@Override
			public void visitEnd() {
				super.visitEnd();
				final IMethodCoverage methodCoverage = getCoverage();
				if (methodCoverage.getInstructionCounter().getTotalCount() > 0) {
					// Only consider methods that actually contain code
					coverage.addMethod(methodCoverage);
				}
			}
		};
	}

	@Override
	public FieldVisitor visitField(final int access, final String name,
			final String desc, final String signature, final Object value) {
		InstrSupport.assertNotInstrumented(name, coverage.getName());
		return super.visitField(access, name, desc, signature, value);
	}

	@Override
	public void visitTotalProbeCount(final int count) {
		// nothing to do
	}

}