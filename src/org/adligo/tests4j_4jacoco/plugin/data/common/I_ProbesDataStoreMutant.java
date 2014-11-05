package org.adligo.tests4j_4jacoco.plugin.data.common;

import org.adligo.tests4j.models.shared.coverage.I_ClassProbes;
import org.adligo.tests4j.models.shared.coverage.I_ClassProbesMutant;

public interface I_ProbesDataStoreMutant extends I_ProbesDataStore {
	public I_ClassProbesMutant getMutable(long classId);
	public void put(long id, I_ClassProbes classProbes);
	public void remove(long id);
}
