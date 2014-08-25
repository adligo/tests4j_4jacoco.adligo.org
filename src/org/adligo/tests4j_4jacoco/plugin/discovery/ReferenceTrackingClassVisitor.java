package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.adligo.tests4j.models.shared.common.ClassRoutines;
import org.adligo.tests4j.models.shared.dependency.ClassMethods;
import org.adligo.tests4j.models.shared.dependency.ClassMethodsMutant;
import org.adligo.tests4j.models.shared.dependency.I_MethodSignature;
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
	private Map<String, ClassMethodsMutant> classReferences;
	
	private ReferenceTrackingMethodVisitor mv;
	private String className;
	
	public ReferenceTrackingClassVisitor(int version, I_Tests4J_Log pLog) {
		super(version);
		log = pLog;
		mv = new ReferenceTrackingMethodVisitor(super.api, log);
		reset();
	}
	
	public void reset() {
		classReferences = new HashMap<String, ClassMethodsMutant>();
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
			mv.addClassMethod(desc, null, null);
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
	
	public List<ClassMethods> getClassCalls() {
		List<ClassMethods> methods = new ArrayList<ClassMethods>();
		Collection<ClassMethodsMutant> vals =  classReferences.values();
		for (ClassMethodsMutant val: vals) {
			methods.add(new ClassMethods(val));
		}
		return methods;
	}

	public Set<String> getClassReferences() {
		Set<String> toRet = new HashSet<String>();
		Collection<ClassMethodsMutant> vals =  classReferences.values();
		for (ClassMethodsMutant val: vals) {
			toRet.add(val.getClassName());
			Set<I_MethodSignature> methods =  val.getMethods();
			for (I_MethodSignature meth: methods) {
				for (int i = 0; i < meth.getParameters(); i++) {
					String param = meth.getParameterClassName(i);
					toRet.add(param);
				}
			}
		}
		return toRet;
	}
}
