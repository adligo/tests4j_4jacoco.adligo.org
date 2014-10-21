package org.adligo.tests4j_4jacoco.plugin.common;

import java.util.List;

import org.adligo.tests4j.models.shared.association.I_ClassAssociationsLocal;

public interface I_OrderedClassDependencies {
	public I_ClassAssociationsLocal getClassDependencies();
	public List<String> getOrder();
}
