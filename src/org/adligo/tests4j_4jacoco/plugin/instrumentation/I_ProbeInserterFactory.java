package org.adligo.tests4j_4jacoco.plugin.instrumentation;

import org.adligo.tests4j_4jacoco.plugin.common.I_ObtainProbesStrategy;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ClassInstrumentationInfo;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ObtainProbesOfType;
import org.objectweb.asm.MethodVisitor;

/**
 * broken out of I_ClassInstrumenterFactory
 * since it is specific to probe insertion.
 * 
 * @author scott
 *
 */
public interface I_ProbeInserterFactory {
	/**
	 * create a obtain probes strategy
	 * @param accessorFactory
	 * @param classInfo
	 * @return
	 */
	public I_ObtainProbesStrategy createObtainProbesStrategy(I_ObtainProbesOfType type, 
			I_ClassInstrumentationInfo classInfo);
	
	public AbstractProbeInserter createProbeInserter(final int access, final String desc, final MethodVisitor mv,
			final I_ObtainProbesStrategy arrayStrategy);
}
