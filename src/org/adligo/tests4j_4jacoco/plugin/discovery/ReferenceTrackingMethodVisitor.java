package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.util.Set;

import org.adligo.tests4j.models.shared.common.ClassMethods;
import org.adligo.tests4j.models.shared.dependency.I_ClassFilter;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.map.MapInstrConstants;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class ReferenceTrackingMethodVisitor extends MethodVisitor {

	private Set<String> classReferences;
	private I_ClassFilter classFilter;
	private I_Tests4J_Log log;
	private String currentMethodName = "";
	
	public ReferenceTrackingMethodVisitor(int version, I_Tests4J_Log pLog) {
		super(version);
		log = pLog;
	}

	public Set<String> getClassReferences() {
		return classReferences;
	}

	public void setClassReferences(Set<String> classReferences) {
		this.classReferences = classReferences;
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name,
			String desc)  {
		
		if (currentMethodName.equals(MapInstrConstants.METHOD_NAME)) {
			return;
		}
		String className = "L" + owner + ";";
		className = ClassMethods.fromTypeDescription(className);
		desc = ClassMethods.fromTypeDescription(desc);
		if (log.isLogEnabled(ReferenceTrackingMethodVisitor.class)) {
			log.log(super.toString() + " visitFieldInsn " + desc + "   " + className);
		}
		if ( !classFilter.isFiltered(desc)) {
			classReferences.add(desc);
		}
		if ( !classFilter.isFiltered(className)) {
			classReferences.add(className);
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
		
		if (currentMethodName.equals(MapInstrConstants.METHOD_NAME)) {
			return;
		}
		
		desc = ClassMethods.fromTypeDescription(desc);
		if (log.isLogEnabled(ReferenceTrackingMethodVisitor.class)) {
			log.log(super.toString() + " visitLocalVariable " + name + " " + desc);
		}
		if ( !classFilter.isFiltered(desc)) {
			classReferences.add(desc);
		}
	}


	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc) {
		
		if (currentMethodName.equals(MapInstrConstants.METHOD_NAME)) {
			return;
		}
		String className = "L" + owner + ";";
		className = ClassMethods.fromTypeDescription(className);
		
		if (log.isLogEnabled(ReferenceTrackingMethodVisitor.class)) {
			log.log(super.toString() + " visitMethodInsn " + className);
		}
		if ( !classFilter.isFiltered(className)) {
			classReferences.add(className);
		}
	}




	@Override
	public void visitTypeInsn(int opcode, String type) {
		if (currentMethodName.equals(MapInstrConstants.METHOD_NAME)) {
			return;
		}
		String className = "L" + type + ";";
		className = ClassMethods.fromTypeDescription(className);
		if (log.isLogEnabled(ReferenceTrackingMethodVisitor.class)) {
			log.log(super.toString() + " visitTypeInsn " + className);
		}
		if ( !classFilter.isFiltered(className)) {
			classReferences.add(className);
		}
		super.visitTypeInsn(opcode, type);
	}



	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler,
			String type) {

		if (currentMethodName.equals(MapInstrConstants.METHOD_NAME)) {
			return;
		}
		String className = "L" + type + ";";
		className = ClassMethods.fromTypeDescription(className);
		if (log.isLogEnabled(ReferenceTrackingMethodVisitor.class)) {
			log.log(super.toString() + " visitTryCatchBlock " + className);
		}
		if ( !classFilter.isFiltered(className)) {
			classReferences.add(className);
		}
		super.visitTryCatchBlock(start, end, handler, type);
	}

	@Override
	public void visitLdcInsn(Object cst) {
		if (currentMethodName.equals(MapInstrConstants.METHOD_NAME)) {
			return;
		}
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
				classReferences.add(className);
			}
		 }
		 
	}

	public I_ClassFilter getClassFilter() {
		return classFilter;
	}

	public void setClassFilter(I_ClassFilter classFilter) {
		this.classFilter = classFilter;
	}

	public String getCurrentMethodName() {
		return currentMethodName;
	}

	public void setCurrentMethodName(String currentMethodName) {
		this.currentMethodName = currentMethodName;
	}

}
