package org.adligo.tests4j_4jacoco.plugin.instrumentation.wrappers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.adligo.tests4j_4jacoco.plugin.runtime.I_Instrumenter;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.objectweb.asm.ClassReader;

public class JacocoInstrumenterWrapper implements I_Instrumenter {
	private Instrumenter instance;
	
	public JacocoInstrumenterWrapper(IExecutionDataAccessorGenerator runtime) {
		instance = new Instrumenter(runtime);
	}

	public int hashCode() {
		return instance.hashCode();
	}

	public void setRemoveSignatures(boolean flag) {
		instance.setRemoveSignatures(flag);
	}

	public byte[] instrument(ClassReader reader) {
		return instance.instrument(reader);
	}

	public byte[] instrument(byte[] buffer, String name) throws IOException {
		return instance.instrument(buffer, name);
	}

	public boolean equals(Object obj) {
		return instance.equals(obj);
	}

	public byte[] instrument(InputStream input, String name) throws IOException {
		return instance.instrument(input, name);
	}

	public void instrument(InputStream input, OutputStream output, String name)
			throws IOException {
		instance.instrument(input, output, name);
	}

	public int instrumentAll(InputStream input, OutputStream output, String name)
			throws IOException {
		return instance.instrumentAll(input, output, name);
	}

	public String toString() {
		return instance.toString();
	}
}
