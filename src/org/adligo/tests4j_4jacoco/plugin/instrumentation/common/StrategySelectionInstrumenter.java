package org.adligo.tests4j_4jacoco.plugin.instrumentation.common;

import org.adligo.tests4j.run.Tests4J_UncaughtExceptionHandler;
import org.adligo.tests4j_4jacoco.plugin.asm.ApiVersion;
import org.adligo.tests4j_4jacoco.plugin.asm.BytecodeInjectionDebuger;
import org.adligo.tests4j_4jacoco.plugin.common.I_ClassInstrumenterFactory;
import org.adligo.tests4j_4jacoco.plugin.common.I_ObtainProbesStrategy;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.AbstractProbeInserter;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.I_ProbeInserterFactory;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AnalyzerAdapter;

public class StrategySelectionInstrumenter extends ClassVisitor
	implements I_ClassProbesVisitor, I_ClassInstrumentationInfo {

		private final long id;


		private I_ObtainProbesStrategy probeArrayStrategy;
		private I_ProbeInserterFactory instrumenterFactory;
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
		public StrategySelectionInstrumenter(final long id,
				final I_ProbeInserterFactory pInstrumenterFactory,
				final ClassVisitor cv) {
			super(ApiVersion.VERSION, cv);
			this.id = id;
			instrumenterFactory = pInstrumenterFactory;
			
		}

		@Override
		public void visit(final int version, final int access, final String name,
				final String signature, final String superName,
				final String[] interfaces) {
			this.className = name;
			withFrames = (version & 0xff) >= Opcodes.V1_6;
			if ((access & Opcodes.ACC_INTERFACE) == 0) {
				this.probeArrayStrategy = instrumenterFactory.createObtainProbesStrategy(
						ObtainProbesStrategyType.CLASS, this);
			} else {
				this.probeArrayStrategy = instrumenterFactory.createObtainProbesStrategy(
						ObtainProbesStrategyType.INTERFACE, this);
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
			if (BytecodeInjectionDebuger.isEnabled()) {
				System.out.println("" + this.getClass().getName() +
					"\n ... visiting " + className + "." + name);
			}
			if (mv == null) {
				return null;
			}
			AnalyzerAdapter aa = new AnalyzerAdapter(
					this.getClass().getName()+
					"_" + className, access, name, desc, mv);
			AbstractProbeInserter lvs = instrumenterFactory.createProbeInserter(
					access, desc, aa, probeArrayStrategy);
			MethodInstrumenter toRet = new MethodInstrumenter(lvs);
			
			return toRet;
		}

		@Override
		public void visitTotalProbeCount(final int count) {
			probeCount = count;
		}

		@Override
		public void visitEnd() {
			if (probeArrayStrategy.hasJacocoData()) {
				probeArrayStrategy.createJacocoData(cv);
			}
			if (probeArrayStrategy.hasJacocoInit()) {
				probeArrayStrategy.createJacocoInit(cv);
			}
			super.visitEnd();
		}


		@Override
		public ClassVisitor getThis() {
			return this;
		}

		@Override
		public long getId() {
			return id;
		}

		@Override
		public String getClassName() {
			return className;
		}

		@Override
		public int getProbeCount() {
			return probeCount;
		}

		@Override
		public boolean isWithFrames() {
			return withFrames;
		}

	}