package org.adligo.tests4j_4jacoco.plugin.common;

import org.adligo.tests4j.models.shared.coverage.I_SourceFileCoverageBrief;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ClassInstrumentationMetadata;

import java.util.Set;



/**
 * a interface for controlling the jacoco runtime
 * @author scott
 *
 */
public interface I_Runtime {
	/**
	 * starts/resumes recording for a scope. 
	 * @param scope
	 * @throws SecurityException
	 */
	public void startup() throws SecurityException;
	
	/**
	 * 
	 * @param threadGroupName
	 * @param probeJavaFilter this is either a java package name, 
	 *    or java class name which the threadGroup is paying attention to.
	 *    It comes from the @PackageScope or @SourceFileScope annotation
	 *    for the trial. Or it may be null if there is NO filter for use case trials.
	 *    
	 */
	public void putThreadGroupFilter(String threadGroupName, String probeJavaFilter);
	/**
	 * @see #putThreadGroupFilter(String, String)
	 * @param threadGroupName
	 * @return the thread group filter (a java package or class name)
	 * which this thread group name is filtering on currently.
	 */
	public String getThreadGroupFilter(String threadGroupName);
	/**
	 * clears out the classes covered
	 * @param threadGroupName
	 */
	public void clearClassesCovered(String threadGroupName);
	/**
	 * return the Set<Long> of class id's covered by this
	 * thread group
	 * @param threadGroupName
	 * @return
	 */
	public Set<Long> getClassesCovered(String threadGroupName);
	/**
	 * when a thread in the thread group with this name
	 * touches a class (changes a probe to true)
	 * this method should be called
	 * @param threadGroupName
	 * @param classId
	 */
	public void putClassCovered(String threadGroupName, long classId);
	
	/**
	 * shutsdown for all scopes
	 */
	public void shutdown();
	/**
	 * returns the coverage data and clears it out of memory.
	 * @param root determines the if this is the root recording
	 *    if not assume it is a child ThreadGroupLocal recording.
	 *    
	 * @return
	 */
	public I_ProbesDataStore end(boolean root);
	 /**
	  * This method makes sure the probes are initialized for 
	  * the class, and must be called for all classes
	  * which I_SourceFileCoverageBrief are eventually expected.
	  * This makes sure that all parent child classes
	  * have been setup, even though some of those classes
	  * were not actually touched by the execution.
   * @param className
   */
  public void ensureProbesInitialized(I_ClassInstrumentationMetadata info);
  
	/**
	 * @param threadGroupName 
	 * @param sourceFileClassName
	 * @return the source file probes for the 
   * source file which is getting tested by 
   * the trial running in thread with threadGroupName.
	 */
	public I_SourceFileCoverageBrief getSourceFileCoverage(String threadGroupName, String sourceFileClassName);
	
	 /**
   * @param sourceFileClassName
   * @return the source file probes for the 
   * source file which is getting tested by 
   * the trial running in thread with threadGroupName.
   */
  public I_SourceFileCoverageBrief getSourceFileCoverage(String sourceFileClassName);
}
