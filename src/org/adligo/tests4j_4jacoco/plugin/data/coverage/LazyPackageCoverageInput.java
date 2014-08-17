package org.adligo.tests4j_4jacoco.plugin.data.coverage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;

public class LazyPackageCoverageInput {
	private String packageName;
	/**
	 * these are long class names
	 */
	private List<String> classNames = new ArrayList<String>();
	private I_ProbesDataStore probeData;
	
	public String getPackageName() {
		return packageName;
	}
	public List<String> getClassNames() {
		return classNames;
	}
	public I_ProbesDataStore getProbeData() {
		return probeData;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public void setClassNames(Collection<String> p) {
		classNames.clear();
		classNames.addAll(p);
	}
	public void setProbeData(I_ProbesDataStore probeData) {
		this.probeData = probeData;
	}
	
}
