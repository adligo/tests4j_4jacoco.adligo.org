package org.adligo.tests4j_4jacoco.plugin.instrumentation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryClassLoader extends ClassLoader {

	private final ConcurrentHashMap<String, byte[]> definitions = new ConcurrentHashMap<String, byte[]>();

	/**
	 * Add a in-memory representation of a class.
	 * 
	 * @param name
	 *            name of the class
	 * @param bytes
	 *            class definition
	 */
	public void addDefinition(final String name, final byte[] bytes) {
		definitions.put(name, bytes);
	}

	public boolean hasDefinition(final String name) {
		return definitions.containsKey(name);
	}
	
	public Class<?> getClass(final String name) {
		return super.findLoadedClass(name);
	}
	@Override
	protected Class<?> loadClass(final String name, final boolean resolve)
			throws ClassNotFoundException {
		final byte[] bytes = definitions.get(name);
		if (bytes != null) {
			return defineClass(name, bytes, 0, bytes.length);
		}
		return super.loadClass(name, resolve);
	}

}

