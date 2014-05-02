package org.adligo.tests4j_4jacoco.plugin.data.map;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.adligo.tests4j_4jacoco.plugin.data.common.I_ExecutionDataStore;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.IExecutionDataVisitor;

/**
 * In-memory data store for execution data. The data can be added through its
 * {@link IExecutionDataVisitor} interface. If execution data is provided
 * multiple times for the same class the data is merged, i.e. a probe is marked
 * as executed if it is reported as executed at least once. This allows to merge
 * coverage date from multiple runs. A instance of this class is not thread
 * safe.
 */
public final class MapDataStore implements I_MapExecutionDataVisitor, I_ExecutionDataStore {

		private final Map<Long, MapExecutionData> entries = new HashMap<Long, MapExecutionData>();

		private final Set<String> names = new HashSet<String>();

		/**
		 * Adds the given {@link ExecutionData} object into the store. If there is
		 * already execution data with this same class id, this structure is merged
		 * with the given one.
		 * 
		 * @param data
		 *            execution data to add or merge
		 * @throws IllegalStateException
		 *             if the given {@link ExecutionData} object is not compatible
		 *             to a corresponding one, that is already contained
		 * @see ExecutionData#assertCompatibility(long, String, int)
		 */
		public void put(final MapExecutionData data) throws IllegalStateException {
			final Long id = Long.valueOf(data.getId());
			final MapExecutionData entry = entries.get(id);
			if (entry == null) {
				entries.put(id, data);
				names.add(data.getName());
			} else {
				entry.merge(data);
			}
		}

		/**
		 * Subtracts the probes in the given {@link ExecutionData} object from the
		 * store. I.e. for all set probes in the given data object the corresponding
		 * probes in this store will be unset. If there is no execution data with id
		 * of the given data object this operation will have no effect.
		 * 
		 * @param data
		 *            execution data to subtract
		 * @throws IllegalStateException
		 *             if the given {@link ExecutionData} object is not compatible
		 *             to a corresponding one, that is already contained
		 * @see ExecutionData#assertCompatibility(long, String, int)
		 */
		public void subtract(final MapExecutionData data) throws IllegalStateException {
			final Long id = Long.valueOf(data.getId());
			final MapExecutionData entry = entries.get(id);
			if (entry != null) {
				entry.merge(data, false);
			}
		}

		/**
		 * Subtracts all probes in the given execution data store from this store.
		 * 
		 * @param store
		 *            execution data store to subtract
		 * @see #subtract(ExecutionData)
		 */
		public void subtract(final MapDataStore store) {
			for (final MapExecutionData data : store.getContents()) {
				subtract(data);
			}
		}

		/**
		 * Returns the {@link ExecutionData} entry with the given id if it exists in
		 * this store.
		 * 
		 * @param id
		 *            class id
		 * @return execution data or <code>null</code>
		 */
		public MapExecutionData get(final long id) {
			return entries.get(Long.valueOf(id));
		}

		/**
		 * Checks whether execution data for classes with the given name are
		 * contained in the store.
		 * 
		 * @param name
		 *            VM name
		 * @return <code>true</code> if at least one class with the name is
		 *         contained.
		 */
		public boolean contains(final String name) {
			return names.contains(name);
		}

		/**
		 * Returns the coverage data for the class with the given identifier. If
		 * there is no data available under the given id a new entry is created.
		 * 
		 * @param id
		 *            class identifier
		 * @param name
		 *            VM name of the class
		 * @param probecount
		 *            probe data length
		 * @return execution data
		 */
		public MapExecutionData get(final Long id, final String name,
				final int probecount) {
			MapExecutionData entry = entries.get(id);
			if (entry == null) {
				entry = new MapExecutionData(id.longValue(), name, probecount);
				entries.put(id, entry);
				names.add(name);
			} else {
				entry.assertCompatibility(id.longValue(), name, probecount);
			}
			return entry;
		}

		/**
		 * Resets all execution data probes, i.e. marks them as not executed. The
		 * execution data objects itself are not removed.
		 */
		public void reset() {
			for (final MapExecutionData executionData : this.entries.values()) {
				executionData.reset();
			}
		}

		/**
		 * Returns a collection that represents current contents of the store.
		 * 
		 * @return current contents
		 */
		public Collection<MapExecutionData> getContents() {
			return entries.values();
		}

		/**
		 * Writes the content of the store to the given visitor interface.
		 * 
		 * @param visitor
		 *            interface to write content to
		 */
		public void accept(final I_MapExecutionDataVisitor visitor) {
			for (final MapExecutionData data : entries.values()) {
				visitor.visitClassExecution(data);
			}
		}

		// === IExecutionDataVisitor ===

		public void visitClassExecution(final MapExecutionData data) {
			put(data);
		}
	}

