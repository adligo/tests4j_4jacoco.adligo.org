package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.adligo.tests4j.models.shared.common.ClassMethods;
import org.adligo.tests4j.models.shared.dependency.ClassReferences;
import org.adligo.tests4j.models.shared.dependency.ClassReferencesMutant;
import org.adligo.tests4j.models.shared.dependency.DependencyMutant;
import org.adligo.tests4j.models.shared.dependency.I_ClassFilter;
import org.adligo.tests4j.models.shared.dependency.I_ClassReferences;
import org.adligo.tests4j.models.shared.dependency.I_Dependency;
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
	private List<String> initalRefsToIdentify = new ArrayList<String>();
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
		if (log.isLogEnabled(ClassReferencesDiscovery.class)) {
			log.log("ClassReferencesDiscovery.discoverAndLoad " + c.getName());
		}
		refMap.clear();
		I_ClassReferences crefs = loadInitalReferences(c, c);
		//ok only for the primary class initial references should be loaded 
		Set<String> refs = crefs.getReferences();
		for (String ref: refs) {
			if ( !discoveryMemory.isFiltered(ref)) {
				initalRefsToIdentify.add(ref);
			}
		}
		initalRefsToIdentify.removeAll(refMap.keySet());
		while (initalRefsToIdentify.size() >= 1) {
			String next = initalRefsToIdentify.get(0);
			Class<?> nextClass = Class.forName(next);
			I_ClassReferences delRefs = loadInitalReferences(nextClass, c);
			Set<String> delRs = delRefs.getReferences();
			for (String ref: delRs) {
				//check local caches first
				if (!initalRefsToIdentify.contains(ref)) {
					if ( !discoveryMemory.isFiltered(ref)) {
						initalRefsToIdentify.add(ref);
					}
				}
			}
			initalRefsToIdentify.removeAll(refMap.keySet());
		}
		//ok all of the initial references should be loaded in the refMap
		rebuildRefMapWithAllReferences();
		//ok all references are loaded
		calcCircles();
		addToRefsCache();
		List<String> refOrder = calcRefOrder(c);
		return refOrder;
	}

	
	/**
	 * This loads the initial one tier references
	 * into the refMap.
	 * 
	 * @param c
	 * @param referencingClass only for the log, the referencingClass
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private I_ClassReferences loadInitalReferences(Class<?> c, Class<?> referencingClass) 
		throws IOException, ClassNotFoundException {
		
		if (discoveryMemory.isFiltered(c)) {
			return null;
		}
		String className = c.getName();
		
		I_ClassReferences crefs =  discoveryMemory.getReferences(className);
		if (crefs != null) {
			refMap.put(className, crefs);
			return crefs;
		}
		if ( !classLoader.hasCache(className)) {
			Class<?>[] interfaces =  c.getInterfaces();
			for (int i = 0; i < interfaces.length; i++) {
				Class<?> face = interfaces[i];
				loadInitalReferences(face, face.getSuperclass());
			}
			
			Class<?> realParent = c.getSuperclass();
			if (realParent != null) {
				//load parents first 
				String realParentName = realParent.getName();
				if (!Object.class.getName().equals(realParentName)) {
					loadInitalReferences(realParent, realParent.getSuperclass());
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
		return findInitalRefs(c);
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
	private I_ClassReferences findInitalRefs(Class<?> c) throws IOException, ClassNotFoundException {
		String className = c.getName();
		
		//check cache, to keep ASM visitor calls down because they are expensive
		I_ClassReferences crefs =  discoveryMemory.getReferences(className);
		if (crefs != null) {
			refMap.put(className, crefs);
			return crefs;
		}
		
		//add references from ASM, byte code inspection
		InputStream in = classLoader.getCachedBytesStream(className);
		ClassReader classReader=new ClassReader(in);
		cv.reset();
		classReader.accept(cv, 0);
		I_ClassReferences asmRefs = cv.getClassReferences();
		
		ClassReferencesMutant crm = readReflectionReferences(c, asmRefs);
		
		ClassReferences toRet = new ClassReferences(crm);
		refMap.put(className, toRet);
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
	protected void addRefAndRecurse(ClassReferencesMutant target, String reference,  Set<String> done)  {
		
		target.addReference(reference);
		
		I_ClassReferences initalRef =  refMap.get(reference);
		if (initalRef == null) {
			//it is a filtered class
			return;
		}
		done.add(reference);
		List<String> currentNames = new ArrayList<String>(initalRef.getReferences());

		currentNames.removeAll(done);
		
		for (String name: currentNames) {
			addRefAndRecurse(target, name, done);
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
		
		//add the interfaces
		Class<?>[] interfaces = c.getInterfaces();
		for (int i = 0; i < interfaces.length; i++) {
			Class<?> face = interfaces[i];
			String interfaceName = face.getName();
			crm.addReference(interfaceName);
		}
		
		//add the super classes
		Class<?> realParent = c.getSuperclass();
		if (realParent != null) {
			String realParentName = realParent.getName();
			//ok the parent should have been cached already, and have all of its refs calculated
			crm.addReference(realParentName);
		}
		if (c.isInterface()) {
			//add a self reference, so the counts don't get screwed up
			crm.addReference(c.getName());
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
		Set<I_Dependency> deps = toDependencies(topName);
		List<String> toRet = new ArrayList<String>();
		for (I_Dependency dep: deps) {
			toRet.add(dep.getClassName());
		}
		boolean adding = true;
		int count = 1;
		while (adding) {
			String inName = topName + "$" + count;
			if (refMap.containsKey(inName)) {
				if (!toRet.contains(inName)) {
					toRet.add(inName);
				}
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

	public Set<I_Dependency> toDependencies(String topName) {
		Map<String,DependencyMutant> refCounts = new HashMap<String,DependencyMutant>();
		
		Set<Entry<String, I_ClassReferences>> refs =  refMap.entrySet();
		for (Entry<String,I_ClassReferences> e: refs) {
			String className = e.getKey();
			I_ClassReferences crs = e.getValue();
			Set<String> classNames = crs.getReferences();
			
			DependencyMutant count = null;
			if (isNotClassOrInnerClass(className, topName)) {
				
				
				for (String name: classNames) {
					if (isNotClassOrInnerClass(name, className)) {
						count = refCounts.get(name);
						if (count == null) {
							count = new DependencyMutant();
							count.setClassName(name);
							count.addReference();
						} else {
							count.addReference();
						}
						refCounts.put(name, count);
					}
				}
			} else {
				for (String name: classNames) {
					if (isNotClassOrInnerClass(name, className)) {
						count = refCounts.get(name);
						if (count == null) {
							count = new DependencyMutant();
							count.setClassName(name);
							count.addReference();
						} else {
							count.addReference();
						}
						refCounts.put(name, count);
					}
				}
			}
		}
		
		Set<I_Dependency> deps = new TreeSet<I_Dependency>(refCounts.values());
		return deps;
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
	private void rebuildRefMapWithAllReferences() {
		Map<String, I_ClassReferences> newRefMap = new HashMap<String, I_ClassReferences>();
		Set<Entry<String, I_ClassReferences>> entries = refMap.entrySet();
		for (Entry<String, I_ClassReferences> e: entries) {
			ClassReferencesMutant crm = new ClassReferencesMutant();
			crm.setClassName(e.getKey());
			I_ClassReferences val = e.getValue();
			Set<String> refs = val.getReferences();
			Set<String> done = new HashSet<String>();
			for (String ref: refs) {
				addRefAndRecurse(crm, ref, done);
			}
			newRefMap.put(e.getKey(), new ClassReferences(crm));
		}
		refMap = newRefMap;
	}
	private void calcCircles() {
		Map<String, I_ClassReferences> newRefMap = new HashMap<String, I_ClassReferences>();
		Set<Entry<String, I_ClassReferences>> entries = refMap.entrySet();
		for (Entry<String, I_ClassReferences> e: entries) {
			String name = e.getKey();
			I_ClassReferences cr =  e.getValue();
			ClassReferencesMutant crm = new ClassReferencesMutant(cr);
			Set<String> refs =  cr.getReferences();
			for (String ref: refs) {
				if (!name.equals(ref)) {
					I_ClassReferences refRef = refMap.get(ref);
					if (refRef != null) {
						Set<String> refRefRef = refRef.getReferences();
						if (refRefRef.contains(name)) {
							crm.addCircularReferences(refRef.getClassName());
						}
					}
				}
			}
			
			newRefMap.put(name, new ClassReferences(crm));
		}
		refMap = newRefMap;
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
	
	/**
	 * note this is separated out so that
	 * the circular dependencies are calculated
	 * which require all down stream references to be 
	 * calculated first
	 */
	private void addToRefsCache() {
		Set<Entry<String, I_ClassReferences>> entries = refMap.entrySet();
		for (Entry<String, I_ClassReferences> e: entries) {
			discoveryMemory.putReferencesIfAbsent(e.getValue());
		}
	}
}
