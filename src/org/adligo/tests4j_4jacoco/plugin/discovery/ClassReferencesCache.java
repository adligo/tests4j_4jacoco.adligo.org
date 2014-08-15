package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.adligo.tests4j.models.shared.dependency.I_ClassReferencesCache;
import org.adligo.tests4j.models.shared.dependency.I_ClassReferencesLocal;

public class ClassReferencesCache implements I_ClassReferencesCache {
	private Map<String, I_ClassReferencesLocal> refs  = new ConcurrentHashMap<String, I_ClassReferencesLocal>();
	
	@Override
	public void putReferencesIfAbsent(I_ClassReferencesLocal p) {
		refs.put(p.getName(), p);
	}

	@Override
	public I_ClassReferencesLocal getReferences(String name) {
		return refs.get(name);
	}
	
}
