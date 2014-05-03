package org.adligo.tests4j_4jacoco.plugin.data.multi;

import org.adligo.tests4j.models.shared.system.I_CoveragePlugin;

/**
 * a immutable class to provide a lookup (hashCode()) of the 
 * probes for a specific recorder in MultiProbeDataStore
 * 
 * @author scott
 *
 */
public class RecorderProbesId {
	/**
	 * this is the traditional classId from jacoco's ExecutionDataStore
	 */
	private long classId;
	/**
	 * this is the scope from {@link I_CoveragePlugin#createRecorder(String scope)}
	 */
	private String scope;
	/**
	 * the name of the class
	 */
	private String className;
	private int hashCode;
	
	public RecorderProbesId(long pClassId, String pScope, String pClassName) {
		classId = pClassId;
		scope = pScope;
		className = pClassName;
		hashCode = genHashCode();
	}

	public long getClassId() {
		return classId;
	}

	public String getScope() {
		return scope;
	}
	
	public String getClassName() {
		return className;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
	
	public int genHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (classId ^ (classId >>> 32));
		result = prime * result
				+ ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RecorderProbesId other = (RecorderProbesId) obj;
		if (classId != other.classId)
			return false;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (scope == null) {
			if (other.scope != null)
				return false;
		} else if (!scope.equals(other.scope))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ClassRecordingId [classId=" + classId + ", scope=" + scope
				+ ", className=" + className + "]";
	}

}
