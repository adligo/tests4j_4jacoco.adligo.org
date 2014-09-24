package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.util.concurrent.ConcurrentHashMap;

import org.adligo.tests4j.models.shared.dependency.I_ClassDependenciesCache;
import org.adligo.tests4j.models.shared.dependency.I_ClassDependenciesLocal;
import org.adligo.tests4j.shared.common.I_CacheControl;

public class ClassDependenciesCache implements I_ClassDependenciesCache {
	private ConcurrentHashMap<String, I_ClassDependenciesLocal> refs  = new ConcurrentHashMap<String, I_ClassDependenciesLocal>();
	
	public ClassDependenciesCache() {
		
	}
	public ClassDependenciesCache(I_CacheControl control) {
		control.addClearRunnable(new Runnable() {
			
			@Override
			public void run() {
				refs.clear();
			}
		});
	}
	@Override
	public void putDependenciesIfAbsent(I_ClassDependenciesLocal p) {
		refs.putIfAbsent(p.getName(), p);
	}

	@Override
	public I_ClassDependenciesLocal getDependencies(String name) {
		return refs.get(name);
	}
}
