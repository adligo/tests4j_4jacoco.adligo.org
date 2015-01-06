package org.adligo.tests4j_4jacoco.plugin.instrumentation.map;

import org.adligo.tests4j_4jacoco.plugin.common.I_ClassInstrumenter;
import org.adligo.tests4j_4jacoco.plugin.common.I_ClassInstrumenterFactory;
import org.adligo.tests4j_4jacoco.plugin.common.I_CoveragePluginMemory;
import org.adligo.tests4j_4jacoco.plugin.common.I_ProbeDataAccessorFactory;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.ClassInstrumenter;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ClassInstrumentationMetadataStoreMutant;

public class MapClassInstrumenterFactory implements I_ClassInstrumenterFactory {

	@Override
	public I_ClassInstrumenter create(I_CoveragePluginMemory memory) {
		I_ProbeDataAccessorFactory factory = memory.getProbeDataAccessorFactory();
		I_ClassInstrumentationMetadataStoreMutant store = memory.getClassInstrumentationInfoStore();
		return new ClassInstrumenter(new MapProbeInserterFactory(factory), store);
	}

}
