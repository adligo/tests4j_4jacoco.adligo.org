package org.adligo.tests4j_4jacoco.plugin.discovery;

import org.adligo.tests4j.models.shared.common.ClassMethods;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;
import org.adligo.tests4j.run.discovery.ClassReferencesMutant;
import org.adligo.tests4j.run.discovery.I_ClassFilter;
import org.adligo.tests4j.run.discovery.I_ClassReferences;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;


public class ReferenceTrackingClassVisitor extends ClassVisitor {
	private I_Tests4J_Log log;
	private ClassReferencesMutant classReferences;
	
	private ReferenceTrackingMethodVisitor mv;
	private I_ClassFilter classFilter;
	
	private String className;
	
	public ReferenceTrackingClassVisitor(int version, I_Tests4J_Log pLog) {
		super(version);
		log = pLog;
		mv = new ReferenceTrackingMethodVisitor(super.api, log);
		reset();
	}
	
	public void reset() {
		classReferences = new ClassReferencesMutant();
		mv.setClassReferences(classReferences);
	}
	
	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		className = name;
		if (log.isLogEnabled(ReferenceTrackingClassVisitor.class)) {
			StringBuilder sb = new StringBuilder();
			sb.append(super.toString() + " in class " + name + 
					" signature is " + signature);
			for (int i = 0; i < interfaces.length; i++) {
				sb.append(log.getLineSeperator());
				sb.append(interfaces[i]);
			}
			log.log(sb.toString());
		}
		classReferences.setClassName(className);
		
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		
		desc = ClassMethods.fromTypeDescription(desc);
		if ( !classFilter.isFiltered(desc)) {
			classReferences.addReference(desc);
		}
		return super.visitField(access, name, desc, signature, value);
	}
	
	@Override
	public MethodVisitor visitMethod(final int access, final String name,
			final String desc, final String signature, final String[] exceptions) {
		if (log.isLogEnabled(ReferenceTrackingClassVisitor.class)) {
			StringBuilder sb = new StringBuilder();
			sb.append(super.toString() + " in method " + className + 
					"." + name + " signature is " + signature);
			if (exceptions != null) {
				for (int i = 0; i < exceptions.length; i++) {
					sb.append(log.getLineSeperator());
					sb.append(exceptions[i]);
				}
			}
			log.log(sb.toString());
		}
		
		
		return mv;
	}

	public I_ClassReferences getClassReferences() {
		return classReferences;
	}

	public I_ClassFilter getClassFilter() {
		return classFilter;
	}

	public void setClassFilter(I_ClassFilter classFilter) {
		this.classFilter = classFilter;
		mv.setClassFilter(classFilter);
	}
	

}
