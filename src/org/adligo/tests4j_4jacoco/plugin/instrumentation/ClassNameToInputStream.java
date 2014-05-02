package org.adligo.tests4j_4jacoco.plugin.instrumentation;

import java.io.InputStream;

public class ClassNameToInputStream {
	
	public static InputStream getTargetClass(final String name) {
		final String resource = '/' + name.replace('.', '/') + ".class";
		return ClassNameToInputStream.class.getResourceAsStream(resource);
	}
	
	
}
