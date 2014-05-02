package org.adligo.tests4j_4jacoco.plugin.data.wrappers;

import org.adligo.tests4j_4jacoco.plugin.data.common.I_ClassCoverage;
import org.jacoco.core.data.ExecutionData;

public class WrappedExecutionData implements I_ClassCoverage {
	private ExecutionData data;
	
	public WrappedExecutionData(ExecutionData p) {
		data = p;
	}

	@Override
	public boolean[] getProbes() {
		return data.getProbes();
	}

	@Override
	public String getClassName() {
		return data.getName();
	}

	@Override
	public long getClassId() {
		return data.getId();
	}
	
}
