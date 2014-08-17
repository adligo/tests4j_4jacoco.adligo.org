package org.adligo.tests4j_4jacoco.plugin.common;

import java.io.IOException;

import org.adligo.tests4j.models.shared.trials.I_AbstractTrial;

public interface I_TrialInstrumenter {

	/**
	 * @diagram_sync with InstrumentationOverview.seq on 8/17/2014
	 * 
	 * @param trial
	 * @return
	 * @throws IOException
	 */
	public abstract Class<? extends I_AbstractTrial> instrument(
			Class<? extends I_AbstractTrial> trial)
			throws IOException;

}