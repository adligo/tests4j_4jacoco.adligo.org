package org.adligo.tests4j_4jacoco.plugin.runtime.simple;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.adligo.tests4j_4jacoco.plugin.runtime.I_JacocoRuntimeData;

public class RuntimeLoggingHandler extends Handler {
	private final SimpleLoggerRuntime runtime;

	public RuntimeLoggingHandler(SimpleLoggerRuntime pRuntime){
		runtime = pRuntime;
	}
	
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
