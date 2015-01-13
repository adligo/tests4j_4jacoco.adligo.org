package org.adligo.tests4j_4jacoco.plugin;

/**
 * These are constants which the CoveragePlugin
 * uses to obtain values passed to it from the 
 * various factories.
 * 
 * @author scott
 *
 */
public class CoveragePluginMapParams {
  /**
   * key which points to a I_Tests4J_Constants value
   */
  public static final String CONSTANTS = "constants";
  /**
   * key which points to a I_Tests4J_Log value
   */
  public static final String LOGGER = "logger";

  /**
   * key which points to a Set<String> value
   * for packages which are not instrumented 
   * (i.e. 'java.','javax.', 'org.jacoco.', 'sun.' exc
   */
  public static final String NON_INSTRUMENTED_PACKAGES = "nonInstrumentedPackage";
  
  /**
   * key which points to a Set<String> value
   */
  public static final String WHITELIST = "whitelist";
  
  /**
   * key which points to a Set<String> value
   */
  public static final String NON_RESULT_PACKAGES = "nonResultPackages";
}
