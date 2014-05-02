package org.adligo.tests4j_4jacoco.plugin;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.adligo.tests4j_4jacoco.plugin.data.common.I_RuntimeData;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.boolean_array.BooleanArrayInstrumenterFactory;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.DataInstrumenter;
import org.adligo.tests4j_4jacoco.plugin.runtime.simple.ProbeDataAccessorByLoggingApiFactory;
import org.adligo.tests4j_4jacoco.plugin.runtime.simple.SimpleLoggerRuntime;
import org.adligo.tests4j_4jacoco.plugin.runtime.simple.SimpleRuntimeData;
import org.jacoco.core.internal.instr.InstrSupport;

/**
 * This plugin can only record the top level coverage
 * simmilar to the way JUnit and Jacoco work together before this project.
 * 
 * @author scott
 *
 */
public class SimpleJacocoPlugin extends AbstractPlugin {
	
	public SimpleJacocoPlugin() {
		ProbeDataAccessorByLoggingApiFactory factory = new ProbeDataAccessorByLoggingApiFactory(
				InstrSupport.DATAFIELD_DESC);
		BooleanArrayInstrumenterFactory instrFactory = new BooleanArrayInstrumenterFactory(factory);
		DataInstrumenter cdi = new DataInstrumenter(instrFactory);
		SimpleLoggerRuntime runtime = new SimpleLoggerRuntime(factory);
		runtime.setup(new SimpleRuntimeData());
		memory = new Tests4J_4JacocoMemory(runtime, cdi);
	}


	
	



	@Override
	public boolean canSubRecord() {
		return false;
	}

}
