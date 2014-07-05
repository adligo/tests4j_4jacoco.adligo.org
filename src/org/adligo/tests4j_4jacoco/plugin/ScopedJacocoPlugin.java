package org.adligo.tests4j_4jacoco.plugin;

import org.adligo.tests4j_4jacoco.plugin.data.multi.MultiProbeDataStoreAdaptor;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.DataInstrumenter;
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
	
	public ScopedJacocoPlugin() {
		ProbeDataAccessorByLoggingApiFactory factory = new ProbeDataAccessorByLoggingApiFactory(
				MapInstrConstants.DATAFIELD_DESC);
		MapInstrumenterFactory instrFactory = new MapInstrumenterFactory(factory);
		DataInstrumenter cdi = new DataInstrumenter(instrFactory);
		SimpleLoggerRuntime runtime = new SimpleLoggerRuntime(factory);
		runtime.setup(new MultiProbeDataStoreAdaptor());
		memory = new Tests4J_4JacocoMemory(runtime, cdi);
	}


	@Override
	public boolean canThreadLocalRecord() {
		return true;
	}
}
