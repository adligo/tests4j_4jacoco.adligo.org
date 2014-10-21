package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.util.concurrent.ConcurrentHashMap;

import org.adligo.tests4j.models.shared.association.I_ClassAssociationsCache;
import org.adligo.tests4j.models.shared.association.I_ClassAssociationsLocal;
import org.adligo.tests4j.shared.common.I_CacheControl;

public class ClassDependenciesCache implements I_ClassAssociationsCache {
	private ConcurrentHashMap<String, I_ClassAssociationsLocal> refs  = new ConcurrentHashMap<String, I_ClassAssociationsLocal>();
	
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
	public void putDependenciesIfAbsent(I_ClassAssociationsLocal p) {
		refs.putIfAbsent(p.getName(), p);
	}

	@Override
	public I_ClassAssociationsLocal getDependencies(String name) {
		return refs.get(name);
	}
}
