package org.adligo.tests4j_4jacoco.plugin.instrumentation.common;

import org.adligo.tests4j_4jacoco.plugin.asm.ApiVersion;
import org.adligo.tests4j_4jacoco.plugin.asm.BytecodeInjectionDebuger;
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

		private final long id_;


		private I_ObtainProbesStrategy probeArrayStrategy_;
		private I_ProbeInserterFactory instrumenterFactory_;
		private String className_;
		private boolean withFrames_;

		private int probeCount_;

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
			this.id_ = id;
			instrumenterFactory_ = pInstrumenterFactory;
		}

		@Override
		public void visit(final int version, final int access, final String name,
				final String signature, final String superName,
				final String[] interfaces) {
			withFrames_ = (version & 0xff) >= Opcodes.V1_6;
			className_ = name;
			if ((access & Opcodes.ACC_INTERFACE) == 0) {
				this.probeArrayStrategy_ = instrumenterFactory_.createObtainProbesStrategy(
						ObtainProbesStrategyType.CLASS, this);
			} else {
				this.probeArrayStrategy_ = instrumenterFactory_.createObtainProbesStrategy(
						ObtainProbesStrategyType.INTERFACE, this);
			}
			super.visit(version, access, name, signature, superName, interfaces);
		}

		@Override
		public FieldVisitor visitField(final int access, final String name,
				final String desc, final String signature, final Object value) {
			InstrSupport.assertNotInstrumented(name, className_);
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

			InstrSupport.assertNotInstrumented(name, className_);

			final MethodVisitor mv = cv.visitMethod(access, name, desc, signature,
					exceptions);
			if (BytecodeInjectionDebuger.isEnabled()) {
				System.out.println("" + this.getClass().getName() +
					"\n ... visiting " + className_ + "." + name);
			}
			if (mv == null) {
				return null;
			}
			AnalyzerAdapter aa = new AnalyzerAdapter(
					this.getClass().getName()+
					"_" + className_, access, name, desc, mv);
			AbstractProbeInserter lvs = instrumenterFactory_.createProbeInserter(
					access, desc, aa, probeArrayStrategy_);
			MethodInstrumenter toRet = new MethodInstrumenter(lvs);
			
			return toRet;
		}

		@Override
		public void visitTotalProbeCount(final int count) {
			probeCount_ = count;
		}

		@Override
		public void visitEnd() {
			if (probeArrayStrategy_.hasJacocoData()) {
				probeArrayStrategy_.createJacocoData(cv);
			}
			if (probeArrayStrategy_.hasJacocoInit()) {
				probeArrayStrategy_.createJacocoInit(cv);
			}
			super.visitEnd();
		}


		@Override
		public ClassVisitor getThis() {
			return this;
		}

		@Override
		public long getId() {
			return id_;
		}

		@Override
		public String getClassName() {
			return className_;
		}

		@Override
		public int getProbeCount() {
			return probeCount_;
		}

		@Override
		public boolean isWithFrames() {
			return withFrames_;
		}

	}