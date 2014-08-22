package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.util.List;

import org.adligo.tests4j.models.shared.dependency.I_ClassDependenciesLocal;
import org.adligo.tests4j_4jacoco.plugin.common.I_OrderedClassDependencies;

/**
 * a result class to pass around dependencies so 
 * I don't have to rely on the cache to get dependencies.
 * 
 * @author scott
 *
 */
public class OrderedClassDependencies implements I_OrderedClassDependencies {
	private I_ClassDependenciesLocal dependencies;
	private List<String> order;
	
	public OrderedClassDependencies (I_ClassDependenciesLocal depsIn, List<String> orderIn) {
		dependencies = depsIn;
		order = orderIn;
	}
	
	@Override
	public I_ClassDependenciesLocal getClassDependencies() {
		return dependencies;
	}

	@Override
	public List<String> getOrder() {
		return order;
	}

}
