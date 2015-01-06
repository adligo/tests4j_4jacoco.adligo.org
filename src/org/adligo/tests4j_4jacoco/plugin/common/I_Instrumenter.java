package org.adligo.tests4j_4jacoco.plugin.common;

import java.io.IOException;

import org.adligo.tests4j.system.shared.api.I_Tests4J_CoverageTrialInstrumentation;
import org.adligo.tests4j.system.shared.trials.I_AbstractTrial;

public interface I_Instrumenter {

	/**
	 * Instrument any arbitrary class
	 * @param trial
	 * @return
	 * @throws IOException
	 */
	public abstract I_InstrumentedClassDependencies instrumentClass(Class<?> clazz)
			throws IOException;

}