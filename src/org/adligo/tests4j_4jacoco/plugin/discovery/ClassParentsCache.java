package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.util.concurrent.ConcurrentHashMap;

import org.adligo.tests4j.models.shared.dependency.I_ClassParentsCache;
import org.adligo.tests4j.models.shared.dependency.I_ClassParentsLocal;
import org.adligo.tests4j.shared.common.I_CacheControl;

public class ClassParentsCache implements I_ClassParentsCache {
	private ConcurrentHashMap<String, I_ClassParentsLocal> parentsCache = new ConcurrentHashMap<String, I_ClassParentsLocal>();

	public ClassParentsCache(I_CacheControl control) {
		control.addClearRunnable(new Runnable() {
			
			@Override
			public void run() {
				parentsCache.clear();
			}
		});
	}
	@Override
	public void putParentsIfAbsent(I_ClassParentsLocal p) {
		parentsCache.put(p.getName(), p);
	}

	@Override
	public I_ClassParentsLocal getParents(String name) {
		return parentsCache.get(name);
	}
	
}
