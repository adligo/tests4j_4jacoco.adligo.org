package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.adligo.tests4j.models.shared.common.ClassMethods;
import org.adligo.tests4j.models.shared.common.StringMethods;
import org.adligo.tests4j.models.shared.dependency.ClassAttributesMutant;
import org.adligo.tests4j.models.shared.dependency.MethodSignature;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.map.MapInstrConstants;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import com.sun.org.apache.bcel.internal.generic.Type;

/**
 * This class reads java byte code scanning for 
 * java types.
 * 
 * @diagram_sync with DiscoveryOverview.seq on 8/17/2014
 * @author scott
 *
 */
public class ReferenceTrackingMethodVisitor extends MethodVisitor {

	private Map<String, ClassAttributesMutant> classReferences;
	//private I_Tests4J_Log log;
	private String currentMethodName = "";
	private I_Tests4J_Log log;
	
	public ReferenceTrackingMethodVisitor(int version, I_Tests4J_Log logIn) {
		super(version);
		log = logIn;
		//log = pLog;
	}

	public Map<String, ClassAttributesMutant> getClassReferences() {
		return classReferences;
	}

	public void setClassReferences(Map<String, ClassAttributesMutant> classReferences) {
		this.classReferences = classReferences;
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name,
			String desc)  {
		
		if (currentMethodName.equals(MapInstrConstants.METHOD_NAME)) {
			return;
		}
		addClassMethod(desc, null, null);
		addClassMethod("L" + owner + ";", null, null);
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
		addClassMethod(desc, null, null);
	}


	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc) {
		
		if (currentMethodName.equals(MapInstrConstants.METHOD_NAME)) {
			return;
		}
		addClassMethod("L" + owner + ";", name, desc);
	}




	@Override
	public void visitTypeInsn(int opcode, String type) {
		if (currentMethodName.equals(MapInstrConstants.METHOD_NAME)) {
			return;
		}
		addClassMethod("L" + type + ";", null, null);
		super.visitTypeInsn(opcode, type);
	}



	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler,
			String type) {

		if (currentMethodName.equals(MapInstrConstants.METHOD_NAME)) {
			return;
		}
		addClassMethod("L" + type + ";", null, null);
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
			addClassMethod(cst.toString(), null, null);
		} 
	}

	public void addClassMethod(String className, String methodName, String desc) {
		if (log.isLogEnabled(ReferenceTrackingClassVisitor.class)) {
			log.log("addClassMethod (" + className + "," + methodName + "," + desc + ")");
		}
		ClassAttributesMutant mut = classReferences.get(className);
		if (mut == null) {
			mut = new ClassAttributesMutant();
			mut.setClassName(className);
			classReferences.put(className, mut);
		}
		if (methodName != null) {
			String [] params = null;
			if ( !StringMethods.isEmpty(desc)) {
				if (log.isLogEnabled(ReferenceTrackingClassVisitor.class)) {
					log.log("parseAsmMethodSig '" + desc + "'");
				}
				params = parseAsmMethodSig(desc);
			}
			MethodSignature ms = new MethodSignature(methodName, params);
			mut.addMethod(ms);
			
		}
	}

	public static String [] parseAsmMethodSig(String sig) {
		if (sig.indexOf("()") != -1) {
			//no parameters
			return null;
		}
		char [] chars = sig.toCharArray();
		List<String> classes = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		boolean inClass = false;
		boolean inArray = false;
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (c == '(') {
				//first char of method sig
			} else if (c == ')') {
				break;
			} else if (inClass) {
				if (c == ';') {
					sb.append(c);
					String result = sb.toString();
					classes.add(result);
					sb = new StringBuilder();
					inClass = false;
					inArray = false;
				} else {
					sb.append(c);
				}
			} else if (inArray) {
				if (ClassMethods.isClass(c)) {
					inClass = true;
					sb.append(c);
				} else if (ClassMethods.isPrimitiveClassChar(c)) {
					sb.append(c);
					String result = sb.toString();
					classes.add(result);
					sb = new StringBuilder();
					inArray = false;
				} else {
					sb.append(c);
				}
			} else if (ClassMethods.isPrimitiveClassChar(c)) {
				sb.append(c);
				String result = sb.toString();
				classes.add(result);
				sb = new StringBuilder();
			} else if (ClassMethods.isArray(c)) {
				inArray = true;
				sb.append(c);
			
			} else if (ClassMethods.isClass(c)) {
				inClass = true;
				sb.append(c);
			
			} else {
				sb.append(c);
			}
		}
		String result = sb.toString();
		if ( !StringMethods.isEmpty(result)) {
			classes.add(result);
		}
		if (classes.size() == 0) {
			return null;
		}
		return classes.toArray(new String[classes.size()]);
	}
	
	public String getCurrentMethodName() {
		return currentMethodName;
	}

	public void setCurrentMethodName(String currentMethodName) {
		this.currentMethodName = currentMethodName;
	}

}
