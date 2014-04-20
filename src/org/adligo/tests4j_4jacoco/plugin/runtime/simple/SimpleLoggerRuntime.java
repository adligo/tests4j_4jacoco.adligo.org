package org.adligo.tests4j_4jacoco.plugin.runtime.simple;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.adligo.tests4j_4jacoco.plugin.runtime.I_JacocoRuntime;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_JacocoRuntimeData;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_LoggerDataAccessorFactory;
import org.jacoco.core.internal.instr.InstrSupport;

public class SimpleLoggerRuntime implements I_JacocoRuntime {
	private static final Random RANDOM = new Random();
	private final I_LoggerDataAccessorFactory factory;

	private final Logger logger;
	private final String key;
	private final Handler handler;
	/** access to the runtime data */
	protected I_JacocoRuntimeData data;
	
	/**
	 * Creates a new runtime.
	 */
	public SimpleLoggerRuntime(I_LoggerDataAccessorFactory pFactory, Handler pHandler) {
		super();
		this.factory = pFactory;
		key = factory.getKey();
		this.logger = configureLogger();
		this.handler = pHandler;
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
		this.logger.addHandler(handler);
	}
	
	@Override
	public I_JacocoRuntimeData shutdown() {
		this.logger.removeHandler(handler);
		return data;
	}
	
	public void disconnect(final Class<?> type) throws Exception {
		if (!type.isInterface()) {
			final Field dataField = type
					.getDeclaredField(InstrSupport.DATAFIELD_NAME);
			dataField.setAccessible(true);
			dataField.set(null, null);
		}
	}

	/**
	 * Creates a random session identifier.
	 * 
	 * @return random session identifier
	 */
	public static String createRandomId() {
		return Integer.toHexString(RANDOM.nextInt());
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

	public I_JacocoRuntimeData getData() {
		return data;
	}

	@Override
	public void setup(I_JacocoRuntimeData p) {
		data = p;
	}

}
