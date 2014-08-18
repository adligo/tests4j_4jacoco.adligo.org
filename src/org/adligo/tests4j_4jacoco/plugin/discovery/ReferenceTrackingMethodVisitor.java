package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.util.Set;

import org.adligo.tests4j.shared.output.I_Tests4J_Log;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.map.MapInstrConstants;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * This class reads java byte code scanning for 
 * java types.
 * 
 * @diagram_sync with DiscoveryOverview.seq on 8/17/2014
 * @author scott
 *
 */
public class ReferenceTrackingMethodVisitor extends MethodVisitor {

	private Set<String> classReferences;
	//private I_Tests4J_Log log;
	private String currentMethodName = "";
	
	public ReferenceTrackingMethodVisitor(int version, I_Tests4J_Log pLog) {
		super(version);
		//log = pLog;
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
		classReferences.add(desc);
		classReferences.add(className);
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
		
		classReferences.add(desc);
	}


	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc) {
		
		if (currentMethodName.equals(MapInstrConstants.METHOD_NAME)) {
			return;
		}
		String className = "L" + owner + ";";
		classReferences.add(className);
	}




	@Override
	public void visitTypeInsn(int opcode, String type) {
		if (currentMethodName.equals(MapInstrConstants.METHOD_NAME)) {
			return;
		}
		String className = "L" + type + ";";
		classReferences.add(className);
		super.visitTypeInsn(opcode, type);
	}



	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler,
			String type) {

		if (currentMethodName.equals(MapInstrConstants.METHOD_NAME)) {
			return;
		}
		String className = "L" + type + ";";
		classReferences.add(className);
		super.visitTryCatchBlock(start, end, handler, type);
	}

	@Override
	public void visitLdcInsn(Object cst) {
		if (currentMethodName.equals(MapInstrConstants.METHOD_NAME)) {
			return;
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
			classReferences.add(cst.toString());
		} 
	}


	public String getCurrentMethodName() {
		return currentMethodName;
	}

	public void setCurrentMethodName(String currentMethodName) {
		this.currentMethodName = currentMethodName;
	}

}
