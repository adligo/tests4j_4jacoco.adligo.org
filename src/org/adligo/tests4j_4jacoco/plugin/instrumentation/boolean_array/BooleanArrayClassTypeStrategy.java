package org.adligo.tests4j_4jacoco.plugin.instrumentation.boolean_array;

import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.AbstractObtainProbesStrategy;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ClassInstrumentationInfo;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ObtainProbesStrategy;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_ProbeDataAccessorFactory;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class BooleanArrayClassTypeStrategy extends AbstractObtainProbesStrategy implements I_ObtainProbesStrategy {

	public BooleanArrayClassTypeStrategy(I_ClassInstrumentationInfo pClassInfo,
			I_ProbeDataAccessorFactory pAccessorGenerator) {
		super(pClassInfo,pAccessorGenerator);
	}


	@Override
	public boolean hasJacocoData() {
		// TODO Auto-generated method stub
		return true;
	}


	@Override
	public void createJacocoData(ClassVisitor cv) {
		cv.visitField(InstrSupport.DATAFIELD_ACC,
				InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC,
				null, null);
	}
	
	@Override
	public boolean hasJacocoInit() {
		// TODO Auto-generated method stub
		return true;
	}


	@Override
	public void createJacocoInit(ClassVisitor cv) {
		String className = classInfo.getClassName();
		int probeCount = classInfo.getProbeCount();
		
		final MethodVisitor mv = cv.visitMethod(
				InstrSupport.INITMETHOD_ACC, InstrSupport.INITMETHOD_NAME,
				InstrSupport.INITMETHOD_DESC, null, null);
		mv.visitCode();

		// Load the value of the static data field:
		mv.visitFieldInsn(Opcodes.GETSTATIC, className,
				InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC);
		mv.visitInsn(Opcodes.DUP);

		// Stack[1]: [Z
		// Stack[0]: [Z

		// Skip initialization when we already have a data array:
		final Label alreadyInitialized = new Label();
		mv.visitJumpInsn(Opcodes.IFNONNULL, alreadyInitialized);

		// Stack[0]: [Z

		mv.visitInsn(Opcodes.POP);
		final int size = genInitializeDataField(mv, probeCount);

		// Stack[0]: [Z

		// Return the class' probe array:
		if (classInfo.isWithFrames()) {
			mv.visitFrame(Opcodes.F_NEW, 0, new Object [] {}, 1, new Object[] { InstrSupport.DATAFIELD_DESC });
		}
		mv.visitLabel(alreadyInitialized);
		mv.visitInsn(Opcodes.ARETURN);

		mv.visitMaxs(Math.max(size, 2), 0); // Maximum local stack size is 2
		mv.visitEnd();
	}

	/**
	 * Generates the byte code to initialize the static coverage data field
	 * within this class.
	 * 
	 * The code will push the [Z data array on the operand stack.
	 * 
	 * @param mv
	 *            generator to emit code to
	 */
	private int genInitializeDataField(final MethodVisitor mv,
			final int probeCount) {
		long id = classInfo.getId();
		String className = classInfo.getClassName();
		
		final int size = accessorGenerator.create(id,
				className, probeCount, mv);

		// Stack[0]: [Z

		mv.visitInsn(Opcodes.DUP);

		// Stack[1]: [Z
		// Stack[0]: [Z

		mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
				InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC);

		// Stack[0]: [Z

		return Math.max(size, 2); // Maximum local stack size is 2
	}


	@Override
	public int createProbeDataAccessorCall(MethodVisitor mv, int variable) {
		String className = classInfo.getClassName();
		
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, className,
				InstrSupport.INITMETHOD_NAME, InstrSupport.INITMETHOD_DESC, false);
		mv.visitVarInsn(Opcodes.ASTORE, variable);
		return 1;
	}


	


	

	
}
