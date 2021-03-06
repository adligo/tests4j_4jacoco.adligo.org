package org.adligo.tests4j_4jacoco.plugin.instrumentation;

import org.adligo.tests4j_4jacoco.plugin.common.I_ClassInstrumenter;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.ClassInstrumentaionMetadata;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.ClassProbesAdapter;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.I_ClassInstrumentationMetadataStoreMutant;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.common.StrategySelectionInstrumenter;
import org.jacoco.core.internal.ContentTypeDetector;
import org.jacoco.core.internal.Pack200Streams;
import org.jacoco.core.internal.data.CRC64;
import org.jacoco.core.internal.instr.SignatureRemover;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
/**
 * Several APIs to instrument Java class definitions for coverage tracing.
 */
public class ClassInstrumenter implements I_ClassInstrumenter {

	private final I_ProbeInserterFactory instrumenterFactory;
	private final SignatureRemover signatureRemover;
	private final I_ClassInstrumentationMetadataStoreMutant store_;
	
	/**
	 * Creates a new instance based on the given runtime.
	 * 
	 * @param runtime
	 *            runtime used by the instrumented classes
	 */
	public ClassInstrumenter(final I_ProbeInserterFactory pInstrumenterFactory, I_ClassInstrumentationMetadataStoreMutant store) {
		instrumenterFactory = pInstrumenterFactory;
		this.signatureRemover = new SignatureRemover();
		store_ = store;
	}

	/* (non-Javadoc)
	 * @see org.adligo.tests4j_4jacoco.plugin.instrumentation.I_I#setRemoveSignatures(boolean)
	 */
	public void setRemoveSignatures(final boolean flag) {
		signatureRemover.setActive(flag);
	}

	/**
	 * Creates a ASM adapter for a class with the given id.
	 * 
	 * @param classid
	 *            id of the class calculated with {@link CRC64}
	 * @param cv
	 *            next class visitor in the chain
	 * @return new visitor to write class definition to
	 */
	private ClassVisitor createInstrumentingVisitor(final long classid,
			final ClassVisitor cv, String className) {
		StrategySelectionInstrumenter jci = new StrategySelectionInstrumenter(classid,
				instrumenterFactory, cv);
		store_.add(new ClassInstrumentaionMetadata(classid, className, jci));
		return new ClassProbesAdapter(jci, true);
	}

	/* (non-Javadoc)
	 * @see org.adligo.tests4j_4jacoco.plugin.instrumentation.I_I#instrument(org.objectweb.asm.ClassReader)
	 */
	public byte[] instrument(final ClassReader reader, String className) {
		final ClassWriter writer = new ClassWriter(reader, 0);
		final ClassVisitor visitor = createInstrumentingVisitor(
				CRC64.checksum(reader.b), writer, className);
		reader.accept(visitor, ClassReader.EXPAND_FRAMES);
		return writer.toByteArray();
	}

	/* (non-Javadoc)
	 * @see org.adligo.tests4j_4jacoco.plugin.instrumentation.I_I#instrument(byte[], java.lang.String)
	 */
	public byte[] instrument(final byte[] buffer, final String className)
			throws IOException {
		try {
			return instrument(new ClassReader(buffer), className);
		} catch (final RuntimeException e) {
			throw instrumentError(className, e);
		}
	}

	/* (non-Javadoc)
	 * @see org.adligo.tests4j_4jacoco.plugin.instrumentation.I_I#instrument(java.io.InputStream, java.lang.String)
	 */
	@Override
	public byte[] instrumentClass(final InputStream input, final String className)
			throws IOException {
		try {
			return instrument(new ClassReader(input), className);
		} catch (final RuntimeException e) {
			throw instrumentError(className, e);
		}
	}

	/* (non-Javadoc)
	 * @see org.adligo.tests4j_4jacoco.plugin.instrumentation.I_I#instrument(java.io.InputStream, java.io.OutputStream, java.lang.String)
	 */
	public void instrument(final InputStream input, final OutputStream output,
			final String className) throws IOException {
		try {
			output.write(instrument(new ClassReader(input), className));
		} catch (final RuntimeException e) {
			throw instrumentError(className, e);
		}
	}

	private IOException instrumentError(final String name,
			final RuntimeException cause) {
		final IOException ex = new IOException(String.format(
				"Error while instrumenting class %s.", name));
		ex.initCause(cause);
		return ex;
	}

	/* (non-Javadoc)
	 * @see org.adligo.tests4j_4jacoco.plugin.instrumentation.I_I#instrumentAll(java.io.InputStream, java.io.OutputStream, java.lang.String)
	 */
	public int instrumentAll(final InputStream input,
			final OutputStream output, final String name) throws IOException {
		final ContentTypeDetector detector = new ContentTypeDetector(input);
		switch (detector.getType()) {
		case ContentTypeDetector.CLASSFILE:
			instrument(detector.getInputStream(), output, name);
			return 1;
		case ContentTypeDetector.ZIPFILE:
			return instrumentZip(detector.getInputStream(), output, name);
		case ContentTypeDetector.GZFILE:
			return instrumentGzip(detector.getInputStream(), output, name);
		case ContentTypeDetector.PACK200FILE:
			return instrumentPack200(detector.getInputStream(), output, name);
		default:
			copy(detector.getInputStream(), output);
			return 0;
		}
	}

	private int instrumentZip(final InputStream input,
			final OutputStream output, final String name) throws IOException {
		final ZipInputStream zipin = new ZipInputStream(input);
		final ZipOutputStream zipout = new ZipOutputStream(output);
		ZipEntry entry;
		int count = 0;
		while ((entry = zipin.getNextEntry()) != null) {
			final String entryName = entry.getName();
			if (signatureRemover.removeEntry(entryName)) {
				continue;
			}

			zipout.putNextEntry(new ZipEntry(entryName));
			if (!signatureRemover.filterEntry(entryName, zipin, zipout)) {
				count += instrumentAll(zipin, zipout, name + "@" + entryName);
			}
			zipout.closeEntry();
		}
		zipout.finish();
		return count;
	}

	private int instrumentGzip(final InputStream input,
			final OutputStream output, final String name) throws IOException {
		final GZIPOutputStream gzout = new GZIPOutputStream(output);
		final int count = instrumentAll(new GZIPInputStream(input), gzout, name);
		gzout.finish();
		return count;
	}

	private int instrumentPack200(final InputStream input,
			final OutputStream output, final String name) throws IOException {
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final int count = instrumentAll(Pack200Streams.unpack(input), buffer,
				name);
		Pack200Streams.pack(buffer.toByteArray(), output);
		return count;
	}

	private void copy(final InputStream input, final OutputStream output)
			throws IOException {
		final byte[] buffer = new byte[1024];
		int len;
		while ((len = input.read(buffer)) != -1) {
			output.write(buffer, 0, len);
		}
	}

}

