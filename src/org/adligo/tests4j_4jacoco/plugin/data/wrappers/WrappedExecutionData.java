package org.adligo.tests4j_4jacoco.plugin.data.wrappers;

import java.util.Map;

import org.adligo.tests4j_4jacoco.plugin.data.I_ExecutionClassData;
import org.adligo.tests4j_4jacoco.plugin.data.SimpleProbes;
import org.jacoco.core.data.ExecutionData;

public class WrappedExecutionData implements I_ExecutionClassData {
	private ExecutionData data;
	
	public WrappedExecutionData(ExecutionData p) {
		data = p;
	}

	@Override
	public Map<Integer,Boolean> getProbes() {
		return SimpleProbes.toArray(data.getProbes());
	}
	
}
