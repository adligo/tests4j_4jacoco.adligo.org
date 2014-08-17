package org.adligo.tests4j_4jacoco.plugin.instrumentation.map;

import org.adligo.tests4j_4jacoco.plugin.common.I_ObtainProbesStrategy;
import org.adligo.tests4j_4jacoco.plugin.common.I_ProbeDataAccessorFactory;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.AbstractProbeInserter;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.I_ProbeInserterFactory;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ClassInstrumentationInfo;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.ObtainProbesStrategyType;
import org.objectweb.asm.MethodVisitor;

public class MapProbeInserterFactory implements I_ProbeInserterFactory {
	private I_ProbeDataAccessorFactory accessorFactory;

	public MapProbeInserterFactory(I_ProbeDataAccessorFactory pAccessorFactory) {
		accessorFactory = pAccessorFactory;
	}
	
	
	public I_ProbeDataAccessorFactory getProbeDataAccessorFactory() {
		return accessorFactory;
	}

	public I_ObtainProbesStrategy createObtainProbesStrategy(
			ObtainProbesStrategyType type, I_ClassInstrumentationInfo classInfo) {
		
		switch (type) {
			case CLASS:
				return new MapClassTypeStrategy(classInfo, accessorFactory);
			default:
				return new MapInterfaceTypeStrategy(classInfo, accessorFactory);
		}
	}

	public AbstractProbeInserter createProbeInserter(int access, String desc,
			MethodVisitor mv, I_ObtainProbesStrategy arrayStrategy) {
		return new MapProbeInserter(access, desc, mv, arrayStrategy);
	}

}
