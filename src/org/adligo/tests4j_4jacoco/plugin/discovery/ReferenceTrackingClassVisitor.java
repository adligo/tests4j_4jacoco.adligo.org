package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.util.HashSet;
import java.util.Set;

import org.adligo.tests4j.models.shared.common.ClassRoutines;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.map.MapInstrConstants;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * This class reads byte code and creates a list
 * of ASM types which represent referenced classes.
 * 
 * @diagram_sync with DiscoveryOverview.seq on 8/17/2014
 * @author scott
 *
 */
public class ReferenceTrackingClassVisitor extends AbstractReferenceTrackingClassVisitor {
	private I_Tests4J_Log log;
	private Set<String> classReferences;
	
	private ReferenceTrackingMethodVisitor mv;
	private String className;
	
	public ReferenceTrackingClassVisitor(int version, I_Tests4J_Log pLog) {
		super(version);
		log = pLog;
		mv = new ReferenceTrackingMethodVisitor(super.api, log);
		reset();
	}
	
	public void reset() {
		classReferences = new HashSet<String>();
		mv.setClassReferences(classReferences);
	}
	
	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		
		className = ClassRoutines.fromTypeDescription(name);
		
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
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		
		if (!name.equals(MapInstrConstants.FIELD_NAME)) {
			classReferences.add(desc);
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
		
		mv.setCurrentMethodName(name);
		return mv;
	}

	public String getClassName() {
		return className;
	}
	
	public Set<String> getClassReferences() {
		return classReferences;
	}



}
