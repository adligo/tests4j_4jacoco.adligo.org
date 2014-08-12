package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.adligo.tests4j.models.shared.common.ClassMethods;
import org.adligo.tests4j.models.shared.dependency.ClassFilter;
import org.adligo.tests4j.models.shared.dependency.ClassFilterMutant;
import org.adligo.tests4j.models.shared.dependency.ClassReferences;
import org.adligo.tests4j.models.shared.dependency.ClassReferencesMutant;
import org.adligo.tests4j.models.shared.dependency.I_ClassDependencies;
import org.adligo.tests4j.models.shared.dependency.I_ClassFilter;
import org.adligo.tests4j.models.shared.dependency.I_ClassReferences;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;
import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.map.MapInstrConstants;
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
	private I_ClassFilter basicClassFilter;
	private Map<String, I_ClassReferences> refMap = new HashMap<String,I_ClassReferences>();
	
	public ClassReferencesDiscovery(I_CachedClassBytesClassLoader pClassLoader,
			I_Tests4J_Log pLog,  I_DiscoveryMemory dc) {
		classLoader = pClassLoader;
		log = pLog;
		discoveryMemory = dc;
		basicClassFilter = dc.getBasicClassFilter();
		cv = new ReferenceTrackingClassVisitor(Opcodes.ASM5, log);
		cv.setInstrumentClassFilter(dc);
		
		cv.setBasicClassFilter(dc.getBasicClassFilter());
	}
	
	public List<String> discoverAndLoad(Class<?> c) throws IOException, ClassNotFoundException {
		refMap.clear();
		Stack<String> recStack = new Stack<String>();
		loadReferences(c, c, recStack);
		List<String> refOrder = calcRefOrder(c);
		return refOrder;
	}

	
	/**
	 * This is a method which is recursed to.
	 * It load the class into the shared class loader and
	 * find references, put them in the refMap.
	 * 
	 * @param c
	 * @param referencingClass only for the log, the referencingClass
	 * @param recursionStack this includes any class names who's references are currently getting
	 * calculated by this method.  This is passed as a Stack of Strings so that classloading
	 * issues will not appear.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private I_ClassReferences loadReferences(Class<?> c, Class<?> referencingClass, Stack<String> recursionStack) 
		throws IOException, ClassNotFoundException {
		
		if (discoveryMemory.isFiltered(c)) {
			return null;
		}
		String className = c.getName();
		recursionStack.add(className);
		
		
		if ( !classLoader.hasCache(className)) {
			Class<?> realParent = c.getSuperclass();
			if (realParent != null) {
				//load parents first 
				String realParentName = realParent.getName();
				if (!Object.class.getName().equals(realParentName)) {
					if ( !recursionStack.contains(realParentName)) {
						if ( !classLoader.hasCache(realParentName)) {
							loadReferences(realParent, realParent.getSuperclass(),recursionStack);
						}
					}
				}
			}
			

			if (c.isInterface()) {
				Class<?>[] interfaces =  c.getInterfaces();
				for (int i = 0; i < interfaces.length; i++) {
					Class<?> face = interfaces[i];
					if ( !recursionStack.contains(face.getClass().getName())) {
						loadReferences(face, face.getSuperclass(), recursionStack);
					}
				}
			}
			if (log.isLogEnabled(ClassReferencesDiscovery.class)) {
				StringBuilder sb = new StringBuilder();
				sb.append("Loading ");
				if (referencingClass != null) {
					sb.append(" reference from ");
					sb.append(referencingClass.getName());
					sb.append(" to ");
				}
				sb.append(className);
				log.log(sb.toString());
			}
			String resourceName = ClassMethods.toResource(className);
			InputStream in = c.getResourceAsStream(resourceName);
			if (in == null) {
				log.log("Error loading class " + resourceName);
			} else {
				classLoader.addCache(in, className);
			}
		}
		return findAllRefs(c, recursionStack);
	}
	
	/**
	 * Do not use this method for recursion!
	 * you want loadReferences(Class<?> c, Class<?> parent, Stack<String> recursionStack);
	 * 
	 * This method finds references from this class to other classes (at a simple 1 teir level),
	 * and then calls doRecursion()
	 * 
	 * @param c
	 * @param recursionStack
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private I_ClassReferences findAllRefs(Class<?> c, Stack<String> recursionStack) throws IOException, ClassNotFoundException {
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
		
		ClassReferencesMutant crm = readReflectionReferences(c, asmRefs);
		
		doRecursion( crm, c, recursionStack);
		ClassReferences toRet = new ClassReferences(crm);
		refMap.put(className, toRet);
		recursionStack.remove(c);
		return toRet;
	}

	/**
	 * this should recurse up the references tree
	 * @param c
	 * @param classNames
	 * @param recursionStack
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	protected void doRecursion(ClassReferencesMutant oneTeir, Class<?> referencingClass,  Stack<String> recursionStack) throws ClassNotFoundException, IOException {
		List<String> currentNames = new ArrayList<String>(oneTeir.getReferences());

		for (String className: recursionStack) {
			currentNames.remove(className);
		}
		for (String name: currentNames) {
			I_ClassReferences refs = refMap.get(name);
			if (refs == null) {
					I_ClassDependencies cdeps = discoveryMemory.get(name);
					if (cdeps != null) {
						refs = new ClassReferences(cdeps);
					}
			}
			if (refs == null) {
				Class<?> referencedClass = Class.forName(name);
				if ( !Object.class.getName().equals(referencedClass.getName())) {
					refs = loadReferences(referencedClass, referencingClass, recursionStack);
				}
			}
			if (refs != null) {
				//it was completely calculated earlier on in the process
				Set<String> crefs  = refs.getReferences();
				for (String ref: crefs) {
					oneTeir.addReference(ref);
				}
			} else {
				oneTeir.addReference(name);
			}
		}
	}

	/**
	 * this reads the method parameter and return types
	 * for interfaces, and the super types for interfaces, and classes
	 * @param c
	 * @param asmRefs
	 * @param classNames
	 * @return
	 */
	protected ClassReferencesMutant readReflectionReferences(Class<?> c, I_ClassReferences asmRefs) 
		throws ClassNotFoundException, IOException {
		
		ClassReferencesMutant crm = new ClassReferencesMutant(asmRefs);
		
		Class<?> realParent = c.getSuperclass();
		if (realParent != null) {
			String realParentName = realParent.getName();
			//ok the parent should have been cached already, and have all of its refs calculated
			
			I_ClassReferences parentRefs = refMap.get(realParentName);
			if (parentRefs != null) {
				for (String pr: parentRefs.getReferences()) {
					crm.addReference(pr);
				}
			} else {
				crm.addReference(realParentName);
			}
		}
		if (c.isInterface()) {
			//add a self reference, so the counts don't get screwed up
			crm.addReference(c.getName());
			
			Class<?>[] interfaces = c.getInterfaces();
			for (int i = 0; i < interfaces.length; i++) {
				Class<?> face = interfaces[i];
				String interfaceName = face.getName();
				I_ClassReferences parentRefs = refMap.get(interfaceName);
				if (parentRefs != null) {
					for (String pr: parentRefs.getReferences()) {
						crm.addReference(pr);
					}
				}
			}
		}
		
		//add references from reflection, for abstract methods, with no byte code
		Method [] methods =  c.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			
			Method m = methods[i];
			if ( !MapInstrConstants.METHOD_NAME.equals(m.getName())) {
				Class<?> returnClazz =  m.getReturnType();
				addReflectionNames(returnClazz, c, crm);
				Class<?> [] exceptions = m.getExceptionTypes();
				for (int j = 0; j < exceptions.length; j++) {
					Class<?> e = exceptions[j];
					addReflectionNames(e, c, crm);
				}
				Class<?> [] params =  m.getParameterTypes();
				for (int j = 0; j < params.length; j++) {
					Class<?> p = params[j];
					addReflectionNames(p, c,  crm);
				}
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
			I_ClassReferences crs = e.getValue();
			Set<String> classNames = crs.getReferences();
			
			Integer count = refCounts.get(className);
			
			if (isNotClassOrInnerClass(className, topName)) {
				
				if (count == null) {
					count = 1;
				} else {
					count = count + 1;
				}
				refCounts.put(className, count);
				
				for (String name: classNames) {
					if (isNotClassOrInnerClass(name, className)) {
						count = refCounts.get(name);
						if (count == null) {
							count = 1;
						} else {
							count = count + 1;
						}
						refCounts.put(name, count);
					}
				}
			} else {
				for (String name: classNames) {
					if (isNotClassOrInnerClass(name, className)) {
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
		if (!toRet.contains(topName)) {
			toRet.add(topName);
		}
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
	protected void addReflectionNames( Class<?> clazz, Class<?> referencingClass, 
			ClassReferencesMutant classReferences) throws ClassNotFoundException, IOException {
		if (clazz != null) {
			//don't add arrays
			if (clazz.isArray()) {
				Class<?> type = clazz.getComponentType();
				if ( !basicClassFilter.isFiltered(type)) {
					classReferences.addReference(type.getName());
				}
			} else {
				if (  !basicClassFilter.isFiltered(clazz)) {
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

	public I_ClassFilter getPrimitiveClassFilter() {
		return cv.getBasicClassFilter();
	}

	public void setPrimitiveClassFilter(I_ClassFilter primitiveClassFilter) {
		cv.setBasicClassFilter(primitiveClassFilter);
	}
}
