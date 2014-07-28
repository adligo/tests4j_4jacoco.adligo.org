package org.adligo.tests4j_4jacoco.plugin.discovery;

import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;


public class ReferenceTrackingClassVisitor extends ClassVisitor {
	private I_Tests4J_Log log;
	
	public ReferenceTrackingClassVisitor(I_Tests4J_Log pLog) {
		super(Opcodes.ASM5);
		log = pLog;
	}
	/*
	@Override
	public MethodVisitor visitMethod(final int access, final String name,
			final String desc, final String signature, final String[] exceptions) {
		//TODO visit the method, grab visit all of the local variables and pass the 
		//types back to a reference counter, it is possible through the ASM class viewer
		// so it must be possible through the api, after class loading 
		if (log.isLogEnabled(ReferenceTrackingClassVisitor.class)) {
			log.log("");
		}
	}
	*/
}
