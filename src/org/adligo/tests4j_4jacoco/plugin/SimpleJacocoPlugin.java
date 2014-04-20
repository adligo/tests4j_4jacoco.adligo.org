package org.adligo.tests4j_4jacoco.plugin;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.adligo.tests4j_4jacoco.plugin.asm.LoggingDataAccessorFactory;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.MapInstrConstants;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.wrappers.JacocoInstrumenterWrapper;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_JacocoRuntimeData;
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
public class SimpleJacocoPlugin extends AbstractJacocoPlugin {
	private final SimpleLoggerRuntime runtime;
	
	public SimpleJacocoPlugin() {
		LoggingDataAccessorFactory factory = new LoggingDataAccessorFactory(
				InstrSupport.DATAFIELD_DESC);
		JacocoInstrumenterWrapper wrapper = new JacocoInstrumenterWrapper(factory);
		Handler handler = new RuntimeHandler();
		runtime = new SimpleLoggerRuntime(factory, handler);
		runtime.setup(new SimpleRuntimeData());
		memory = new JacocoMemory(runtime, wrapper);
	}

	@Override
	public boolean hasSupportForRecorderScope() {
		return false;
	}
	
	private class RuntimeHandler extends Handler {
		
		public RuntimeHandler(){}
		
		@Override
		public void publish(final LogRecord record) {
			String key = runtime.getKey();
			if (key.equals(record.getMessage())) {
				Object [] params = record.getParameters();
				I_JacocoRuntimeData data = runtime.getData();
				data.getProbes(params);
			}
		}

		@Override
		public void flush() {
			// nothing to do
		}

		@Override
		public void close() throws SecurityException {
			// The Java logging framework removes and closes all handlers on JVM
			// shutdown. As soon as our handler has been removed, all classes
			// that might get instrumented during shutdown (e.g. loaded by other
			// shutdown hooks) will fail to initialize. Therefore we add ourself
			// again here. This is a nasty hack that might fail in some Java
			// implementations.
			Logger logger = runtime.getLogger();
			Handler handler = runtime.getHandler();
			logger.addHandler(handler);
		}
	}

}