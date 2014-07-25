package org.adligo.tests4j_4jacoco.plugin;

import org.adligo.tests4j.models.shared.system.I_Tests4J_CoveragePlugin;
import org.adligo.tests4j.models.shared.system.I_Tests4J_CoveragePluginFactory;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;

public class ScopedJacocoPluginFactory implements I_Tests4J_CoveragePluginFactory  {
	@Override
	public I_Tests4J_CoveragePlugin create(I_Tests4J_Log reporter) {
		ScopedJacocoPlugin toRet =  new ScopedJacocoPlugin(reporter);
		//toRet.setWriteOutInstrumentedClassFiles(true);
		return toRet;
	}
}
