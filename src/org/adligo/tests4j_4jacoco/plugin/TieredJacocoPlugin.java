package org.adligo.tests4j_4jacoco.plugin;

import org.adligo.tests4j_4jacoco.plugin.instrumentation.MapDataInstrumenter;
import org.adligo.tests4j_4jacoco.plugin.runtime.multicast.MulticastLoggerRuntime;

public class TieredJacocoPlugin extends AbstractJacocoPlugin {

		public TieredJacocoPlugin() {
			MulticastLoggerRuntime runtime = new MulticastLoggerRuntime();
			MapDataInstrumenter wrapper = new MapDataInstrumenter(runtime);
			memory = new JacocoMemory(runtime, wrapper);
		}
}
