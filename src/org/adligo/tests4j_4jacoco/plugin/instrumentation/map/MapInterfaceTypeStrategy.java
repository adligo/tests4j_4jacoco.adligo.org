package org.adligo.tests4j_4jacoco.plugin.instrumentation.map;

import org.adligo.tests4j_4jacoco.plugin.asm.StackHelper;
import org.adligo.tests4j_4jacoco.plugin.common.I_ObtainProbesStrategy;
import org.adligo.tests4j_4jacoco.plugin.common.I_ProbeDataAccessorFactory;
import org.adligo.tests4j_4jacoco.plugin.common.I_StackHelper;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.AbstractObtainProbesStrategy;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ClassInstrumentationInfo;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MapInterfaceTypeStrategy extends AbstractObtainProbesStrategy implements I_ObtainProbesStrategy {
	
	public MapInterfaceTypeStrategy(I_ClassInstrumentationInfo pClassInfo,
			I_ProbeDataAccessorFactory pAccessorGenerator) {
		super(pClassInfo,pAccessorGenerator);
	}
	public int createProbeDataAccessorCall(final MethodVisitor mv, final int variable) {
		long id = classInfo.getId();
		String className = classInfo.getClassName();
		int probeCount = classInfo.getProbeCount();
		I_StackHelper sh = new StackHelper();
		
		final int maxStack = accessorGenerator.create(id,
				className, probeCount, mv, sh);
		mv.visitVarInsn(Opcodes.ASTORE, variable);
		return maxStack;
	}

	@Override
	public boolean hasJacocoData() {
		return false;
	}
	
	@Override
	public void createJacocoData(final ClassVisitor delegate) {
		//do nothing
	}	
	
	@Override
	public void createJacocoInit(final ClassVisitor delegate) {
		//do nothing
	}



	@Override
	public boolean hasJacocoInit() {
		return false;
	}
}
