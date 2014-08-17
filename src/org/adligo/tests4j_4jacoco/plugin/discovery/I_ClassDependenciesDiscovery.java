package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.io.IOException;

import org.adligo.tests4j.models.shared.dependency.I_ClassDependenciesLocal;

public interface I_ClassDependenciesDiscovery {

	public abstract I_ClassDependenciesLocal findOrLoad(Class<?> c)
			throws IOException, ClassNotFoundException;

}