package org.adligo.tests4j_4jacoco.plugin.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.objectweb.asm.ClassReader;

public interface I_Instrumenter {
	/**
	 * Determines whether signatures should be removed from JAR files. This is
	 * typically necessary as instrumentation modifies the class files and
	 * therefore invalidates existing JAR signatures. Default is
	 * <code>true</code>.
	 * 
	 * @param flag
	 *            <code>true</code> if signatures should be removed
	 */
	public abstract void setRemoveSignatures(boolean flag);

	/**
	 * Creates a instrumented version of the given class if possible.
	 * 
	 * @param reader
	 *            definition of the class as ASM reader
	 * @return instrumented definition
	 * 
	 */
	public abstract byte[] instrument(ClassReader reader);

	/**
	 * Creates a instrumented version of the given class if possible.
	 * 
	 * @param buffer
	 *            definition of the class
	 * @param name
	 *            a name used for exception messages
	 * @return instrumented definition
	 * @throws IOException
	 *             if the class can't be analyzed
	 */
	public abstract byte[] instrument(byte[] buffer, String name)
			throws IOException;

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
	public abstract byte[] instrument(InputStream input, String name)
			throws IOException;

	/**
	 * Creates a instrumented version of the given class file.
	 * 
	 * @param input
	 *            stream to read class definition from
	 * @param output
	 *            stream to write the instrumented version of the class to
	 * @param name
	 *            a name used for exception messages
	 * @throws IOException
	 *             if reading data from the stream fails or the class can't be
	 *             instrumented
	 */
	public abstract void instrument(InputStream input, OutputStream output,
			String name) throws IOException;

	/**
	 * Creates a instrumented version of the given resource depending on its
	 * type. Class files and the content of archive files are instrumented. All
	 * other files are copied without modification.
	 * 
	 * @param input
	 *            stream to contents from
	 * @param output
	 *            stream to write the instrumented version of the contents
	 * @param name
	 *            a name used for exception messages
	 * @return number of instrumented classes
	 * @throws IOException
	 *             if reading data from the stream fails or a class can't be
	 *             instrumented
	 */
	public abstract int instrumentAll(InputStream input, OutputStream output,
			String name) throws IOException;
}
