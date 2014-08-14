package org.adligo.tests4j_4jacoco.plugin.discovery;

import org.adligo.tests4j.models.shared.dependency.I_ClassDependenciesCache;
import org.adligo.tests4j.models.shared.dependency.I_ClassFilter;
import org.adligo.tests4j.models.shared.dependency.I_ClassReferencesCache;

public interface I_DiscoveryMemory extends I_ClassDependenciesCache, I_ClassFilter, I_ClassReferencesCache {
	/**
	 * this filters some basic classes,
	 * arrays, primitives
	 * @return
	 */
	public I_ClassFilter getBasicClassFilter();
}
