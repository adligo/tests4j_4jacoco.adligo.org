package org.adligo.tests4j_4jacoco.plugin.instrumentation;

import org.adligo.tests4j_4jacoco.plugin.common.I_ClassInstrumenterFactory;
import org.adligo.tests4j_4jacoco.plugin.common.I_CoveragePluginMemory;
import org.adligo.tests4j_4jacoco.plugin.common.I_OrderedClassDiscoveryFactory;
import org.adligo.tests4j_4jacoco.plugin.common.I_TrialInstrumenter;
import org.adligo.tests4j_4jacoco.plugin.common.I_TrialInstrumenterFactory;

public class TrialInstrumenterFactory implements I_TrialInstrumenterFactory {
	private ClassInstrumenterSharedMemory localMemory = new ClassInstrumenterSharedMemory();
	
	@Override
	public I_TrialInstrumenter create(I_CoveragePluginMemory memory) {
		TrialInstrumenter ti = new TrialInstrumenter(localMemory, memory);
		ti.setLog(memory.getLog());
		
		I_ClassInstrumenterFactory classInstrumenterFactory = memory.getClassInstrumenterFactory();
		ti.setClassInstrumenter(classInstrumenterFactory.create(memory));
		
		I_OrderedClassDiscoveryFactory factory =  memory.getOrderedClassDiscoveryFactory();
		ti.setOrderedClassDiscovery(factory.create(memory));
		
		ti.setCachedClassLoader(memory.getCachedClassLoader());
		ti.setInstrumentedClassLoader(memory.getInstrumentedClassLoader());
		ti.setClassFilter(memory.getClassFilter());
		ti.setInstrumentedClassFileOutputFolder(memory.getInstrumentedClassFileOutputFolder());
		ti.setWriteOutInstrumentedClassFiles(memory.isWriteOutInstrumentedClassFiles());
		
		
		return ti;
	}

}
