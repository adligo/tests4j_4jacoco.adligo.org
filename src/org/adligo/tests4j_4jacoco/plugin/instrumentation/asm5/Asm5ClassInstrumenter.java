package org.adligo.tests4j_4jacoco.plugin.instrumentation.asm5;

import org.adligo.tests4j.run.Tests4J_UncaughtExceptionHandler;
import org.adligo.tests4j_4jacoco.plugin.asm.ApiVersion;
import org.adligo.tests4j_4jacoco.plugin.asm.MapBytecodeHelper;
import org.adligo.tests4j_4jacoco.plugin.asm.BytecodeInjectionDebuger;
import org.adligo.tests4j_4jacoco.plugin.asm.StackHelper;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.I_ClassProbesVisitor;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.I_JacocoProbeArrayStrategy;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.I_MethodProbesVisitor;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.MapInstrConstants;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_ProbeDataAccessorFactory;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AnalyzerAdapter;

public class Asm5ClassInstrumenter extends ClassVisitor
	implements I_ClassProbesVisitor {


		private static final Object[] NO_LOCALS = new Object[0];

		private final long id;

		private final I_ProbeDataAccessorFactory accessorGenerator;

		private I_JacocoProbeArrayStrategy probeArrayStrategy;

		private String className;

		private boolean withFrames;

		private int probeCount;

		/**
		 * Emits a instrumented version of this class to the given class visitor.
		 * 
		 * @param id
		 *            unique identifier given to this class
		 * @param accessorGenerator
		 *            this generator will be used for instrumentation
		 * @param cv
		 *            next delegate in the visitor chain will receive the
		 *            instrumented class
		 */
		public Asm5ClassInstrumenter(final long id,
				final I_ProbeDataAccessorFactory accessorGenerator,
				final ClassVisitor cv) {
			super(ApiVersion.VERSION, cv);
			this.id = id;
			this.accessorGenerator = accessorGenerator;
		}

		@Override
		public void visit(final int version, final int access, final String name,
				final String signature, final String superName,
				final String[] interfaces) {
			this.className = name;
			withFrames = (version & 0xff) >= Opcodes.V1_6;
			if ((access & Opcodes.ACC_INTERFACE) == 0) {
				this.probeArrayStrategy = new ClassTypeStrategy();
			} else {
				this.probeArrayStrategy = new InterfaceTypeStrategy();
			}
			super.visit(version, access, name, signature, superName, interfaces);
		}

		@Override
		public FieldVisitor visitField(final int access, final String name,
				final String desc, final String signature, final Object value) {
			InstrSupport.assertNotInstrumented(name, className);
			return super.visitField(access, name, desc, signature, value);
		}

		@Override
		public MethodVisitor visitMethod(final int access, final String name,
				final String desc, final String signature, final String[] exceptions) {
			return 
					visitMethodForProbes(access, name, desc, signature, exceptions)
					.getThis();
		}
		
		@Override
		public I_MethodProbesVisitor visitMethodForProbes(final int access, final String name,
				final String desc, final String signature, final String[] exceptions) {

			InstrSupport.assertNotInstrumented(name, className);

			final MethodVisitor mv = cv.visitMethod(access, name, desc, signature,
					exceptions);
			Tests4J_UncaughtExceptionHandler.OUT.println("" + this.getClass().getName() +
					"\n ... visiting " + className + "." + name);
			if (mv == null) {
				return null;
			}
			AnalyzerAdapter aa = new AnalyzerAdapter(
					this.getClass().getName()+
					"_" + className, access, name, desc, mv);
			Asm5ProbeInserterSorter lvs = new Asm5ProbeInserterSorter(
					access, desc, aa, probeArrayStrategy);
			Asm5MethodInstrumenter toRet = new Asm5MethodInstrumenter(lvs);
			
			return toRet;
		}

		@Override
		public void visitTotalProbeCount(final int count) {
			probeCount = count;
		}

		@Override
		public void visitEnd() {
			probeArrayStrategy.createJacocoData(cv);
			probeArrayStrategy.createJacocoInit(cv);
			super.visitEnd();
		}

		// === probe array strategies ===

		private class ClassTypeStrategy implements I_JacocoProbeArrayStrategy {

			
			public int createPutDataInLocal(final MethodVisitor mv, final int variable) {
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, className,
						//InstrSupport.INITMETHOD_NAME, InstrSupport.INITMETHOD_DESC);
						InstrSupport.INITMETHOD_NAME, MapInstrConstants.INIT_METHOD_DESC,
						false);
				mv.visitVarInsn(Opcodes.ASTORE, variable);
				return 1;
			}

			public void createJacocoData(final ClassVisitor delegate) {
				createDataField();
			}	
			
			public void createJacocoInit(final ClassVisitor delegate) {
				createInitMethod(probeCount);
			}

			private void createDataField() {
				cv.visitField(InstrSupport.DATAFIELD_ACC,
						//InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC,
						InstrSupport.DATAFIELD_NAME, MapInstrConstants.DATAFIELD_CLAZZ,
						null, null);
			}

			private void createInitMethod(final int probeCount) {
				
				final MethodVisitor mv = cv.visitMethod(
						InstrSupport.INITMETHOD_ACC, InstrSupport.INITMETHOD_NAME,
						//InstrSupport.INITMETHOD_DESC, null, null);
						MapInstrConstants.INIT_METHOD_DESC, null, null);
				
				
				mv.visitCode();

				StackHelper sh = new StackHelper();
				
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
				if (withFrames) {
					mv.visitFrame(Opcodes.F_NEW, 0, NO_LOCALS, 1, MapInstrConstants.DATAFIELD_INSTANCE);
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
			private void genInitializeDataField(final StackHelper sh,  final MethodVisitor mv,
					final int probeCount) {
				
				if (BytecodeInjectionDebuger.isEnabled()) {
					BytecodeInjectionDebuger.log(sh, mv,
							"before accessorGenerator.create " + className + ".jacocoInit()  " + 
							"\n" + sh);
				}
				final int size = accessorGenerator.create(id,
						className, probeCount, mv);
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
				
				// Stack[0]: Map

				for (int i = 0; i < probeCount; i++) {
					mv.visitInsn(Opcodes.DUP);
					sh.incrementStackSize();
					if (BytecodeInjectionDebuger.isEnabled()) {
						BytecodeInjectionDebuger.dupStackDebug();
					}
					
					// Stack[1]: Map
					// Stack[0]: Map
					
					MapBytecodeHelper.callMapPut(sh, i, false, mv);
					
					// Stack[0]: Map
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

		private class InterfaceTypeStrategy implements I_JacocoProbeArrayStrategy {

			public int createPutDataInLocal(final MethodVisitor mv, final int variable) {
				final int maxStack = accessorGenerator.create(id,
						className, probeCount, mv);
				mv.visitVarInsn(Opcodes.ASTORE, variable);
				return maxStack;
			}

			public void createJacocoData(final ClassVisitor delegate) {
				//do nothing
			}	
			
			public void createJacocoInit(final ClassVisitor delegate) {
				//do nothing
			}
		}

		@Override
		public ClassVisitor getThis() {
			return this;
		}

	}