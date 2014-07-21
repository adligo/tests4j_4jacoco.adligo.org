package org.adligo.tests4j_4jacoco.plugin;

import org.adligo.tests4j.models.shared.system.I_CoveragePlugin;
import org.adligo.tests4j.models.shared.system.I_CoveragePluginFactory;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Logger;

public class SimpleJacocoPluginFactory implements I_CoveragePluginFactory  {
	@Override
	public I_CoveragePlugin create(I_Tests4J_Logger reporter) {
		SimpleJacocoPlugin toRet =  new SimpleJacocoPlugin(reporter);
		//toRet.setWriteOutInstrumentedClassFiles(true);
		return toRet;
	}
}
