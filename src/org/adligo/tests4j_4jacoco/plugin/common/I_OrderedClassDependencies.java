package org.adligo.tests4j_4jacoco.plugin.common;

import java.util.List;
import org.adligo.tests4j.models.shared.dependency.I_ClassDependenciesLocal;

public interface I_OrderedClassDependencies {
	public I_ClassDependenciesLocal getClassDependencies();
	public List<String> getOrder();
}
