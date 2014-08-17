package org.adligo.tests4j_4jacoco.plugin.common;


/**
 * implementations of this interface provide a pluggable
 * data type to represent the probes.
 * 
 * 
 * @author scott
 *
 */
public interface I_ClassInstrumenterFactory {
	public I_ClassInstrumenter create(I_CoveragePluginMemory memory);
}
