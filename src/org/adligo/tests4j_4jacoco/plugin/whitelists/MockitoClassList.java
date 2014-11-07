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
public class MockitoClassList extends BaseClassList {
	
	private static Set<String> getSharedClassWhitelist() {
		Set<String> toRet = new HashSet<String>();
	
		toRet.add("org.mockito.Matchers");
	  toRet.add("org.mockito.Mockito");
	  toRet.add("org.mockito.MockingDetails");
	  toRet.add("org.mockito.MockSettings");
	  toRet.add("org.mockito.invocation.InvocationOnMock");
	  toRet.add("org.mockito.stubbing.Answer");
	  toRet.add("org.mockito.stubbing.Stubber");
	  toRet.add("org.mockito.stubbing.OngoingStubbing");
	  
	  
	  toRet.add("org.mockito.cglib.proxy.Callback");
	  toRet.add("org.mockito.cglib.proxy.Factory");
	  toRet.add("org.mockito.cglib.proxy.NoOp");
	  toRet.add("org.mockito.cglib.proxy.MethodProxy");
	  
	  toRet.add("org.mockito.cglib.proxy.MethodInterceptor");
	  toRet.add("org.mockito.cglib.core.ReflectUtils");
	  
	  
	  return Collections.unmodifiableSet(toRet);
	}

	public MockitoClassList() {
	  this(new ClassesDelegate());
	}
	
	public MockitoClassList(I_Classes classes) {
    super(getSharedClassWhitelist(), classes);
  }
}
