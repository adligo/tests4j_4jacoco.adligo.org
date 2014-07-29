package org.adligo.tests4j_4jacoco.plugin.discovery;

import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;

/**
 * a model like class that discovers 
 * references in the class passed in.
 * It does NOT discover referenced references, 
 * or in other words just one level.
 * 
 * @author scott
 *
 */
public class ClassReferenceDiscovery {
	private I_CachedClassBytesClassLoader classLoader;
	
}
