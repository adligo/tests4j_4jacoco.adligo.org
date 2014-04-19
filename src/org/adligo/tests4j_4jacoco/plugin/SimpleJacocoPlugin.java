package org.adligo.tests4j_4jacoco.plugin;

import org.adligo.tests4j_4jacoco.plugin.instrumentation.wrappers.JacocoInstrumenterWrapper;
import org.adligo.tests4j_4jacoco.plugin.runtime.simple.JacocoSimpleLoggerRuntime;

/**
 * This plugin can only record the top level coverage
 * simmilar to the way JUnit and Jacoco work together before this project.
 * 
 * @author scott
 *
 */
public class SimpleJacocoPlugin extends AbstractJacocoPlugin {

	public SimpleJacocoPlugin() {
		JacocoSimpleLoggerRuntime runtime = new JacocoSimpleLoggerRuntime();
		JacocoInstrumenterWrapper wrapper = new JacocoInstrumenterWrapper(runtime);
		memory = new JacocoMemory(runtime, wrapper);
	}
}
