package org.adligo.tests4j_4jacoco.plugin.common;

import java.io.IOException;

import org.adligo.tests4j.models.shared.trials.I_AbstractTrial;
import org.adligo.tests4j.system.shared.I_Tests4J_CoverageTrialInstrumentation;

public interface I_TrialInstrumenter {

	/**
	 * @diagram_sync with InstrumentationOverview.seq on 8/17/2014
	 * 
	 * @param trial
	 * @return
	 * @throws IOException
	 */
	public abstract I_Tests4J_CoverageTrialInstrumentation instrument(
			Class<? extends I_AbstractTrial> trial)
			throws IOException;

	/**
	 * 
	 * @return 0.0 - 100.0
	 */
	public double getPctDone();
}