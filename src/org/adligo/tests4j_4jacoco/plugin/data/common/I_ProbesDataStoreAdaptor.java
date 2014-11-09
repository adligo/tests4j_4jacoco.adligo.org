package org.adligo.tests4j_4jacoco.plugin.data.common;

import org.adligo.tests4j.models.shared.coverage.I_ClassProbes;
import org.adligo.tests4j.models.shared.coverage.I_SourceFileProbes;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;

import java.util.concurrent.ConcurrentSkipListSet;

/**
 * this is called IRuntimeData in jacoco,
 * I changed the name to match better with what I think it is intended for
 * and how it is getting used in tests4j_4jacoco.
 * This is to adapt the I_ProbeDataStore (aka jacoco's IExecutionDataStore)
 * to the jacoco runtime, most notably getProbes(Object []); 
 * 
 * Note at one time I had some code to try to clean out unused probes
 * to keep memory footprint smaller.  After having considerable trouble
 * with this I ended up just keeping all the probes all the time.
 * The difference in memory spikes was about 7% or 100m out of 1.5g
 * for a tests4_tests run.
 * 
 * @author scott
 *
 */
public interface I_ProbesDataStoreAdaptor extends I_MultiProbesStore {
	
	/**
	 * Sets a session identifier for this runtime. The identifier is used when
	 * execution data is collected. If no identifier is explicitly set a
	 * identifier is generated from the host name and a random number. This
	 * method can be called at any time.
	 * 
	 * @see #collect(IExecutionDataVisitor, ISessionInfoVisitor, boolean)
	 * @param id
	 *            new session identifier
	 */
	public void setSessionId(final String id);

	/**
	 * Get the current a session identifier for this runtime.
	 * 
	 * @see #setSessionId(String)
	 * @return current session identifier
	 */
	public String getSessionId();

	/**
	 * Retrieves the execution probe array for a given class. The passed
	 * {@link Object} array instance is used for parameters and the return value
	 * as follows. Call parameters:
	 * 
	 * <ul>
	 * <li>args[0]: class id ({@link Long})
	 * <li>args[1]: vm class name ({@link String})
	 * <li>args[2]: probe count ({@link Integer})
	 * </ul>
	 * 
	 * Return value may be one of:
	 * 
	 * <ul>
	 * <li>args[0]: probe array (<code>boolean[]</code>)
	 * </ul>
	 *
	 *	<ul>
	 * 	<li>args[0]: Map backed by a custom Map impl so the 
	 *           mutations to the probes can be filtered for sub recordings.
	 *           note for the Map implementation clear may be called
	 *           at the end of altered methods to help the garbage collector.
	 * 	</ul>
	 * 
	 * @param args
	 *            parameter array of length 3
	 */
	public void getProbes(final Object[] args);
	
	/**
	 * start tracking probes
	 */
	public void startup();
	/**
	 * @param mainScope
	 * @return the current recorded probes for the 
	 * scope either mainScope (all the probes from any thread),
	 * or not the mainScope (the thread group of trial and
	 * test thread pertaining to a trial).
	 */
	public I_ProbesDataStore getRecordedProbes(boolean mainScope);
	
}
