package org.adligo.tests4j_4jacoco.plugin.data.map;

import org.adligo.tests4j_4jacoco.plugin.data.common.AbstractRuntimeData;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ExecutionDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_RuntimeData;

public class MapRuntimeData extends AbstractRuntimeData implements I_RuntimeData {
	/** store for execution data */
	protected final MapDataStore store;
	
	/**
	 * Creates a new runtime.
	 */
	public MapRuntimeData() {
		store = new MapDataStore();
	}



	/**
	 * Resets all coverage information.
	 */
	public final void reset() {
		synchronized (store) {
			store.reset();
		}
	}

	/**
	 * Returns the coverage data for the class with the given identifier. If
	 * there is no data available under the given id a new entry is created.
	 * This is a synchronized access to the underlying store.
	 * 
	 * @param id
	 *            class identifier
	 * @param name
	 *            VM name of the class
	 * @param probecount
	 *            probe data length
	 * @return execution data
	 */
	public MapExecutionData getExecutionData(final Long id, final String name,
			final int probecount) {
		synchronized (store) {
			return store.get(id, name, probecount);
		}
	}

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
	 * Return value:
	 * 
	 * <ul>
	 * <li>args[0]: probe array (<code>boolean[]</code>)
	 * </ul>
	 * 
	 * @param args
	 *            parameter array of length 3
	 */
	public void getProbes(final Object[] args) {
		final Long classid = (Long) args[0];
		final String name = (String) args[1];
		final int probecount = ((Integer) args[2]).intValue();
		args[0] = new SimpleProbesMap(
				getExecutionData(classid, name, probecount).getProbes());
	}

	/**
	 * In violation of the regular semantic of {@link Object#equals(Object)}
	 * this implementation is used as the interface to the execution data store.
	 * 
	 * @param args
	 *            the arguments as an {@link Object} array
	 * @return has no meaning
	 */
	@Override
	public boolean equals(final Object args) {
		if (args instanceof Object[]) {
			getProbes((Object[]) args);
		}
		return super.equals(args);
	}
	
	public I_ExecutionDataStore getCoverageData(String scope) {
		return store;
	}
}
