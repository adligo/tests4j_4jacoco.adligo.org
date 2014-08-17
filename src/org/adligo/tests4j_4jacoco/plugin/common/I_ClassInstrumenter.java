package org.adligo.tests4j_4jacoco.plugin.common;

import java.io.IOException;
import java.io.InputStream;

public interface I_ClassInstrumenter {

	/**
	 * Creates a instrumented version of the given class if possible.
	 * @diagram_sync with InstrumentationOverview.sql on 8/14/2014
	 * 
	 * @param input
	 *            stream to read class definition from
	 * @param name
	 *            a name used for exception messages
	 * @return instrumented definition
	 * @throws IOException
	 *             if reading data from the stream fails or the class can't be
	 *             instrumented
	 */
	public abstract byte[] instrumentClass(InputStream input, String name)
			throws IOException;

}
