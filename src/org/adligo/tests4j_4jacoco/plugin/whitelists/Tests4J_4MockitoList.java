package org.adligo.tests4j_4jacoco.plugin.whitelists;

import org.adligo.tests4j.run.common.ClassesDelegate;
import org.adligo.tests4j.run.common.I_Classes;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * this provides a way to load classes (mostly interfaces) in the default class loader
 * so that they are the same in child class loaders.
 * 
 * @author scott
 *
 */
public class Tests4J_4MockitoList extends BaseClassList {
	
	private static Set<String> getSharedClassWhitelist() {
		Set<String> toRet = new HashSet<String>();
	
	  toRet.add("org.adligo.tests4j_4mockito.I_ReturnFactory");
	  
	  toRet.add("org.adligo.tests4j_4mockito.MockitoApiTrial");
	  toRet.add("org.adligo.tests4j_4mockito.MockitoSourceFileTrial");
	  toRet.add("org.adligo.tests4j_4mockito.MockitoUseCaseTrial");
	  
	  return Collections.unmodifiableSet(toRet);
	}

  public Set<String> getNonInstrumentedPackages() {
    Set<String> names = new HashSet<String>();
    names.add("org.adligo.tests4j_4mockito.");
    names.add("org.adligo.tests4j.");
    return names;
  }
  
	public Tests4J_4MockitoList() {
	  this(new ClassesDelegate());
	}
	
	public Tests4J_4MockitoList(I_Classes classes) {
    super(getSharedClassWhitelist(), classes);
  }
}
