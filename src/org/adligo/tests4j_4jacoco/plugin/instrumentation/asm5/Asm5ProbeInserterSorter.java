package org.adligo.tests4j_4jacoco.plugin.instrumentation.asm5;

import org.adligo.tests4j.run.Tests4J_UncaughtExceptionHandler;
import org.adligo.tests4j_4jacoco.plugin.asm.ApiVersion;
import org.adligo.tests4j_4jacoco.plugin.asm.BytecodeInjectionDebuger;
import org.adligo.tests4j_4jacoco.plugin.asm.MapBytecodeHelper;
import org.adligo.tests4j_4jacoco.plugin.asm.StackHelper;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.I_JacocoProbeArrayStrategy;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.I_JacocoProbeInserter;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.MapInstrConstants;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

public class Asm5ProbeInserterSorter extends LocalVariablesSorter implements I_JacocoProbeInserter {

	private final I_JacocoProbeArrayStrategy arrayStrategy;

	/** Position of the inserted variable. 
	 *  So this is the Map's local variable location for $jacocoData
	 */
	private int variable;

	/** Maximum stack usage of the code to access the probe array. */
	private int accessorStackSize;
	
	private int insertProbeStackSize;
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
	public Asm5ProbeInserterSorter(final int access, final String desc, final MethodVisitor mv,
			final I_JacocoProbeArrayStrategy arrayStrategy) {
		super(ApiVersion.VERSION, access, desc,  mv);
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
		accessorStackSize = arrayStrategy.createPutDataInLocal(mv, variable);
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