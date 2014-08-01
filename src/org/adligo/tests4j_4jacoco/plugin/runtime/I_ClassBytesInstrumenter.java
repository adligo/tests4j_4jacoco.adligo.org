package org.adligo.tests4j_4jacoco.plugin.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.objectweb.asm.ClassReader;

public interface I_ClassBytesInstrumenter {

	/**
	 * Creates a instrumented version of the given class if possible.
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
