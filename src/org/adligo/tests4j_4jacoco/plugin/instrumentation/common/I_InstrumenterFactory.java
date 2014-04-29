package org.adligo.tests4j_4jacoco.plugin.instrumentation.common;

import org.adligo.tests4j_4jacoco.plugin.runtime.I_ProbeDataAccessorFactory;
import org.objectweb.asm.MethodVisitor;

/**
 * implementations of this interface provide a pluggable
 * data type to represent the probes.
 * 
 * 
 * @author scott
 *
 */
public interface I_InstrumenterFactory {
	/**
	 * return a reference to the data accessor factory
	 * @return
	 */
	public I_ProbeDataAccessorFactory getProbeDataAccessorFactory();
	/**
	 * create a obtain probes strategy
	 * @param accessorFactory
	 * @param classInfo
	 * @return
	 */
	public I_ObtainProbesStrategy createObtainProbesStrategy(ObtainProbesStrategyType type, 
			I_ClassInstrumentationInfo classInfo);
	
	public AbstractProbeInserter createProbeInserter(final int access, final String desc, final MethodVisitor mv,
			final I_ObtainProbesStrategy arrayStrategy);
}
