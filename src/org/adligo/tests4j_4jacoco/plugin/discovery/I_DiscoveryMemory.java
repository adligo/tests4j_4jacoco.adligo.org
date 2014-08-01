package org.adligo.tests4j_4jacoco.plugin.discovery;

import org.adligo.tests4j.run.discovery.I_ClassDependenciesCache;

public interface I_DiscoveryMemory extends I_ClassDependenciesCache {
	public boolean isFiltered(Class<?> clazz);
}
