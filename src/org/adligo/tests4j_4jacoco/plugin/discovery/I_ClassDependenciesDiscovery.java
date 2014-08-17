package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.io.IOException;

import org.adligo.tests4j.models.shared.dependency.I_ClassDependenciesLocal;

/**
 * implementations should discover something about the class
 * passed to findOrLoad class.
 * 
 * Known implementations;
 * CircularDependenciesDiscovery
 * FullDependenciesDiscovery
 * InitialDependenciesDiscovery
 * 
 * @author scott
 *
 */
public interface I_ClassDependenciesDiscovery {

	/**
	 * @diagram_sync with DiscoveryOverview.seq on 8/17/2014
	 * @param c
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public abstract I_ClassDependenciesLocal findOrLoad(Class<?> c)
			throws IOException, ClassNotFoundException;

}