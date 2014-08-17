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
import org.adligo.tests4j.models.shared.dependency.I_ClassFilter;
import org.adligo.tests4j.models.shared.dependency.I_ClassParentsCache;
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
public class ClassParentsDiscovery implements I_ClassParentsDiscovery {
	private I_CachedClassBytesClassLoader classLoader;
	private I_Tests4J_Log log;
	private I_ClassFilter classFilter;
	private I_ClassParentsCache cache;
	
	public ClassParentsDiscovery(){}
	
	/** 
	 * @diagram_sync with DiscoveryOverview.seq on 8/17/2014
	 * @see org.adligo.tests4j_4jacoco.plugin.discovery.I_ClassParentsDiscovery#findOrLoad(java.lang.Class)
	 */
	@Override
	public I_ClassParentsLocal findOrLoad(Class<?> c) throws IOException, ClassNotFoundException {
		String className = c.getName();
		if (log.isLogEnabled(ClassParentsDiscovery.class)) {
			log.log("ClassBytesDiscovery.findOrLoad " + className);
		}
		
		I_ClassParentsLocal toRet = cache.getParents(className);
		if (toRet != null) {
			return toRet;
		}
		ClassParentsLocalMutant cpm = new ClassParentsLocalMutant(c);
		doSuperclasses(cpm, c);
		doInterfaces(cpm, c);
		
		
		toRet = new ClassParentsLocal(cpm);
		cache.putParentsIfAbsent(toRet);
		loadClassBytes(c);
		return toRet;
	}
	
	/**
	 * @diagram_sync with DiscoveryOverview.seq on 8/17/2014
	 * @param cpm
	 * @param c
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
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

	
	/**
	 * @diagram_sync with DiscoveryOverview.seq on 8/17/2014
	 * @param cpm
	 * @param c
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void doSuperclasses(ClassParentsLocalMutant cpm, Class<?> c) throws IOException, ClassNotFoundException {
		Stack<Class<?>> parents = new Stack<Class<?>>();
		Class<?> parent = c.getSuperclass();
		while (parent != null) {
			parents.add(parent);
			parent = parent.getSuperclass();
		}
		
		for (Class<?> p: parents) {
			String parentName = p.getName();
			I_ClassParentsLocal cp = cache.getParents(parentName);
			if (cp != null) {
				cpm.addParent(cp);
			} else {
				I_ClassParentsLocal pcp = findOrLoad(p);
				cpm.addParent(pcp);
			}
		}
	}
	/**
	 * @diagram_sync with DiscoveryOverview.seq on 8/17/2014
	 * @param c
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private void loadClassBytes(Class<?> c)
			throws ClassNotFoundException, IOException {
		
		String className = c.getName();
		if ( !classFilter.isFiltered(c)) {
			if ( !classLoader.hasCache(className)) {
				if (log.isLogEnabled(ClassParentsDiscovery.class)) {
					log.log("ClassBytesCacheHelper.loadClassBytes " + className);
				}
				
				String resourceName = ClassMethods.toResource(className);
				InputStream in = c.getResourceAsStream(resourceName);
				if (in == null) {
					log.log("Error loading class " + resourceName);
				} else {
					//@diagram_sync with DiscoveryOverview.seq on 8/17/2014
					classLoader.addCache(in, className);
				}
			}
		}
	}

	public I_CachedClassBytesClassLoader getClassLoader() {
		return classLoader;
	}

	public I_Tests4J_Log getLog() {
		return log;
	}

	public I_ClassFilter getClassFilter() {
		return classFilter;
	}

	public I_ClassParentsCache getCache() {
		return cache;
	}

	public void setClassLoader(I_CachedClassBytesClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public void setLog(I_Tests4J_Log log) {
		this.log = log;
	}

	public void setClassFilter(I_ClassFilter classFilter) {
		this.classFilter = classFilter;
	}

	public void setCache(I_ClassParentsCache cache) {
		this.cache = cache;
	}

	
}
