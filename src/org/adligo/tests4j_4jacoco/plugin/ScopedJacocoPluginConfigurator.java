package org.adligo.tests4j_4jacoco.plugin;

import org.adligo.tests4j.models.shared.system.I_CoveragePlugin;
import org.adligo.tests4j.models.shared.system.I_CoveragePluginConfigurator;

public class ScopedJacocoPluginConfigurator implements I_CoveragePluginConfigurator {

	@Override
	public void configure(I_CoveragePlugin p) {
		ScopedJacocoPlugin plugin = (ScopedJacocoPlugin) p;
		plugin.setWriteOutInstrumentedClassFiles(true);
	}

}
