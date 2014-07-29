package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import org.adligo.tests4j.models.shared.common.ClassMethods;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;
import org.adligo.tests4j.run.discovery.Dependency;
import org.adligo.tests4j.run.discovery.DependencyMutant;
import org.adligo.tests4j.run.discovery.I_Dependency;
import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

/**
 * a model like class that loads classes into
 * the class loader, discovers 
 * references in the class passed in,
 * and makes sure all things down the reference
 * tree are loaded.
 * 
 * 
 * @author scott
 *
 */
public class ClassReferenceDiscovery {
	private I_CachedClassBytesClassLoader classLoader;
	private I_Tests4J_Log log;
	private ReferenceTrackingClassVisitor cv;
	private Map<String, DependencyMutant> dependencies = new HashMap<String, DependencyMutant>();
	private String currentDiscoverAndLoadClassName;
	
	public ClassReferenceDiscovery(I_CachedClassBytesClassLoader pClassLoader,
			I_Tests4J_Log pLog) {
		classLoader = pClassLoader;
		log = pLog;
		cv = new ReferenceTrackingClassVisitor(Opcodes.ASM5, log);
	}
	
	public void discoverAndLoad(Class<?> c) throws IOException, ClassNotFoundException {
		currentDiscoverAndLoadClassName = c.getName();
		dependencies.clear();
		load(c, c);
		
	}
	
	/**
	 * a list of elements in exact order
	 * @return
	 */
	public List<I_Dependency> getDependencies() {
		List<I_Dependency> toRet = new ArrayList<I_Dependency>();
		
		Collection<DependencyMutant> dms = dependencies.values();
		int refLevel = 0;
		for (DependencyMutant dm: dms) {
			int refs = dm.getReferences();
			if (refLevel < refs) {
				refLevel = refs;
			}
		}
		List<DependencyMutant> clone = new ArrayList<DependencyMutant>(dms);
		addByLevel(refLevel, clone, toRet);
		return toRet;
	}

	protected void addByLevel(int level, List<DependencyMutant> clone, List<I_Dependency> toRet) {
		Iterator<DependencyMutant> it =  clone.iterator();
		while (it.hasNext()) {
			DependencyMutant dm = it.next();
			if (dm.getReferences() == level) {
				toRet.add(new Dependency(dm));
				it.remove();
			}
		}
		if (level >= 1) {
			addByLevel(level -1, clone, toRet);
		}
	}
	
	private void load(Class<?> c, Class<?> parent) throws IOException, ClassNotFoundException {
		String className = c.getName();
		if (log.isLogEnabled(ClassReferenceDiscovery.class)) {
			log.log("Loading " + parent.getName() + " ref " + className);
		}
		String resourceName = ClassMethods.toResource(c.getName());
		InputStream in = c.getResourceAsStream(resourceName);
		classLoader.addCache(in, className);
		findDependencies(c);
	}
	
	private void findDependencies(Class<?> c) throws IOException, ClassNotFoundException {
		String className = c.getName();
		InputStream in = classLoader.getCachedBytesStream(className);
		ClassReader classReader=new ClassReader(in);
		classReader.accept(cv, 0);
		
		ClassReferencesMutant crm =  cv.getClassReferences();
		Set<String> classNames = crm.getClassNames();
		for (String name: classNames) {
			if (log.isLogEnabled(ClassReferenceDiscovery.class)) {
				log.log("adding reference " + name);
			}
			DependencyMutant dm =  dependencies.get(name);
			if (dm == null) {
				dm = new DependencyMutant();
				dm.setClazzName(name);
				//don't add reference for this or inner classes ($1, $2 exc)
				if (name.indexOf(currentDiscoverAndLoadClassName) == -1) {
					dm.addReference();
				}
				dependencies.put(name, dm);
			} else {
				if (name.indexOf(currentDiscoverAndLoadClassName) == -1) {
					dm.addReference();
				}
			}
		}
		//prevent conncurent modification exception
		List<String> currentNames = new ArrayList<String>(classNames);
		for (String name: currentNames) {
			if (classLoader.hasCache(name)) {
				if (log.isLogEnabled(ClassReferenceDiscovery.class)) {
					log.log("Cached " + c.getName() + " ref " + className);
				}
			} else {
				Class<?> rClazz = Class.forName(name);
				load(rClazz, c);
			}
		}
	}
}
