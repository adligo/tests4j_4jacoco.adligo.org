package org.adligo.tests4j_4jacoco.plugin.data.wrappers;

import org.adligo.tests4j_4jacoco.plugin.data.I_ExecutionClassData;
import org.adligo.tests4j_4jacoco.plugin.data.I_ExecutionDataStore;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;

public class WrappedDataStore implements I_ExecutionDataStore {
	private ExecutionDataStore dataStore;
	
	public WrappedDataStore(ExecutionDataStore other) {
		dataStore = other;
	}

	public I_ExecutionClassData get(long id) {
		ExecutionData ed = dataStore.get(id);
		if (ed == null) {
			return null;
		}
		return new WrappedExecutionData(ed);
	}

	public boolean contains(String name) {
		return dataStore.contains(name);
	}
}