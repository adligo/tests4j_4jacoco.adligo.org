package org.adligo.tests4j_4jacoco.plugin.runtime;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.adligo.tests4j.models.shared.system.I_CoverageRecorder;
import org.jacoco.core.internal.instr.InstrSupport;

public abstract class JacocoAbstractRuntime implements I_JacocoRuntime {

	/**
	 * keep track of what is getting recorded
	 */
	protected CopyOnWriteArraySet<String> currentScopes = new CopyOnWriteArraySet<String>();
	
	/** access to the runtime data */
	protected ConcurrentHashMap<String, I_JacocoRuntimeData> data =
			new ConcurrentHashMap<String, I_JacocoRuntimeData>();
	private I_JacocoRuntimeData rootData = new JacocoRuntimeData();
	
	public void disconnect(final Class<?> type) throws Exception {
		if (!type.isInterface()) {
			final Field dataField = type
					.getDeclaredField(InstrSupport.DATAFIELD_NAME);
			dataField.setAccessible(true);
			dataField.set(null, null);
		}
	}

	public void startup() {
		data.put(I_CoverageRecorder.TRIAL_RUN, rootData);
		startRecording(I_CoverageRecorder.TRIAL_RUN);
	}
	/**
	 * Subclasses must call this method when overwriting it.
	 */
	public void startRecording(final String pScope) {
		if (!data.contains(pScope)) {
			data.put(pScope, new JacocoRuntimeData());
		}
		currentScopes.add(pScope);
	}

	public I_JacocoRuntimeData stopRecording(final String pScope) {
		currentScopes.remove(pScope);
		return data.get(pScope);
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
