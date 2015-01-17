package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.adligo.tests4j.shared.asserts.reference.ClassAttributes;
import org.adligo.tests4j.shared.asserts.reference.ClassAttributesMutant;
import org.adligo.tests4j.shared.asserts.reference.I_MethodSignature;
import org.adligo.tests4j.shared.common.ClassMethods;
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
	private Map<String, ClassAttributesMutant> classReferences;
	
	private ReferenceTrackingMethodVisitor mv;
	private String className;
	private String superClassName;
	private String [] interfaceNames;
	
	public ReferenceTrackingClassVisitor(int version, I_Tests4J_Log pLog) {
		super(version);
		log = pLog;
		mv = new ReferenceTrackingMethodVisitor(super.api, log);
		reset();
	}
	
	public void reset() {
		classReferences = new HashMap<String, ClassAttributesMutant>();
		mv.setClassReferences(classReferences);
		className = null;
		superClassName = null;
		interfaceNames  = null;
	}
	
	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		
		className = ClassMethods.fromTypeDescription("L" + name + ";");
		if (superName != null) {
			superClassName = "L" + superName + ";";
		}
		if (interfaces != null && interfaces.length >= 1) {
			interfaceNames = interfaces;
			for (int i = 0; i < interfaces.length; i++) {
				interfaceNames[i] = "L" + interfaces[i]  + ";";
			}
		}
		if (log.isLogEnabled(ReferenceTrackingClassVisitor.class)) {
			
			StringBuilder sb = new StringBuilder();
			sb.append(super.toString() + " in class " + name + 
					" signature is " + signature);
			for (int i = 0; i < interfaces.length; i++) {
				sb.append(log.lineSeparator());
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
					sb.append(log.lineSeparator());
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
	
	/**
	 * @see AbstractReferenceTrackingClassVisitor#getClassReferences()
	 * @return
	 */
	@Override
	public List<ClassAttributes> getClassReferences() {
		List<ClassAttributes> refs = new ArrayList<ClassAttributes>();
		Set<String> refNames = new HashSet<String>();
		Collection<ClassAttributesMutant> vals =  classReferences.values();
		for (ClassAttributesMutant val: vals) {
			if (val.getName() != null) {
				refs.add(new ClassAttributes(val));
				refNames.add(val.getName());
			}
		}
		if (!refNames.contains(superClassName)) {
			ClassAttributesMutant cam = new ClassAttributesMutant();
			cam.setName(superClassName);
			refs.add(new ClassAttributes(cam));
		}
		if (interfaceNames != null) {
			for (int i = 0; i < interfaceNames.length; i++) {
				String iname = interfaceNames[i];
				if (!refNames.contains(iname)) {
					ClassAttributesMutant cam = new ClassAttributesMutant();
					cam.setName(iname);
					refs.add(new ClassAttributes(cam));
				}
			}
		}
		return refs;
	}

	
	public Set<String> getClassReferenceNames() {
		Set<String> toRet = new HashSet<String>();
		Collection<ClassAttributesMutant> vals =  classReferences.values();
		for (ClassAttributesMutant val: vals) {
			toRet.add(val.getName());
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

	public String getSuperClassName() {
		return superClassName;
	}

	public void setSuperClassName(String superClassName) {
		this.superClassName = superClassName;
	}

	public String[] getInterfaceNames() {
		return interfaceNames;
	}

	public void setInterfaceNames(String[] interfaceNames) {
		this.interfaceNames = interfaceNames;
	}

}
