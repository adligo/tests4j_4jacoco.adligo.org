package org.adligo.tests4j_4jacoco.plugin;

import java.io.File;

import org.adligo.tests4j.models.shared.system.I_Tests4J_CoveragePlugin;
import org.adligo.tests4j.models.shared.system.I_Tests4J_CoveragePluginFactory;
import org.adligo.tests4j.models.shared.system.I_Tests4J_CoveragePluginParams;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;

public class CoveragePluginFactory implements I_Tests4J_CoveragePluginFactory  {
	@Override
	public I_Tests4J_CoveragePlugin create(I_Tests4J_CoveragePluginParams params, I_Tests4J_Log pLog) {
		CoveragePlugin toRet =  new CoveragePlugin(pLog);
		
		CoveragePluginMemory memory = toRet.getMemory();
		memory.setCanThreadGroupLocalRecord(params.isCanThreadLocalGroupRecord());
		boolean writeOutClasses = params.isWriteOutInstrumentedClasses();
		
		memory.setWriteOutInstrumentedClassFiles(params.isWriteOutInstrumentedClasses());
		String output = params.getInstrumentedClassOutputFolder();
		memory.setInstrumentedClassFileOutputFolder(output);
		
		if (writeOutClasses) {
			File file = new File(output);
			if (file.exists()) {
				File[] files = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					File classFile = files[i];
					if (classFile.getName().endsWith(".class")) {
						file.delete();
					}
				}
			}
		}
		
		return toRet;
	}
}
