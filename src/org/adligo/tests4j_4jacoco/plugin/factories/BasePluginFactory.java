package org.adligo.tests4j_4jacoco.plugin.factories;

import org.adligo.tests4j.run.common.I_JseSystem;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;
import org.adligo.tests4j.system.shared.api.I_Tests4J_CoveragePlugin;
import org.adligo.tests4j.system.shared.api.I_Tests4J_CoveragePluginFactory;
import org.adligo.tests4j.system.shared.api.I_Tests4J_CoveragePluginParams;
import org.adligo.tests4j_4jacoco.plugin.CoveragePlugin;
import org.adligo.tests4j_4jacoco.plugin.CoveragePluginMemory;

import java.io.File;
import java.util.Map;
import java.util.Set;

public abstract class BasePluginFactory implements I_Tests4J_CoveragePluginFactory {

  public static final String NO_SYSTEM = "No System";
  private static final String NO_LOG = "No Log";

  protected I_Tests4J_Log log_;
  protected I_JseSystem system_;
  
  @Override
  public I_Tests4J_CoveragePlugin create(I_Tests4J_CoveragePluginParams params, Map<String,Object> runtimeParams) {
    I_Tests4J_Log log_ = (I_Tests4J_Log)
        runtimeParams.get(I_Tests4J_CoveragePluginFactory.LOG);
    if (log_ == null) {
      throw new NullPointerException(NO_LOG);
    }
    system_ = (I_JseSystem)
        runtimeParams.get(I_Tests4J_CoveragePluginFactory.SYSTEM);
    if (system_ == null) {
      throw new NullPointerException(NO_SYSTEM);
    }
    Set<String> whitelist = getWhitelist();
    CoveragePlugin toRet =  new CoveragePlugin(log_, whitelist);
    
    CoveragePluginMemory memory = toRet.getMemory();
    memory.setCanThreadGroupLocalRecord(params.isCanThreadLocalGroupRecord());
    boolean writeOutClasses = params.isWriteOutInstrumentedClasses();
    
    memory.setWriteOutInstrumentedClassFiles(params.isWriteOutInstrumentedClasses());
    String output = params.getInstrumentedClassOutputFolder();
    memory.setInstrumentedClassFileOutputFolder(output);
    
    if (writeOutClasses) {
      File file = system_.newFile(output);
      if (file.exists()) {
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
          File classFile = files[i];
          String fileName = classFile.getName();
          if (fileName.endsWith(".class")) {
            if (!classFile.delete()) {
              throw new RuntimeException("Problem deleting file " + fileName);
            }
          }
        }
      }
    }
    memory.setConcurrentRecording(params.isConcurrentRecording());
    return toRet;
  }
  
  protected abstract Set<String> getWhitelist();
}
