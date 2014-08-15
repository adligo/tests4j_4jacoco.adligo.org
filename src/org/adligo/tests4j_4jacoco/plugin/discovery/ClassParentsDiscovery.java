package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import org.adligo.tests4j.models.shared.common.ClassMethods;
import org.adligo.tests4j.models.shared.dependency.ClassParentsLocal;
import org.adligo.tests4j.models.shared.dependency.ClassParentsLocalMutant;
import org.adligo.tests4j.models.shared.dependency.I_ClassParents;
import org.adligo.tests4j.models.shared.dependency.I_ClassParentsLocal;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;
import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;

/**
 * This class loads all parent interfaces 
 * and classes in order so as to make sure there in the 
 * I_CachedClassBytesClassLoader.
 * 
 * @author scott
 *
 */
public class ClassParentsDiscovery {
	private I_CachedClassBytesClassLoader classLoader;
	private I_Tests4J_Log log;
	private I_DiscoveryMemory discoveryMemory;
	
	public ClassParentsDiscovery(I_CachedClassBytesClassLoader pClassLoader,
			I_Tests4J_Log pLog,  I_DiscoveryMemory dc) {
		classLoader = pClassLoader;
		log = pLog;
		discoveryMemory = dc;
	}
	
	/**
	 * returns a ordered list of class names
	 * that 
	 * @param c
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public I_ClassParentsLocal findOrLoad(Class<?> c) throws IOException, ClassNotFoundException {
		String className = c.getName();
		if (log.isLogEnabled(ClassParentsDiscovery.class)) {
			log.log("ClassBytesDiscovery.findOrLoad " + className);
		}
		
		I_ClassParentsLocal toRet = discoveryMemory.getParents(className);
		if (toRet != null) {
			return toRet;
		}
		ClassParentsLocalMutant cpm = new ClassParentsLocalMutant(c);
		doSuperclasses(cpm, c);
		doInterfaces(cpm, c);
		
		
		toRet = new ClassParentsLocal(cpm);
		discoveryMemory.putParentsIfAbsent(toRet);
		loadClassBytes(c);
		return toRet;
	}
	
	private void doInterfaces(ClassParentsLocalMutant cpm, Class<?> c) throws IOException, ClassNotFoundException {
		Class<?> [] interfaces =  c.getInterfaces();
		
		Map<String, Class<?>> interMap = new TreeMap<String,Class<?>>();
		for (int i = 0; i < interfaces.length; i++) {
			Class<?> inter = interfaces[i];
			interMap.put(inter.getName(), inter);
		}
		
		Set<Entry<String,Class<?>>> entries = interMap.entrySet();
		for (Entry<String,Class<?>> e: entries) {
			I_ClassParentsLocal icps = findOrLoad(e.getValue());
			cpm.addParent(icps);
		}
	}

	
	
	private void doSuperclasses(ClassParentsLocalMutant cpm, Class<?> c) throws IOException, ClassNotFoundException {
		Stack<Class<?>> parents = new Stack<Class<?>>();
		Class<?> parent = c.getSuperclass();
		while (parent != null) {
			parents.add(parent);
			parent = parent.getSuperclass();
		}
		
		for (Class<?> p: parents) {
			String parentName = p.getName();
			I_ClassParentsLocal cp = discoveryMemory.getParents(parentName);
			if (cp != null) {
				cpm.addParent(cp);
			} else {
				I_ClassParentsLocal pcp = findOrLoad(p);
				cpm.addParent(pcp);
			}
		}
	}
	private void loadClassBytes(Class<?> c)
			throws ClassNotFoundException, IOException {
		
		String className = c.getName();
		if ( !discoveryMemory.isFiltered(c)) {
			if ( !classLoader.hasCache(className)) {
				if (log.isLogEnabled(ClassParentsDiscovery.class)) {
					log.log("ClassBytesCacheHelper.loadClassBytes " + className);
				}
				
				String resourceName = ClassMethods.toResource(className);
				InputStream in = c.getResourceAsStream(resourceName);
				if (in == null) {
					log.log("Error loading class " + resourceName);
				} else {
					classLoader.addCache(in, className);
				}
			}
		}
	}

	
}
