package org.adligo.tests4j_4jacoco.plugin.instrumentation.asm5;

import org.adligo.tests4j_4jacoco.plugin.asm.AsmApiVersion;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.JSRInlinerAdapter;

/**
 * This method visitor fixes two potential issues with Java byte code:
 * 
 * <ul>
 * <li>Remove JSR/RET instructions by inlining subroutines which are deprecated
 * since Java 6. The RET statement complicates control flow analysis as the jump
 * target is not explicitly given.</li>
 * <li>Remove code attributes line number and local variable name if they point
 * to invalid offsets which some tools create. When writing out such invalid
 * labels with ASM class files do not verify any more.</li>
 * </ul>
 */
public class Asm5MethodSanatizer extends JSRInlinerAdapter {

	public Asm5MethodSanatizer(final MethodVisitor mv, final int access,
			final String name, final String desc, final String signature,
			final String[] exceptions) {
		super(AsmApiVersion.VERSION, mv, access, name, desc, signature, exceptions);
	}

	@Override
	public void visitLocalVariable(final String name, final String desc,
			final String signature, final Label start, final Label end,
			final int index) {
		// Here we rely on the usage of the info fields by the tree API. If the
		// labels have been properly used before the info field contains a
		// reference to the LabelNode, otherwise null.
		if (start.info != null && end.info != null) {
			super.visitLocalVariable(name, desc, signature, start, end, index);
		}
	}

	@Override
	public void visitLineNumber(final int line, final Label start) {
		// Here we rely on the usage of the info fields by the tree API. If the
		// labels have been properly used before the info field contains a
		// reference to the LabelNode, otherwise null.
		if (start.info != null) {
			super.visitLineNumber(line, start);
		}
	}

}
