package org.adligo.tests4j_4jacoco.plugin.instrumentation.common;

import org.adligo.tests4j_4jacoco.plugin.runtime.I_ProbeDataAccessorFactory;

public class AbstractObtainProbesStrategy {
	protected I_ClassInstrumentationInfo classInfo;
	protected I_ProbeDataAccessorFactory accessorGenerator;
	
	public AbstractObtainProbesStrategy(I_ClassInstrumentationInfo pClassInfo,
			I_ProbeDataAccessorFactory pAccessorGenerator) {
		classInfo = pClassInfo;
		accessorGenerator = pAccessorGenerator;
	}
}
