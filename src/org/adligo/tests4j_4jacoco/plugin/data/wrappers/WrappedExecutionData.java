package org.adligo.tests4j_4jacoco.plugin.data.wrappers;

import org.adligo.tests4j.models.shared.coverage.I_ClassProbes;
import org.adligo.tests4j.models.shared.coverage.I_Probes;
import org.adligo.tests4j.models.shared.coverage.Probes;
import org.adligo.tests4j.models.shared.coverage.ProbesMutant;
import org.jacoco.core.data.ExecutionData;

public class WrappedExecutionData implements I_ClassProbes {
	private ExecutionData data;
	
	public WrappedExecutionData(ExecutionData p) {
		data = p;
	}

	@Override
	public I_Probes getProbes() {
		return new Probes(data.getProbes());
	}

	public boolean[] getProbesInternal() {
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

  @Override
  public int getCoverageUnits() {
    return data.getProbes().length;
  }

  @Override
  public int getCoveredCoverageUnits() {
    return ProbesMutant.getCoveredCoverageUnits(data.getProbes());
  }
	
}
