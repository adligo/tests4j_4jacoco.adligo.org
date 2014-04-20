package org.adligo.tests4j_4jacoco.plugin.instrumentation;

import org.adligo.tests4j_4jacoco.plugin.asm.AsmMapHelper;
import org.jacoco.core.internal.flow.ClassProbesVisitor;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class JacocoClassInstrumenter extends ClassProbesVisitor {


		private static final Object[] NO_LOCALS = new Object[0];

		private final long id;

		private final IExecutionDataAccessorGenerator accessorGenerator;

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
		public JacocoClassInstrumenter(final long id,
				final IExecutionDataAccessorGenerator accessorGenerator,
				final ClassVisitor cv) {
			super(cv);
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
		public MethodProbesVisitor visitMethod(final int access, final String name,
				final String desc, final String signature, final String[] exceptions) {

			InstrSupport.assertNotInstrumented(name, className);

			final MethodVisitor mv = cv.visitMethod(access, name, desc, signature,
					exceptions);

			if (mv == null) {
				return null;
			}
			final MethodVisitor frameEliminator = new JacocoDuplicateFrameEliminator(mv);
			final JacocoProbeInserter probeVariableInserter = new JacocoProbeInserter(access,
					desc, frameEliminator, probeArrayStrategy);
			JacocoMethodInstrumenter mi = new JacocoMethodInstrumenter(probeVariableInserter,
					probeVariableInserter);
			mi.setClassName(className);
			return mi;
		}

		@Override
		public void visitTotalProbeCount(final int count) {
			probeCount = count;
		}

		@Override
		public void visitEnd() {
			probeArrayStrategy.addMembers(cv);
			super.visitEnd();
		}

		// === probe array strategies ===

		private class ClassTypeStrategy implements I_JacocoProbeArrayStrategy {

			
			/**
			 * note I tried adding a boolean false and true
			 * to this depricated method, but it seems to only work
			 * when using the old logic
			 */
			@SuppressWarnings("deprecation")
			public int storeInstance(final MethodVisitor mv, final int variable) {
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, className,
						//InstrSupport.INITMETHOD_NAME, InstrSupport.INITMETHOD_DESC);
						InstrSupport.INITMETHOD_NAME, MapInstrConstants.INIT_METHOD_DESC);
				mv.visitVarInsn(Opcodes.ASTORE, variable);
				return 1;
			}

			public void addMembers(final ClassVisitor delegate) {
				createDataField();
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

				// Load the value of the static data field:
				AsmMapHelper.moveMapToStack(mv, className);
				mv.visitInsn(Opcodes.DUP);

				// Stack[1]: Map
				// Stack[0]: Map

				// Skip initialization when we already have a data array:
				final Label alreadyInitialized = new Label();
				mv.visitJumpInsn(Opcodes.IFNONNULL, alreadyInitialized);

				// Stack[0]: Map

				mv.visitInsn(Opcodes.POP);
				final int size = genInitializeDataField(mv, probeCount);

				// Stack[0]: Map

				// Return the class' probe Map:
				if (withFrames) {
					mv.visitFrame(Opcodes.F_NEW, 0, NO_LOCALS, 1, MapInstrConstants.DATAFIELD_INSTANCE);
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
				final int size = accessorGenerator.generateDataAccessor(id,
						className, probeCount, mv);

				// Stack[0]: Map

				for (int i = 0; i < probeCount; i++) {
					AsmMapHelper.callMapPut(i, false, mv);
				}
				
				// Stack[0]: Map
				AsmMapHelper.moveMapToField(mv, className);

				// Stack[0]: Map

				return Math.max(size, 4); //callMapPut has a max stack size of 4
			}

			
		}

		private class InterfaceTypeStrategy implements I_JacocoProbeArrayStrategy {

			public int storeInstance(final MethodVisitor mv, final int variable) {
				final int maxStack = accessorGenerator.generateDataAccessor(id,
						className, probeCount, mv);
				mv.visitVarInsn(Opcodes.ASTORE, variable);
				return maxStack;
			}

			public void addMembers(final ClassVisitor delegate) {
				// nothing to do
			}

		}

	}