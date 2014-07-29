package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.adligo.tests4j.models.shared.common.ClassMethods;
import org.adligo.tests4j.models.shared.common.StringMethods;
import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;

public class ClassReferencesMutant {
	private Set<String> classNames = new HashSet<String>();
	private Set<String> ignoredPackageNames = Collections.singleton("java.");
	private I_Tests4J_Log log;
	
	public void onLocalVariableClassName(String classTypeName) {
		if (!StringMethods.isEmpty(classTypeName)) {
			if (classTypeName.length() >= 3) {
				if (classTypeName.indexOf("[") == 0) {
					//its an array, but we want the type of the array
					classTypeName = classTypeName.substring(1, classTypeName.length());
				} 
				String className = ClassMethods.fromTypeDescription(classTypeName);
				if (!StringMethods.isEmpty(className)) {
					if (className.indexOf(".") != -1 && className.length() >= 2) {
						boolean ignored = false;
						if (ignoredPackageNames != null) {
							for (String ignoredPackage: ignoredPackageNames) {
								if (className.indexOf(ignoredPackage) == 0) {
									ignored = true;
									break;
								}
							}
						}
						if (!ignored) {
							classNames.add(className);
						}
					}
				}
			}
		}
	}
	
	public void setIgnoredPackageNames(Set<String> ignoredPackageNames) {
		this.ignoredPackageNames = ignoredPackageNames;
	}

	public Set<String> getClassNames() {
		return classNames;
	}
	
	public void clearClassNames() {
		classNames.clear();
	}

	public Set<String> getIgnoredPackageNames() {
		return ignoredPackageNames;
	}

	public I_Tests4J_Log getLog() {
		return log;
	}
}
