package org.adligo.tests4j_4jacoco.plugin.common;

import java.io.IOException;
import java.util.List;

/**
 * Implementations of this interface find all of the dependencies for a class
 * and return them in a ordered list so they can be correctly loaded into a class loader.
 * 
 * @author scott
 *
 */
public interface I_OrderedClassDiscovery {

	/**
	 * @diagram_sync with InstrumentationOverview.seq on 8/17/2014
	 * @diagram_sync with DiscoveryOverview.seq on 8/17/2014
	 * 
	 * @param c
	 * @return A ordered list of class names, where the following conditions 
	 * are meet;
	 * For a particular class A;
	 * 1) The parent/super class of A, must precede A. 
	 * 2) The interfaces implemented by the parent/super of A, must precede A.
	 * 3) The referenced/dependency classes of the parent/super class of A, must precede A.
	 * 4) The referenced/dependency classes of the interfaces implemented by the 
	 *            parent/super class of A, must precede A.
	 * 5) The referenced/dependency classes of the interfaces implemented by class A, must precede A.
	 * 6) The referenced/dependency classes of class of A, must precede A.
	 * 
	 * There is one exception to these rules;
	 * For a particular class A which has circular references/dependencies,
	 *   the ordering rules may be overlooked ONLY for the circular references/dependencies.
	 *   
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public abstract List<String> findOrLoad(Class<?> c) throws IOException,
			ClassNotFoundException;

}