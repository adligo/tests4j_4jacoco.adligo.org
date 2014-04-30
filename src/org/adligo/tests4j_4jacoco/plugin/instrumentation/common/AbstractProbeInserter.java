package org.adligo.tests4j_4jacoco.plugin.instrumentation.common;

import java.util.Map;

import org.adligo.tests4j_4jacoco.plugin.asm.ApiVersion;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

public abstract class AbstractProbeInserter extends LocalVariablesSorter {

	protected final I_ObtainProbesStrategy arrayStrategy;

	/** Position of the inserted variable for the probe data. 
	 */
	protected int variable;

	/** Maximum stack usage of the code to access the probe data. */
	protected int accessorStackSize;
	
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
	public AbstractProbeInserter(final int access, final String desc, final MethodVisitor mv,
			final I_ObtainProbesStrategy arrayStrategy) {
		super(ApiVersion.VERSION, access, desc,  mv);
		this.arrayStrategy = arrayStrategy;
		int pos = (Opcodes.ACC_STATIC & access) == 0 ? 1 : 0;
		for (final Type t : Type.getArgumentTypes(desc)) {
			pos += t.getSize();
		}
		variable = pos;
	}

	/**
	 * This method is where the probe data is 
	 * mutated to represent that some code was covered
	 * either a branch or instruction.
	 */
	public abstract void insertProbe(final int probeIndex);
}