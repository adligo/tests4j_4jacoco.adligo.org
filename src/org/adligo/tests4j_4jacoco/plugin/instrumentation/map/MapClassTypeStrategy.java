package org.adligo.tests4j_4jacoco.plugin.instrumentation.map;

import org.adligo.tests4j_4jacoco.plugin.asm.BytecodeInjectionDebuger;
import org.adligo.tests4j_4jacoco.plugin.asm.I_StackHelper;
import org.adligo.tests4j_4jacoco.plugin.asm.StackHelper;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.AbstractObtainProbesStrategy;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ClassInstrumentationInfo;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ObtainProbesStrategy;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_ProbeDataAccessorFactory;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MapClassTypeStrategy extends AbstractObtainProbesStrategy implements I_ObtainProbesStrategy {

	public MapClassTypeStrategy(I_ClassInstrumentationInfo pClassInfo,
			I_ProbeDataAccessorFactory pAccessorGenerator) {
		super(pClassInfo,pAccessorGenerator);
	}

	@Override
	public int createProbeDataAccessorCall(final MethodVisitor mv, final int variable) {
		String className = classInfo.getClassName();
		
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, className,
				//InstrSupport.INITMETHOD_NAME, InstrSupport.INITMETHOD_DESC);
				InstrSupport.INITMETHOD_NAME, MapInstrConstants.INIT_METHOD_DESC,
				false);
		mv.visitVarInsn(Opcodes.ASTORE, variable);
		return 1;
	}

	@Override
	public boolean hasJacocoData() {
		// TODO Auto-generated method stub
		return true;
	}

	public void createJacocoData(final ClassVisitor cv) {
		cv.visitField(MapInstrConstants.DATAFIELD_ACC,
				//InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC,
				InstrSupport.DATAFIELD_NAME, MapInstrConstants.DATAFIELD_CLAZZ,
				null, null);
	}	
	
	@Override
	public boolean hasJacocoInit() {
		// TODO Auto-generated method stub
		return true;
	}
	
	public void createJacocoInit(final ClassVisitor cv) {
		String className = classInfo.getClassName();
		int probeCount = classInfo.getProbeCount();
		
		final MethodVisitor mv = cv.visitMethod(
				MapInstrConstants.INITMETHOD_ACC, InstrSupport.INITMETHOD_NAME,
				//InstrSupport.INITMETHOD_DESC, null, null);
				MapInstrConstants.INIT_METHOD_DESC, null, null);
		
		
		mv.visitCode();

		I_StackHelper sh = new StackHelper();
		
		// Load the value of the static data field:
		MapBytecodeHelper.moveMapToStack(sh, mv, className);
		
		mv.visitInsn(Opcodes.DUP);
		if (BytecodeInjectionDebuger.isEnabled()) {
			BytecodeInjectionDebuger.dupStackDebug();
		}
		sh.incrementStackSize();
		// Stack[1]: Map
		// Stack[0]: Map
		
		
		if (BytecodeInjectionDebuger.isEnabled()) {
			System.out.println("in createInitMethod for " + className + " scott probe count is " + probeCount);
			BytecodeInjectionDebuger.log(sh, mv, 
					"in "+ className + ".jacocoInit() scott probe count is " + probeCount);
			
		}
		// Stack[1]: Map
		// Stack[0]: Map

		// Skip initialization when we already have a data array:
		final Label alreadyInitialized = new Label();
		mv.visitJumpInsn(Opcodes.IFNONNULL, alreadyInitialized);
		if (BytecodeInjectionDebuger.isEnabled()) {
			BytecodeInjectionDebuger.popOffStackDebug();
		}
		sh.decrementStackSize();
		// Stack[0]: Map
		
		mv.visitInsn(Opcodes.POP);
		if (BytecodeInjectionDebuger.isEnabled()) {
			BytecodeInjectionDebuger.popOffStackDebug();
		}
		sh.decrementStackSize();
		
		genInitializeDataField(sh, mv, probeCount);

		if (BytecodeInjectionDebuger.isEnabled()) {
			BytecodeInjectionDebuger.log(sh, mv, 
					"finished genInitializeDataField in " + className + ".jacocoInit() \n" +
			sh);
		}
		// Stack[0]: Map

		// Return the class' probe Map:
		if (classInfo.isWithFrames()) {
			mv.visitFrame(Opcodes.F_NEW, 0, new Object[] {}, 1, MapInstrConstants.DATAFIELD_INSTANCE);
		}
		
		
		mv.visitLabel(alreadyInitialized);
		mv.visitInsn(Opcodes.ARETURN);

		if (BytecodeInjectionDebuger.isEnabled()) {
			BytecodeInjectionDebuger.popOffStackDebug();
		}
		mv.visitMaxs(sh.getMaxStackSize(), 1); 
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
	private void genInitializeDataField(final I_StackHelper sh,  final MethodVisitor mv,
			final int probeCount) {
		long id = classInfo.getId();
		String className = classInfo.getClassName();
		
		
		if (BytecodeInjectionDebuger.isEnabled()) {
			BytecodeInjectionDebuger.log(sh, mv,
					"before accessorGenerator.create " + className + ".$jacocoInit()  " + 
					"\n" + sh);
		}
		final int size = accessorGenerator.create(id,
				className, probeCount, mv, sh);
		if (BytecodeInjectionDebuger.isEnabled()) {
			BytecodeInjectionDebuger.putInStackDebug(MapInstrConstants.DATAFIELD_CLAZZ);	
		}
		sh.incrementStackSize(size);
		sh.decrementStackSize(size -1);
		if (BytecodeInjectionDebuger.isEnabled()) {
			BytecodeInjectionDebuger.log(sh, mv,
					"after accessorGenerator.create " + className + ".jacocoInit()  "
					+ "\n" + sh);
		}
		
		if (BytecodeInjectionDebuger.isEnabled()) {
			BytecodeInjectionDebuger.log(sh, mv,
					"putting map back to static field " + className + ".jacocoInit() " + sh);
		}
		
		// Stack[0]: Map
		MapBytecodeHelper.moveMapToField(sh, mv, className);
		
		// Stack[0]: Map
	}




	

	
}
