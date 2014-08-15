package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import org.adligo.tests4j.models.shared.common.ClassMethods;
import org.adligo.tests4j.models.shared.dependency.ClassReferencesLocal;
import org.adligo.tests4j.models.shared.dependency.ClassReferencesLocalMutant;
import org.adligo.tests4j.models.shared.dependency.I_ClassFilter;
import org.adligo.tests4j.models.shared.dependency.I_ClassParentsLocal;
import org.adligo.tests4j.models.shared.dependency.I_ClassReferencesCache;
import org.adligo.tests4j.models.shared.dependency.I_ClassReferencesLocal;
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
 * This class only loads the initial references, 
 * which are the parent references and direct
 * references from the byte code or reflection
 * to other classes. Initial references 
 * do not include delegate references ie;
 * A -> B -> C
 * but NOT 
 * A -> C
 * 
 * so C is a delegate reference,
 * and B is a initial reference.
 * 
 * 
 * @author scott
 *
 */
public class ClassInitialReferencesDiscovery {
	private I_CachedClassBytesClassLoader classLoader;
	private I_Tests4J_Log log;
	private ReferenceTrackingClassVisitor cv;
	private I_ClassReferencesCache initalRefsCache;
	private I_ClassFilter basicClassFilter;
	private ClassParentsDiscovery cpd;
	
	public ClassInitialReferencesDiscovery(I_CachedClassBytesClassLoader pClassLoader,
			I_Tests4J_Log pLog,  I_DiscoveryMemory dc) {
		classLoader = pClassLoader;
		log = pLog;
		initalRefsCache = dc.getInitialReferencesCache();
		basicClassFilter = dc.getBasicClassFilter();
		cv = new ReferenceTrackingClassVisitor(Opcodes.ASM5, log);
		cpd = new ClassParentsDiscovery(pClassLoader, pLog, dc);
		
	}
	
	public I_ClassReferencesLocal findOrLoad(Class<?> c) throws IOException, ClassNotFoundException {
		String className = c.getName();
		I_ClassReferencesLocal refs = initalRefsCache.getReferences(className);
		if (refs != null) {
			return refs;
		}
		if (log.isLogEnabled(ClassInitialReferencesDiscovery.class)) {
			log.log(this.getClass().getSimpleName() + ".findOrLoad ... " + c.getName());
		}
		
		return loadInitalReferences(c);
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
	private I_ClassReferencesLocal loadInitalReferences(Class<?> c) 
		throws IOException, ClassNotFoundException {
		
		
		I_ClassParentsLocal cps = cpd.findOrLoad(c);
		ClassReferencesLocal result = findInitalRefs(c, cps);
		initalRefsCache.putReferencesIfAbsent(result);
		return result;
	}
	
	/**
	 * This method finds references from this class to other classes (at a simple 1 teir level),
	 * and then calls doRecursion()
	 * 
	 * @param c
	 * @param recursionStack
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private ClassReferencesLocal findInitalRefs(Class<?> c, I_ClassParentsLocal parents) 
			throws IOException, ClassNotFoundException {
		
		String className = c.getName();
		
		ClassReferencesLocalMutant crm = new ClassReferencesLocalMutant(parents);
		//add references from ASM, byte code inspection
		InputStream in = classLoader.getCachedBytesStream(className);
		ClassReader classReader=new ClassReader(in);
		cv.reset();
		classReader.accept(cv, 0);
		Set<String> asmRefs = cv.getClassReferences();
		for (String asmRef: asmRefs ) {
			if (log.isLogEnabled(ClassInitialReferencesDiscovery.class)) {
				log.log(this.getClass().getSimpleName() + ".findInitalRefs reading asmRef " + asmRef);
			}
			String asmRefName = ClassMethods.fromTypeDescription(asmRef);
			if ( !basicClassFilter.isFiltered(asmRefName)) {
				Class<?> asmClass = Class.forName(asmRefName);
				I_ClassParentsLocal cps = cpd.findOrLoad(asmClass);
				crm.addReference(cps);
			}
		}
		
		readReflectionReferences(c, crm);
		
		return new ClassReferencesLocal(crm);
	}

	/**
	 * this reads the method parameter and return types
	 * for interfaces, and the super types for interfaces, and classes
	 * @param c
	 * @param asmRefs
	 * @param classNames
	 * @return
	 */
	protected void readReflectionReferences(Class<?> c, ClassReferencesLocalMutant crm) 
		throws ClassNotFoundException, IOException {
		
		
		if (c.isInterface()) {
			//add a self reference, so the counts don't get screwed up
			I_ClassParentsLocal cps =  cpd.findOrLoad(c);
			crm.addReference(cps);
		}	
		Annotation [] annotations =  c.getAnnotations();
		for (int i = 0; i < annotations.length; i++) {
			Annotation anno = annotations[i];
			Class<? extends Annotation> annoClass = anno.annotationType();
			addReflectionNames(annoClass, c, crm);
			
			Method [] methods = annoClass.getDeclaredMethods();
			for (int j = 0; j < methods.length; j++) {
				Method f = methods[j];
				Class<?> fieldType =  f.getReturnType();
				addReflectionNames(fieldType, c, crm);
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
	}
	
	protected void addReflectionNames( Class<?> clazz, Class<?> referencingClass, 
			ClassReferencesLocalMutant classReferences) throws ClassNotFoundException, IOException {
		if (clazz != null) {
			//don't add arrays
			if (clazz.isArray()) {
				Class<?> type = clazz.getComponentType();
				if ( !basicClassFilter.isFiltered(type)) {
					I_ClassParentsLocal cps = cpd.findOrLoad(type);
					classReferences.addReference(cps);
				}
			} else {
				if (  !basicClassFilter.isFiltered(clazz)) {
					I_ClassParentsLocal cps = cpd.findOrLoad(clazz);
					classReferences.addReference(cps);
				}
			}
		}
	}
}
