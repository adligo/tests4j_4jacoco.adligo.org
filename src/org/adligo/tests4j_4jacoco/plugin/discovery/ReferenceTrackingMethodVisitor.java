package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.adligo.tests4j.models.shared.common.ClassMethods;
import org.adligo.tests4j.models.shared.common.StringMethods;
import org.adligo.tests4j.models.shared.dependency.ClassAttributesMutant;
import org.adligo.tests4j.models.shared.dependency.FieldSignature;
import org.adligo.tests4j.models.shared.dependency.MethodSignature;
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
		
		addClassField(addWrapperIfNecessary( owner ), name, desc);
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
		addClassMethod(addWrapperIfNecessary( owner ), name, desc);
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc, boolean itf) {
		visitMethodInsn(opcode, owner, name, desc);
	}


	@Override
	public void visitTypeInsn(int opcode, String type) {
		if (currentMethodName.equals(MapInstrConstants.METHOD_NAME)) {
			return;
		}
		addClassMethod(addWrapperIfNecessary( type ), null, null);
		super.visitTypeInsn(opcode, type);
	}



	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler,
			String type) {

		if (currentMethodName.equals(MapInstrConstants.METHOD_NAME)) {
			return;
		}
		addClassMethod(addWrapperIfNecessary( type ), null, null);
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
			addClassMethod( addWrapperIfNecessary( cst.toString() ), null, null);
		} 
	}

	public void addClassField(String className, String fieldName, String fieldClassType) {
		if (log.isLogEnabled(ReferenceTrackingMethodVisitor.class)) {
			log.log("addClassField (" + className + "," + fieldName + "," + fieldClassType + ")");
		}
		ClassAttributesMutant mut = classReferences.get(className);
		if (mut == null) {
			mut = new ClassAttributesMutant();
			mut.setName(className);
			classReferences.put(className, mut);
		}
		mut.addField(new FieldSignature(fieldName, fieldClassType));
			
	}
	
	public void addClassMethod(String className, String methodName, String desc) {
		if (log.isLogEnabled(ReferenceTrackingMethodVisitor.class)) {
			log.log("addClassMethod (" + className + "," + methodName + "," + desc + ")");
		}
		ClassAttributesMutant mut = classReferences.get(className);
		if (mut == null) {
			mut = new ClassAttributesMutant();
			mut.setName(className);
			classReferences.put(className, mut);
		}
		if (methodName != null) {
			String [] params = null;
			if ( !StringMethods.isEmpty(desc)) {
				if (log.isLogEnabled(ReferenceTrackingMethodVisitor.class)) {
					log.log("parseAsmMethodSig '" + desc + "'");
				}
				params = parseAsmMethodSigParams(desc);
			}
			String returnClassType = parseAsmMethodSigReturn(desc);
			MethodSignature ms = new MethodSignature(methodName, params, returnClassType);
			mut.addMethod(ms);
			
		}
	}

	public static String [] parseAsmMethodSigParams(String sig) {
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
	
	public static String parseAsmMethodSigReturn(String sig) {
		if (sig.indexOf(")V") != -1) {
			//no return type
			return null;
		}
		char [] chars = sig.toCharArray();
		StringBuilder sb = null;
		boolean startedReturn = false;
		boolean inClass = false;
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (c == ')') {
				sb = new StringBuilder();
				startedReturn = true;
			} else if (startedReturn) {
				if (c == ';') {
					sb.append(c);
					return sb.toString();
				} else if (ClassMethods.isClass(c)) {
					inClass = true;
					sb.append(c);
				} else if (ClassMethods.isPrimitiveClassChar(c) && !inClass) {
					sb.append(c);
					return sb.toString();
				} else {
					sb.append(c);
				}
				
			}
		}
		return null;
	}
	
	public String getCurrentMethodName() {
		return currentMethodName;
	}

	public void setCurrentMethodName(String currentMethodName) {
		this.currentMethodName = currentMethodName;
	}

	public String addWrapperIfNecessary(String classTypeName) {
		if (classTypeName == null) {
			return null;
		}
		if (classTypeName.length() == 1) {
			return classTypeName;
		}
		if (ClassMethods.isClass(classTypeName.charAt(0))) {
			return classTypeName;
		}
		if (ClassMethods.isArray(classTypeName)) {
			return classTypeName;
		}
		return "L" +classTypeName  + ";";
	}
}
