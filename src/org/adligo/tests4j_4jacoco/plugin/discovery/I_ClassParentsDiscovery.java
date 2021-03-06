package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.io.IOException;

import org.adligo.tests4j.models.shared.association.I_ClassParentsLocal;

public interface I_ClassParentsDiscovery {

	/**
	 * returns a ordered list of class names
	 * that 
	 * @diagram_sync with DiscoveryOverview.seq on 8/17/2014
	 * 
	 * @param c
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public abstract I_ClassParentsLocal findOrLoad(Class<?> c)
			throws IOException, ClassNotFoundException;

}