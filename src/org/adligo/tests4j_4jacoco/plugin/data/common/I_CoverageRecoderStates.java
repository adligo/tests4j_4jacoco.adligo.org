package org.adligo.tests4j_4jacoco.plugin.data.common;

import java.util.List;

import org.adligo.tests4j.models.shared.system.I_CoveragePlugin;

/**
 * helps share/normalize the recording states 
 * of different scopes (@see {@link I_CoveragePlugin#createRecorder(String scope)}.
 * @author scott
 *
 */
public interface I_CoverageRecoderStates {
	
	public List<String> getCurrentRecordingScopes();
}
