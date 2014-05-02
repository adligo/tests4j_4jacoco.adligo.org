package org.adligo.tests4j_4jacoco.plugin.data.map;

import static java.lang.String.format;

import org.adligo.tests4j_4jacoco.plugin.data.common.I_ClassProbesMutant;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_Probes;
import org.adligo.tests4j_4jacoco.plugin.data.common.Probes;
import org.jacoco.core.data.ExecutionData;

/**
 * Execution data for a single Java class. While instances are immutable care
 * has to be taken about the probe data array of type <code>boolean[]</code>
 * which can be modified.
 */
public final class MapExecutionData implements I_ClassProbesMutant {

		private final long id;

		private final String name;

		private final SimpleProbesMap probes;

		/**
		 * Creates a new {@link ExecutionData} object with the given probe data.
		 * 
		 * @param id
		 *            class identifier
		 * @param name
		 *            VM name
		 * @param probes
		 *            probe data
		 */
		public MapExecutionData(final long pId, final String pName,
				final boolean[] pProbes) {
			id = pId;
			name = pName;
			probes = new SimpleProbesMap(pProbes);
		}

		/**
		 * Creates a new {@link ExecutionData} object with the given probe data
		 * length. All probes are set to <code>false</code>.
		 * 
		 * @param id
		 *            class identifier
		 * @param name
		 *            VM name
		 * @param probeCount
		 *            probe count
		 */
		public MapExecutionData(final long id, final String name, final int probeCount) {
			this.id = id;
			this.name = name;
			this.probes = new SimpleProbesMap(probeCount);
		}

		/**
		 * Return the unique identifier for this class. The identifier is the CRC64
		 * checksum of the raw class file definition.
		 * 
		 * @return class identifier
		 */
		public long getId() {
			return id;
		}

		/**
		 * The VM name of the class.
		 * 
		 * @return VM name
		 */
		public String getName() {
			return name;
		}

		/**
		 * Returns the execution data probes. A value of <code>true</code> indicates
		 * that the corresponding probe was executed.
		 * 
		 * @return probe data
		 */
		public I_Probes getProbes() {
			return new Probes(probes.toArray());
		}

		/**
		 * Sets all probes to <code>false</code>.
		 */
		public void reset() {
			probes.reset();
		}

		/**
		 * Merges the given execution data into the probe data of this object. I.e.
		 * a probe entry in this object is marked as executed (<code>true</code>) if
		 * this probe or the corresponding other probe was executed. So the result
		 * is
		 * 
		 * <pre>
		 * A or B
		 * </pre>
		 * 
		 * The probe array of the other object is not modified.
		 * 
		 * @param other
		 *            execution data to merge
		 */
		public void merge(final MapExecutionData other) {
			merge(other, true);
		}

		/**
		 * Merges the given execution data into the probe data of this object. A
		 * probe in this object is set to the value of <code>flag</code> if the
		 * corresponding other probe was executed. For <code>flag==true</code> this
		 * corresponds to
		 * 
		 * <pre>
		 * A or B
		 * </pre>
		 * 
		 * For <code>flag==true</code> this can be considered as a subtraction
		 * 
		 * <pre>
		 * A and not B
		 * </pre>
		 * 
		 * The probe array of the other object is not modified.
		 * 
		 * @param other
		 *            execution data to merge
		 * @param flag
		 *            merge mode
		 */
		public void merge(final MapExecutionData other, final boolean flag) {
			assertCompatibility(other.getId(), other.getName(),
					other.getProbesMutant().length);
			final boolean[] otherData = other.getProbesMutant();
			for (int i = 0; i < probes.size(); i++) {
				if (otherData[i]) {
					probes.put(i, flag);
				}
			}
		}

		/**
		 * Asserts that this execution data object is compatible with the given
		 * parameters. The purpose of this check is to detect a very unlikely class
		 * id collision.
		 * 
		 * @param id
		 *            other class id, must be the same
		 * @param name
		 *            other name, must be equal to this name
		 * @param probecount
		 *            probe data length, must be the same as for this data
		 * @throws IllegalStateException
		 *             if the given parameters do not match this instance
		 */
		public void assertCompatibility(final long id, final String name,
				final int probecount) throws IllegalStateException {
			if (this.id != id) {
				throw new IllegalStateException(format(
						"Different ids (%016x and %016x).", Long.valueOf(this.id),
						Long.valueOf(id)));
			}
			if (!this.name.equals(name)) {
				throw new IllegalStateException(format(
						"Different class names %s and %s for id %016x.", this.name,
						name, Long.valueOf(id)));
			}
			if (this.probes.size() != probecount) {
				throw new IllegalStateException(format(
						"Incompatible execution data for class %s with id %016x.",
						name, Long.valueOf(id)));
			}
		}

		@Override
		public String toString() {
			return String.format("ExecutionData[name=%s, id=%016x]", name,
					Long.valueOf(id));
		}

		@Override
		public String getClassName() {
			// TODO Auto-generated method stub
			return name;
		}

		@Override
		public long getClassId() {
			return id;
		}

		@Override
		public boolean[] getProbesMutant() {
			return probes.toArray();
		}
}