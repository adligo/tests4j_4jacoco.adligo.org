package org.adligo.tests4j_4jacoco.plugin;

import org.adligo.tests4j.models.shared.system.I_CoveragePlugin;
import org.adligo.tests4j.models.shared.system.I_CoveragePluginFactory;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Reporter;

public class ScopedJacocoPluginFactory implements I_CoveragePluginFactory  {
	@Override
	public I_CoveragePlugin create(I_Tests4J_Reporter reporter) {
		ScopedJacocoPlugin toRet =  new ScopedJacocoPlugin(reporter);
		//toRet.setWriteOutInstrumentedClassFiles(true);
		return toRet;
	}
}
