package org.adligo.tests4j_4jacoco.plugin;

import org.adligo.tests4j.models.shared.system.I_Tests4J_Logger;
import org.adligo.tests4j_4jacoco.plugin.data.multi.MultiProbeDataStoreAdaptor;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.map.MapInstrConstants;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.map.MapInstrumenterFactory;
import org.adligo.tests4j_4jacoco.plugin.runtime.simple.ProbeDataAccessorByLoggingApiFactory;
import org.adligo.tests4j_4jacoco.plugin.runtime.simple.SimpleLoggerRuntime;

/**
 * This plugin can only record the top level coverage
 * simmilar to the way JUnit and Jacoco work together before this project.
 * 
 * @author scott
 *
 */
public class ScopedJacocoPlugin extends AbstractPlugin {
	
	public ScopedJacocoPlugin(I_Tests4J_Logger reporter) {
		super.setTests4jLogger(reporter);
		
		ProbeDataAccessorByLoggingApiFactory factory = new ProbeDataAccessorByLoggingApiFactory(
				MapInstrConstants.DATAFIELD_DESC);
		MapInstrumenterFactory instrFactory = new MapInstrumenterFactory(factory);
		
		SimpleLoggerRuntime runtime = new SimpleLoggerRuntime(factory);
		runtime.setup(new MultiProbeDataStoreAdaptor(reporter));
		memory = new Tests4J_4JacocoMemory(runtime, instrFactory);
	}


	@Override
	public boolean canThreadGroupLocalRecord() {
		return true;
	}

}
