package org.adligo.tests4j_4jacoco.plugin.discovery;

import org.adligo.tests4j.models.shared.dependency.I_ClassFilter;
import org.adligo.tests4j.run.discovery.I_ClassDependenciesCache;

public interface I_DiscoveryMemory extends I_ClassDependenciesCache, I_ClassFilter {
	/**
	 * this filters some basic classes,
	 * arrays, primitives
	 * @return
	 */
	public I_ClassFilter getBasicClassFilter();
}
