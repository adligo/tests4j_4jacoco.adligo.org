package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.adligo.tests4j.models.shared.common.ClassMethods;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;
import org.adligo.tests4j.run.discovery.ClassReferences;
import org.adligo.tests4j.run.discovery.ClassReferencesMutant;
import org.adligo.tests4j.run.discovery.I_ClassDependencies;
import org.adligo.tests4j.run.discovery.I_ClassReferences;
import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

/**
 * a model like (non thread safe) class that loads classes into
 * the class loader, discovers 
 * references.
 * 
 * references and dependencies are very similar concepts in this package as follows;
 * references illuminate that one class references another.
 * dependencies illuminate that one class depends on another, 
 *     and indicate 
 *    The I_CachedClassBytesClassLoader is shared memory between threads.
 * Also this model keeps a cache of the references for classes
 * it reads, so it doesn't need to re-ASM byte code read them.
 * 
 * 
 * @author scott
 *
 */
public class ClassReferencesDiscovery {
	private I_CachedClassBytesClassLoader classLoader;
	private I_Tests4J_Log log;
	private ReferenceTrackingClassVisitor cv;
	private I_DiscoveryMemory discoveryMemory;
	private Map<String, I_ClassReferences> refMap = new HashMap<String,I_ClassReferences>();
	
	public ClassReferencesDiscovery(I_CachedClassBytesClassLoader pClassLoader,
			I_Tests4J_Log pLog,  I_DiscoveryMemory dc) {
		classLoader = pClassLoader;
		log = pLog;
		discoveryMemory = dc;
		cv = new ReferenceTrackingClassVisitor(Opcodes.ASM5, log);
		cv.setClassFilter(dc);
	}
	
	public List<String> discoverAndLoad(Class<?> c) throws IOException, ClassNotFoundException {
		refMap.clear();
		Stack<Class<?>> recStack = new Stack<Class<?>>();
		loadReferences(c, c, recStack);
		List<String> refOrder = calcRefOrder(c);
		return refOrder;
	}

	
	/**
	 * load the class into the shared class loader and
	 * find references, put them in the refMap
	 * 
	 * @param c
	 * @param parent
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private I_ClassReferences loadReferences(Class<?> c, Class<?> parent, Stack<Class<?>> recursionStack) throws IOException, ClassNotFoundException {
		if (discoveryMemory.isFiltered(c)) {
			return null;
		}
		String className = c.getName();
		recursionStack.add(c);
		if ( !classLoader.hasCache(className)) {
			if (log.isLogEnabled(ClassReferencesDiscovery.class)) {
				log.log("Loading " + parent.getName() + " reference " + className);
			}
			String resourceName = ClassMethods.toResource(className);
			InputStream in = c.getResourceAsStream(resourceName);
			if (in == null) {
				log.log("Error loading class " + resourceName);
			} else {
				classLoader.addCache(in, className);
			}
		}
		return loadAndFindAllRefs(c, recursionStack);
	}
	
	private I_ClassReferences loadAndFindAllRefs(Class<?> c, Stack<Class<?>> recursionStack) throws IOException, ClassNotFoundException {
		String className = c.getName();
		
		//check cache, to keep ASM visitor calls down because they are expensive
		I_ClassDependencies cdeps = discoveryMemory.get(className);
		if (cdeps != null) {
			ClassReferences toRet = new ClassReferences(cdeps);
			refMap.put(className, toRet);
			return toRet;
		}
		//add references from ASM, byte code inspection
		InputStream in = classLoader.getCachedBytesStream(className);
		ClassReader classReader=new ClassReader(in);
		cv.reset();
		classReader.accept(cv, 0);
		I_ClassReferences asmRefs = cv.getClassReferences();
		Set<String> classNames = asmRefs.getReferences();
		
		ClassReferencesMutant crm = readReflectionReferences(c, asmRefs,
				classNames);
		
		classNames = crm.getReferences();
		doRecursion(c, classNames, recursionStack);
		ClassReferences toRet = new ClassReferences(crm);
		refMap.put(className, toRet);
		recursionStack.remove(c);
		return toRet;
	}

	protected void doRecursion(Class<?> c, 
			Set<String> classNames, Stack<Class<?>> recursionStack) throws ClassNotFoundException, IOException {
		List<String> currentNames = new ArrayList<String>(classNames);

		for (Class<?> clazz: recursionStack) {
			currentNames.remove(clazz.getName());
		}
		for (String name: currentNames) {
			I_ClassDependencies cdeps = discoveryMemory.get(name);
			if (cdeps != null) {
				ClassReferences toRet = new ClassReferences(cdeps);
				String className = c.getName();
				refMap.put(className, toRet);
			} else {
				Class<?> child = Class.forName(name);
				loadReferences(child, c, recursionStack);
			}
		}
	}

	protected ClassReferencesMutant readReflectionReferences(Class<?> c,
			I_ClassReferences asmRefs, Set<String> classNames) {
		ClassReferencesMutant crm = new ClassReferencesMutant(asmRefs);
		
		//add references from reflection, for abstract methods, with no byte code
		Method [] methods =  c.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			Method m = methods[i];
			Class<?> returnClazz =  m.getReturnType();
			addReflectionNames(classNames, returnClazz, crm);
			Class<?> [] exceptions = m.getExceptionTypes();
			for (int j = 0; j < exceptions.length; j++) {
				Class<?> e = exceptions[j];
				addReflectionNames(classNames, e, crm);
			}
			Class<?> [] params =  m.getParameterTypes();
			for (int j = 0; j < params.length; j++) {
				Class<?> p = params[j];
				addReflectionNames(classNames, p, crm);
			}
		}
		return crm;
	}
	
	/**
	 * ok at this point either we have 
	 * just references, or a mix of references and 
	 * cached dependencies.  
	 * Calculate the most referenced in the group and order
	 * 
	 * This calculates a rough order of dependencies
	 * @return
	 */
	private List<String> calcRefOrder(Class<?> c) {
		String topName = c.getName();
		Map<String,Integer> refCounts = new HashMap<String,Integer>();
		
		Set<Entry<String, I_ClassReferences>> refs =  refMap.entrySet();
		for (Entry<String,I_ClassReferences> e: refs) {
			String className = e.getKey();
			
			if (isNotClassOrInnerClass(className, topName)) {
				Integer count = refCounts.get(className);
				
				if (count == null) {
					count = 1;
				} else {
					count = count + 1;
				}
				refCounts.put(className, count);
				
				I_ClassReferences crs = e.getValue();
				Set<String> classNames = crs.getReferences();
				for (String name: classNames) {
					if (isNotClassOrInnerClass(name, topName)) {
						count = refCounts.get(name);
						if (count == null) {
							count = 1;
						} else {
							count = count + 1;
						}
						refCounts.put(name, count);
					}
				}
			}
		}
		/*
		if (log.isLogEnabled(ClassReferencesDiscovery.class)) {
			Set<Entry<String,Integer>> entries = refCounts.entrySet();
			StringBuilder sb = new StringBuilder();
			sb.append("reference counts are as follows;" + log.getLineSeperator());
			for (Entry<String,Integer> e: entries) {
				sb.append("" + e.getKey() + "=" + e.getValue()
						+ log.getLineSeperator());
				
			}
			log.log(sb.toString());
		}
		*/
		int max = 0;
		Collection<Integer> counts = refCounts.values();
		for (Integer ct: counts) {
			if (ct > max) {
				max = ct;
			}
		}
		List<String> toRet = new ArrayList<String>();
		for (int i = max; i >= 0; i--) {
			Set<String> keys = refCounts.keySet();
			//prevent concurrent modification exception
			Set<String> keys2 = new HashSet<String>(keys);
			for (String name: keys2) {
				Integer count = refCounts.get(name);
				if (count >= i) {
					toRet.add(name);
					refCounts.remove(name);
				}
			}
		}
		boolean adding = true;
		int count = 1;
		while (adding) {
			if (refMap.containsKey(topName + "$" + count)) {
				toRet.add(topName + "$" + count);
			} else {
				adding = false;
			}
			count++;
		}
		toRet.add(topName);
		return toRet;
	}
	
	private boolean isNotClassOrInnerClass(String className, String topName) {
		if (className.equals(topName)) {
			return false;
		} else if (className.indexOf(topName + "$") == 0) {
			return false;
		}
		return true;
	}
	protected void addReflectionNames(Set<String> classNames, Class<?> clazz, ClassReferencesMutant classReferences) {
		if (clazz != null) {
			//don't add arrays
			if (clazz.isArray()) {
				Class<?> type = clazz.getComponentType();
				if ( !discoveryMemory.isFiltered(type)) {
					classReferences.addReference(type.getName());
				}
			} else {
				if ( !discoveryMemory.isFiltered(clazz)) {
					classReferences.addReference(clazz.getName());
				}
			}
		}
	}
	
	
	/**
	 * @diagram_sync with Discovery_ClassReferenceDiscovery.seq on 8/1/2014
	 * @diagram_sync with Discovery_ClassInstrumenter.seq on 8/1/2014
	 * 
	 * @param className
	 * @return
	 */
	public I_ClassReferences getReferences(String className) {
		return refMap.get(className);
	}
}
