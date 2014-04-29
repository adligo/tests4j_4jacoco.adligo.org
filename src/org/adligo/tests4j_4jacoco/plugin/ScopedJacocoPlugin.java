package org.adligo.tests4j_4jacoco.plugin;

import java.util.logging.Handler;

import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.DataInstrumenter;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.map.MapInstrConstants;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.map.MapInstrumenterFactory;
import org.adligo.tests4j_4jacoco.plugin.runtime.simple.ProbeDataAccessorByLoggingApiFactory;
import org.adligo.tests4j_4jacoco.plugin.runtime.simple.RuntimeLoggingHandler;
import org.adligo.tests4j_4jacoco.plugin.runtime.simple.SimpleLoggerRuntime;
import org.adligo.tests4j_4jacoco.plugin.runtime.simple.SimpleRuntimeData;

/**
 * This plugin can only record the top level coverage
 * simmilar to the way JUnit and Jacoco work together before this project.
 * 
 * @author scott
 *
 */
public class ScopedJacocoPlugin extends AbstractJacocoPlugin {
	
	public ScopedJacocoPlugin() {
		ProbeDataAccessorByLoggingApiFactory factory = new ProbeDataAccessorByLoggingApiFactory(
				MapInstrConstants.DATAFIELD_DESC);
		MapInstrumenterFactory instrFactory = new MapInstrumenterFactory(factory);
		DataInstrumenter cdi = new DataInstrumenter(instrFactory);
		SimpleLoggerRuntime runtime = new SimpleLoggerRuntime(factory);
		runtime.setup(new SimpleRuntimeData());
		memory = new JacocoMemory(runtime, cdi);
	}


	@Override
	public boolean canSubRecord() {
		return true;
	}
}
