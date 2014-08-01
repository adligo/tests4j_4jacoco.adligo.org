package org.adligo.tests4j_4jacoco.plugin.discovery;

import org.adligo.tests4j.models.shared.common.ClassMethods;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;
import org.adligo.tests4j.run.discovery.ClassReferencesMutant;
import org.adligo.tests4j.run.discovery.I_ClassFilter;
import org.adligo.tests4j.run.discovery.I_ClassReferences;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.Type;

public class ReferenceTrackingMethodVisitor extends MethodVisitor {

	private ClassReferencesMutant classReferences;
	private I_ClassFilter classFilter;
	private I_Tests4J_Log log;
	
	public ReferenceTrackingMethodVisitor(int version, I_Tests4J_Log pLog) {
		super(version);
		log = pLog;
	}

	public I_ClassReferences getClassReferences() {
		return classReferences;
	}

	public void setClassReferences(ClassReferencesMutant classReferences) {
		this.classReferences = classReferences;
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name,
			String desc)  {
		
		String className = "L" + owner + ";";
		className = ClassMethods.fromTypeDescription(className);
		desc = ClassMethods.fromTypeDescription(desc);
		if (log.isLogEnabled(ReferenceTrackingMethodVisitor.class)) {
			log.log(super.toString() + " visitFieldInsn " + desc + "   " + className);
		}
		if ( !classFilter.isFiltered(desc)) {
			classReferences.addReference(desc);
		}
		if ( !classFilter.isFiltered(className)) {
			classReferences.addReference(className);
		}
	}


	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc, boolean itf) {
		visitMethodInsn(opcode, owner, name, desc);
	}
	
	@Override
	public void visitLocalVariable(String name, String desc, String signature,
			Label start, Label end, int index) {
		
		desc = ClassMethods.fromTypeDescription(desc);
		if (log.isLogEnabled(ReferenceTrackingMethodVisitor.class)) {
			log.log(super.toString() + " visitLocalVariable " + name + " " + desc);
		}
		if ( !classFilter.isFiltered(desc)) {
			classReferences.addReference(desc);
		}
	}


	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc) {
		String className = "L" + owner + ";";
		className = ClassMethods.fromTypeDescription(className);
		
		if (log.isLogEnabled(ReferenceTrackingMethodVisitor.class)) {
			log.log(super.toString() + " visitMethodInsn " + className);
		}
		if ( !classFilter.isFiltered(className)) {
			classReferences.addReference(className);
		}
	}




	@Override
	public void visitTypeInsn(int opcode, String type) {
		String className = "L" + type + ";";
		className = ClassMethods.fromTypeDescription(className);
		if (log.isLogEnabled(ReferenceTrackingMethodVisitor.class)) {
			log.log(super.toString() + " visitTypeInsn " + className);
		}
		if ( !classFilter.isFiltered(className)) {
			classReferences.addReference(className);
		}
		super.visitTypeInsn(opcode, type);
	}



	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler,
			String type) {

		String className = "L" + type + ";";
		className = ClassMethods.fromTypeDescription(className);
		if (log.isLogEnabled(ReferenceTrackingMethodVisitor.class)) {
			log.log(super.toString() + " visitTryCatchBlock " + className);
		}
		if ( !classFilter.isFiltered(className)) {
			classReferences.addReference(className);
		}
		super.visitTryCatchBlock(start, end, handler, type);
	}

	@Override
	public void visitLdcInsn(Object cst) {
		String className = ClassMethods.fromTypeDescription(cst.toString());
		if (log.isLogEnabled(ReferenceTrackingMethodVisitor.class)) {
			log.log(super.toString() + " visitLdcInsn " + className + " " + cst.getClass().getName());
		}
		 if (cst instanceof Integer) {
		     return;
		 } else if (cst instanceof Float) {
			 return;
		 } else if (cst instanceof Long) {
			 return;
		 } else if (cst instanceof Double) {
			 return;
		 } else if (cst instanceof String) {
			 return;
		 } else {
			 //if (cst instanceof Type) or other
			 //else if (cst instanceof Handle)
			if ( !classFilter.isFiltered(className)) {
				classReferences.addReference(className);
			}
		 }
		 
	}

	public I_ClassFilter getClassFilter() {
		return classFilter;
	}

	public void setClassFilter(I_ClassFilter classFilter) {
		this.classFilter = classFilter;
	}


	





}
