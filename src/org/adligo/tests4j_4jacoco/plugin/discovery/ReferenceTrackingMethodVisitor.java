package org.adligo.tests4j_4jacoco.plugin.discovery;

import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypePath;

public class ReferenceTrackingMethodVisitor extends MethodVisitor {

	private ClassReferencesMutant classReferences;
	private I_Tests4J_Log log;
	
	public ReferenceTrackingMethodVisitor(int version, I_Tests4J_Log pLog) {
		super(version);
		log = pLog;
	}

	public ClassReferencesMutant getClassReferences() {
		return classReferences;
	}

	public void setClassReferences(ClassReferencesMutant classReferences) {
		this.classReferences = classReferences;
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name,
			String desc) {
		
		String className = "L" + owner + ";";
		
		if (log.isLogEnabled(ReferenceTrackingMethodVisitor.class)) {
			log.log(super.toString() + " visitFieldInsn " + desc + "   " + className);
		}
		classReferences.onLocalVariableClassName(desc);
		classReferences.onLocalVariableClassName(className);
	}


	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc, boolean itf) {
		visitMethodInsn(opcode, owner, name, desc);
	}
	
	@Override
	public void visitLocalVariable(String name, String desc, String signature,
			Label start, Label end, int index) {
		if (log.isLogEnabled(ReferenceTrackingMethodVisitor.class)) {
			log.log(super.toString() + " visitLocalVariable " + name + " " + desc);
		}
		classReferences.onLocalVariableClassName(desc);
	}


	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc) {
		String className = "L" + owner + ";";
		if (log.isLogEnabled(ReferenceTrackingMethodVisitor.class)) {
			log.log(super.toString() + " visitMethodInsn " + className);
		}
		classReferences.onLocalVariableClassName(className);
	}




	@Override
	public void visitTypeInsn(int opcode, String type) {
		String className = "L" + type + ";";
		if (log.isLogEnabled(ReferenceTrackingMethodVisitor.class)) {
			log.log(super.toString() + " visitTypeInsn " + className);
		}
		classReferences.onLocalVariableClassName(className);
		super.visitTypeInsn(opcode, type);
	}



	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler,
			String type) {

		String className = "L" + type + ";";
		if (log.isLogEnabled(ReferenceTrackingMethodVisitor.class)) {
			log.log(super.toString() + " visitTryCatchBlock " + className);
		}
		classReferences.onLocalVariableClassName(className);
		super.visitTryCatchBlock(start, end, handler, type);
	}

	@Override
	public void visitLdcInsn(Object cst) {
		if (log.isLogEnabled(ReferenceTrackingMethodVisitor.class)) {
			log.log(super.toString() + " visitLdcInsn " + cst);
		}
		classReferences.onLocalVariableClassName(cst.toString());
	}


	





}
