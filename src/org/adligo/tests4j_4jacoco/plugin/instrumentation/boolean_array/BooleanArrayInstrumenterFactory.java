package org.adligo.tests4j_4jacoco.plugin.instrumentation.boolean_array;

import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.AbstractProbeInserter;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ClassInstrumentationInfo;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_InstrumenterFactory;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ObtainProbesStrategy;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.ObtainProbesStrategyType;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_ProbeDataAccessorFactory;
import org.objectweb.asm.MethodVisitor;

public class BooleanArrayInstrumenterFactory implements I_InstrumenterFactory {
	private I_ProbeDataAccessorFactory accessorFactory;

	public BooleanArrayInstrumenterFactory(I_ProbeDataAccessorFactory pAccessorFactory) {
		accessorFactory = pAccessorFactory;
	}
	
	
	public I_ProbeDataAccessorFactory getProbeDataAccessorFactory() {
		return accessorFactory;
	}

	@Override
	public I_ObtainProbesStrategy createObtainProbesStrategy(
			ObtainProbesStrategyType type, I_ClassInstrumentationInfo classInfo) {
		
		switch (type) {
			case CLASS:
				return new BooleanArrayClassTypeStrategy(classInfo, accessorFactory);
			default:
				return new BooleanArrayInterfaceTypeStrategy(classInfo, accessorFactory);
		}
	}


	@Override
	public AbstractProbeInserter createProbeInserter(int access, String desc,
			MethodVisitor mv, I_ObtainProbesStrategy arrayStrategy) {
		return new BooleanArrayProbeInserter(access, desc, mv, arrayStrategy);
	}
	
}
