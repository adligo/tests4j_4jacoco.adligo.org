package org.adligo.tests4j_4jacoco.plugin.runtime.simple;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.adligo.tests4j.models.shared.system.I_CoverageRecorder;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_JacocoRuntime;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_JacocoRuntimeData;
import org.jacoco.core.internal.instr.InstrSupport;

public abstract class JacocoSimpleAbstractRuntime implements I_JacocoRuntime {

	/**
	 * keep track of what is getting recorded
	 */
	protected CopyOnWriteArraySet<String> currentScopes = new CopyOnWriteArraySet<String>();
	
	/** access to the runtime data */
	protected I_JacocoRuntimeData data = new JacocoSimpleRuntimeData();
	
	public void disconnect(final Class<?> type) throws Exception {
		if (!type.isInterface()) {
			final Field dataField = type
					.getDeclaredField(InstrSupport.DATAFIELD_NAME);
			dataField.setAccessible(true);
			dataField.set(null, null);
		}
	}

	public void startup() {
	}

	private static final Random RANDOM = new Random();

	/**
	 * Creates a random session identifier.
	 * 
	 * @return random session identifier
	 */
	public static String createRandomId() {
		return Integer.toHexString(RANDOM.nextInt());
	}

}
