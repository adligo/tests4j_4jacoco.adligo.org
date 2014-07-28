package org.adligo.tests4j_4jacoco.plugin.instrumentation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.adligo.tests4j.run.discovery.I_ClassContainer;

/**
 * a threadsafe memory container for classes,
 * it just extends ClassLoader so that they are loaded from disk only once.
 * @author scott
 *
 */
public class MemoryClassLoader extends ClassLoader implements I_ClassContainer {

	private final ConcurrentHashMap<String, byte[]> definitions = new ConcurrentHashMap<String, byte[]>();
	private final ConcurrentHashMap<String, Class<?>> classes = new ConcurrentHashMap<String, Class<?>>();
	/**
	 * Add a in-memory representation of a class.
	 * 
	 * @param name
	 *            name of the class
	 * @param bytes
	 *            class definition
	 */
	public void addDefinition(final String name, final byte[] bytes) {
		definitions.putIfAbsent(name, bytes);
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
		if (definitions.containsKey(name)) {
			if (classes.containsKey(name)) {
				return classes.get(name);
			}
			
			byte[] bytes = definitions.get(name);
			synchronized (classes) {
				if (classes.containsKey(name)) {
					return classes.get(name);
				}
				Class<?> toRet =  defineClass(name, bytes, 0, bytes.length);
				classes.putIfAbsent(name, toRet);
				return classes.get(name);
			}
		}
		return super.loadClass(name, resolve);
	}

	public List<String> getClassesInPackage(String pkgName) {
		Enumeration<String> keys =  definitions.keys();
		List<String> toRet = new ArrayList<String>();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			if (key.contains(pkgName)) {
				toRet.add(key);
			}
		}
		return toRet;
	}

	@Override
	public List<String> getAllClasses() {
		Enumeration<String> keys =  definitions.keys();
		List<String> toRet = new ArrayList<String>();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			toRet.add(key);
		}
		return toRet;
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		byte[] bytes = definitions.get(name);
		if (bytes != null) {
			return new ByteArrayInputStream(bytes);
		}
		return super.getResourceAsStream(name);
	}
 }

