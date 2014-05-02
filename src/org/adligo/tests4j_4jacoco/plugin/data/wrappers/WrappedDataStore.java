package org.adligo.tests4j_4jacoco.plugin.data.wrappers;

import java.util.Collection;
import java.util.Set;

import org.adligo.tests4j_4jacoco.plugin.data.common.I_ClassProbes;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;

public class WrappedDataStore implements I_ProbesDataStore {
	private ExecutionDataStore dataStore;
	
	public WrappedDataStore(ExecutionDataStore other) {
		dataStore = other;
	}

	public I_ClassProbes get(long id) {
		ExecutionData ed = dataStore.get(id);
		if (ed == null) {
			return null;
		}
		return new WrappedExecutionData(ed);
	}

	public boolean contains(String name) {
		return dataStore.contains(name);
	}

	@Override
	public Collection<I_ClassProbes> getAllCoverage() {
		// TODO Auto-generated method stub
		return null;
	}

}
