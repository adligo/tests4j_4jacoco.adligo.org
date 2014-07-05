package org.adligo.tests4j_4jacoco.plugin.runtime.simple;

import java.lang.reflect.Field;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStoreAdaptor;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_LoggerDataAccessorFactory;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_Runtime;
import org.jacoco.core.internal.instr.InstrSupport;

public class SimpleLoggerRuntime implements I_Runtime {
	private final I_LoggerDataAccessorFactory factory;

	private final Logger logger;
	private final String key;
	private final Handler handler;
	/** access to the runtime data */
	protected I_ProbesDataStoreAdaptor data;
	
	/**
	 * Creates a new runtime.
	 */
	public SimpleLoggerRuntime(I_LoggerDataAccessorFactory pFactory) {
		super();
		this.factory = pFactory;
		key = factory.getKey();
		this.logger = configureLogger();
		this.handler = new RuntimeLoggingHandler(this);
	}

	private Logger configureLogger() {
		final Logger l = Logger.getLogger(factory.getChannel());
		l.setUseParentHandlers(false);
		l.setLevel(Level.ALL);
		return l;
	}

	@Override
	public void startup() throws SecurityException {
		if (data == null) {
			throw new IllegalStateException("Null data at startup.");
		}
		data.startTracking();
		this.logger.addHandler(handler);
	}
	
	@Override
	public void shutdown() {
		this.logger.removeHandler(handler);
	}
	
	public void disconnect(final Class<?> type) throws Exception {
		if (!type.isInterface()) {
			final Field dataField = type
					.getDeclaredField(InstrSupport.DATAFIELD_NAME);
			dataField.setAccessible(true);
			dataField.set(null, null);
		}
	}

	public String getKey() {
		return key;
	}

	public Logger getLogger() {
		return logger;
	}

	public Handler getHandler() {
		return handler;
	}

	public I_ProbesDataStoreAdaptor getData() {
		return data;
	}

	public void setup(I_ProbesDataStoreAdaptor p) {
		data = p;
	}

	@Override
	public I_ProbesDataStore end() {
		return data.endTracking();
	}


}
