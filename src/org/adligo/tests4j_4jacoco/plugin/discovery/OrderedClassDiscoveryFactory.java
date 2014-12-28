package org.adligo.tests4j_4jacoco.plugin.discovery;

import org.adligo.tests4j.models.shared.association.I_ClassAssociationsCache;
import org.adligo.tests4j.models.shared.association.I_ClassParentsCache;
import org.adligo.tests4j.run.helpers.I_CachedClassBytesClassLoader;
import org.adligo.tests4j.run.helpers.I_ClassFilter;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;
import org.adligo.tests4j_4jacoco.plugin.common.I_CoveragePluginMemory;
import org.adligo.tests4j_4jacoco.plugin.common.I_OrderedClassDiscovery;
import org.adligo.tests4j_4jacoco.plugin.common.I_OrderedClassDiscoveryFactory;
import org.objectweb.asm.Opcodes;

public class OrderedClassDiscoveryFactory implements I_OrderedClassDiscoveryFactory {

	@Override
	public I_OrderedClassDiscovery create(I_CoveragePluginMemory memory) {
		OrderedClassDiscovery ocd = new OrderedClassDiscovery();
		I_ClassAssociationsCache dependencyCache = memory.getDependencyCache();
		I_Tests4J_Log log = memory.getLog();
		I_ClassFilter basicClassFilter = memory.getBasicClassFilter();
		I_ClassFilter classFilter = memory.getClassFilter();
		I_CachedClassBytesClassLoader classLoaderCache = memory.getCachedClassLoader();
		I_ClassParentsCache parentsCache = memory.getParentsCache();
		
		ocd.setCache(dependencyCache);
		ocd.setLog(log);
		ocd.setClassFilter(classFilter);
		
		ClassParentsDiscovery classParentsDiscovery = new ClassParentsDiscovery();
		classParentsDiscovery.setCache(parentsCache);
		classParentsDiscovery.setClassFilter(classFilter);
		classParentsDiscovery.setClassLoader(classLoaderCache);
		classParentsDiscovery.setLog(log);
		
		InitialDependenciesDiscovery inital = new InitialDependenciesDiscovery();
		inital.setBasicClassFilter(basicClassFilter);
		inital.setCache(memory.getInitialDependencyCache());
		inital.setClassFilter(classFilter);
		inital.setClassLoader(classLoaderCache);
		inital.setClassParentsDiscovery(classParentsDiscovery);
		inital.setClassVisitor(new ReferenceTrackingClassVisitor(Opcodes.ASM5, log));
		inital.setLog(log);
		
		FullDependenciesDiscovery fullDependenciesDiscovery = new FullDependenciesDiscovery();
		fullDependenciesDiscovery.setCache(memory.getFullDependencyCache());
		fullDependenciesDiscovery.setLog(log);
		fullDependenciesDiscovery.setClassFilter(classFilter);
		fullDependenciesDiscovery.setInitialDependenciesDiscovery(inital);
		
		ocd.setFullDependenciesDiscovery(fullDependenciesDiscovery);
		
		CircularDependenciesDiscovery circleDependencyDiscovery = new CircularDependenciesDiscovery();
		circleDependencyDiscovery.setCache(memory.getDependencyCache());
		circleDependencyDiscovery.setFullDependenciesDiscovery(fullDependenciesDiscovery);
		circleDependencyDiscovery.setClassFilter(classFilter);
		circleDependencyDiscovery.setLog(log);
		
		ocd.setCircularDependenciesDiscovery(circleDependencyDiscovery);
		return ocd;
	}

}
