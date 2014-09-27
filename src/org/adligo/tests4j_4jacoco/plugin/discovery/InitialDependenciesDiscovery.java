package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.adligo.tests4j.models.shared.dependency.ClassDependenciesLocal;
import org.adligo.tests4j.models.shared.dependency.ClassDependenciesLocalMutant;
import org.adligo.tests4j.models.shared.dependency.I_ClassDependenciesCache;
import org.adligo.tests4j.models.shared.dependency.I_ClassDependenciesLocal;
import org.adligo.tests4j.models.shared.dependency.I_ClassFilter;
import org.adligo.tests4j.models.shared.dependency.I_ClassParentsLocal;
import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;
import org.adligo.tests4j.shared.asserts.dependency.ClassAttributes;
import org.adligo.tests4j.shared.asserts.dependency.ClassAttributesMutant;
import org.adligo.tests4j.shared.asserts.dependency.FieldSignature;
import org.adligo.tests4j.shared.asserts.dependency.I_FieldSignature;
import org.adligo.tests4j.shared.asserts.dependency.I_MethodSignature;
import org.adligo.tests4j.shared.asserts.dependency.MethodSignature;
import org.adligo.tests4j.shared.common.ClassMethods;
import org.adligo.tests4j.shared.common.StringMethods;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;
import org.adligo.tests4j_4jacoco.plugin.instrumentation.map.MapInstrConstants;
import org.objectweb.asm.ClassReader;

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
public class InitialDependenciesDiscovery implements I_ClassDependenciesDiscovery {
	private I_CachedClassBytesClassLoader classLoader;
	private I_Tests4J_Log log;
	private AbstractReferenceTrackingClassVisitor classVisitor;
	private I_ClassDependenciesCache cache;
	private I_ClassFilter basicClassFilter;
	private I_ClassParentsDiscovery classParentsDiscovery;
	private I_ClassFilter classFilter;
	
	public InitialDependenciesDiscovery(){}
	
	/**
	 * @diagram_sync with DiscoveryOverview.seq on 8/17/2014
	 * @see org.adligo.tests4j_4jacoco.plugin.discovery.I_ClassDependenciesDiscovery#findOrLoad(java.lang.Class)
	 */
	@Override
	public I_ClassDependenciesLocal findOrLoad(Class<?> c) throws IOException, ClassNotFoundException {
		String className = c.getName();
		I_ClassDependenciesLocal refs = cache.getDependencies(className);
		if (refs != null) {
			return refs;
		}
		if (log.isLogEnabled(InitialDependenciesDiscovery.class)) {
			log.log(this.getClass().getSimpleName() + ".findOrLoad ... " + c.getName());
		}
		//@diagram_sync with DiscoveryOverview.seq on 8/17/2014
		return loadInitalReferences(c);
	}

	/**
	 * @diagram_sync with DiscoveryOverview.seq on 8/17/2014
	 * 
	 * @param c
	 * @param referencingClass only for the log, the referencingClass
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private I_ClassDependenciesLocal loadInitalReferences(Class<?> c) 
		throws IOException, ClassNotFoundException {
		
		//@diagram_sync with DiscoveryOverview.seq on 8/17/2014
		I_ClassParentsLocal cps = classParentsDiscovery.findOrLoad(c);
		
		
		if (classFilter.isFiltered(c)) {
			return new ClassDependenciesLocal(classParentsDiscovery.findOrLoad(c));
		}
		String className = c.getName();
		
		//@diagram_sync with DiscoveryOverview.seq on 8/17/2014
		ClassDependenciesLocalMutant crm = new ClassDependenciesLocalMutant(cps);
		//add references from ASM, byte code inspection
		InputStream in = classLoader.getCachedBytesStream(className);
		ClassReader classReader=new ClassReader(in);
		classVisitor.reset();
		classReader.accept(classVisitor, 0);
		//@diagram_sync with DiscoveryOverview.seq on 8/17/2014
		List<ClassAttributes> asmRefs = classVisitor.getClassCalls();
		addCalls(crm, asmRefs);
		//@diagram_sync with DiscoveryOverview.seq on 9/2/2014
		readReflectionReferences(c, crm);
		
		ClassDependenciesLocal result = new ClassDependenciesLocal(crm);
		
		cache.putDependenciesIfAbsent(result);
		return result;
	}

	public void addCalls(ClassDependenciesLocalMutant crm,
			List<ClassAttributes> asmRefs) throws ClassNotFoundException,
			IOException {
		for (ClassAttributes asmRef: asmRefs ) {
			if (log.isLogEnabled(InitialDependenciesDiscovery.class)) {
				log.log(this.getClass().getSimpleName() + ".findInitalRefs reading asmRef " + asmRef);
			}
			String asmClassTypeName = asmRef.getName();
			String javaRefName = ClassMethods.fromTypeDescription(asmClassTypeName);
			
			if ( !basicClassFilter.isFiltered(javaRefName)) {
				addTypes(crm, javaRefName);
			}
			Set<I_FieldSignature> fields =  asmRef.getFields();
			ClassAttributesMutant cmm = new ClassAttributesMutant();
			cmm.setName(javaRefName);
			
			for (I_FieldSignature field: fields) {
				String fieldClassName = ClassMethods.fromTypeDescription(field.getClassName());
				if ( !basicClassFilter.isFiltered(fieldClassName)) {
					addTypes(crm, fieldClassName);
				}
				cmm.addField(new FieldSignature(field.getName(), fieldClassName));
			}
			
			Set<I_MethodSignature> methods =  asmRef.getMethods();
			
			for (I_MethodSignature meth: methods) {
				String [] javaParamNames = new String[meth.getParameters()];
				
				for (int i = 0; i < meth.getParameters(); i++) {
					String param = meth.getParameterClassName(i);
					String methodParamClassName = ClassMethods.fromTypeDescription(param);
					javaParamNames[i] = methodParamClassName;
					
					if ( !basicClassFilter.isFiltered(methodParamClassName)) {
						addTypes(crm, methodParamClassName);
					}
				}
				String returnClassName = meth.getReturnClassName();
				if ( !StringMethods.isEmpty(returnClassName)) {
					returnClassName = ClassMethods.fromTypeDescription(returnClassName);
				
					if ( !basicClassFilter.isFiltered(returnClassName)) {
						addTypes(crm, returnClassName);
					}
				}
				cmm.addMethod(new MethodSignature(meth.getMethodName(), javaParamNames, returnClassName));
			}
			crm.addCall(new ClassAttributes(cmm));
		}
	}

	/**
	 * this adds the dependency for javaClassName to the crm parameter,
	 * if the javaClassName is an array it only adds the
	 * array type to the crm instance.
	 * 
	 * @param crm
	 * @param javaClassName
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public void addTypes(ClassDependenciesLocalMutant crm, String javaClassName)
			throws ClassNotFoundException, IOException {
		
		if (ClassMethods.isArray(javaClassName)) {
			String typeClassName = ClassMethods.getArrayType(javaClassName);
			Class<?> typeClass = Class.forName(typeClassName);
			I_ClassParentsLocal ps = classParentsDiscovery.findOrLoad(typeClass);
			crm.addDependency(ps);
		} else {
			Class<?> clazz = Class.forName(javaClassName);
			I_ClassParentsLocal ps = classParentsDiscovery.findOrLoad(clazz);
			crm.addDependency(ps);
		}
	}

	/**
	 * this reads the method parameter and return types
	 * for interfaces, and the super types for interfaces, and classes
	 * @diagram_sync with DiscoveryOverview.seq on 9/4/2014
	 * @param c
	 * @param asmRefs
	 * @param classNames
	 * @return
	 */
	protected void readReflectionReferences(Class<?> c, ClassDependenciesLocalMutant crm) 
		throws ClassNotFoundException, IOException {
		
		if (c.isInterface()) {
			//add a self reference, so the counts don't get screwed up
			I_ClassParentsLocal cps =  classParentsDiscovery.findOrLoad(c);
			crm.addDependency(cps);
		}	
		Annotation [] annotations =  c.getAnnotations();
		addAnnotations(c, crm, annotations);
		
		//add references from reflection, for abstract methods, with no byte code,
		//and get annotations
		Constructor<?>[] cons = c.getDeclaredConstructors();
		for (int i = 0; i < cons.length; i++) {
			
			Constructor<?> con = cons[i];
			Class<?> [] exceptions = con.getExceptionTypes();
			for (int j = 0; j < exceptions.length; j++) {
				Class<?> e = exceptions[j];
				addReflectionNames(e, c, crm);
			}
			Class<?> [] params =  con.getParameterTypes();
			for (int j = 0; j < params.length; j++) {
				Class<?> p = params[j];
				addReflectionNames(p, c,  crm);
			}
			annotations = con.getAnnotations();
			addAnnotations(c, crm, annotations);
		}

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
			annotations = m.getAnnotations();
			addAnnotations(c, crm, annotations);
		}
	}

	public void addAnnotations(Class<?> c, ClassDependenciesLocalMutant crm,
			Annotation[] annotations) throws ClassNotFoundException,
			IOException {
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
	}
	
	protected void addReflectionNames( Class<?> clazz, Class<?> referencingClass, 
			ClassDependenciesLocalMutant classReferences) throws ClassNotFoundException, IOException {
		if (clazz != null) {
			//don't add arrays
			if (clazz.isArray()) {
				Class<?> type = clazz.getComponentType();
				if ( !basicClassFilter.isFiltered(type)) {
					I_ClassParentsLocal cps = classParentsDiscovery.findOrLoad(type);
					classReferences.addDependency(cps);
				}
			} else {
				if (  !basicClassFilter.isFiltered(clazz)) {
					I_ClassParentsLocal cps = classParentsDiscovery.findOrLoad(clazz);
					classReferences.addDependency(cps);
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

	public AbstractReferenceTrackingClassVisitor getClassVisitor() {
		return classVisitor;
	}

	public I_ClassDependenciesCache getCache() {
		return cache;
	}

	public I_ClassFilter getBasicClassFilter() {
		return basicClassFilter;
	}

	public I_ClassParentsDiscovery getClassParentsDiscovery() {
		return classParentsDiscovery;
	}

	public I_ClassFilter getClassFilter() {
		return classFilter;
	}

	public void setClassLoader(I_CachedClassBytesClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public void setLog(I_Tests4J_Log log) {
		this.log = log;
	}

	public void setClassVisitor(AbstractReferenceTrackingClassVisitor classVisitor) {
		this.classVisitor = classVisitor;
	}

	public void setCache(I_ClassDependenciesCache cache) {
		this.cache = cache;
	}

	public void setBasicClassFilter(I_ClassFilter basicClassFilter) {
		this.basicClassFilter = basicClassFilter;
	}

	public void setClassParentsDiscovery(
			I_ClassParentsDiscovery classParentsDiscovery) {
		this.classParentsDiscovery = classParentsDiscovery;
	}

	public void setClassFilter(I_ClassFilter classFilter) {
		this.classFilter = classFilter;
	}
}
