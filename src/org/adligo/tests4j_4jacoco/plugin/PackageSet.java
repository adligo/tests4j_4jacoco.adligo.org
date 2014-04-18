package org.adligo.tests4j_4jacoco.plugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class PackageSet {
	private Set<String> packages = new HashSet<String>();
	
	public void add(String p) {
		if (p != null) {
			if (!packages.contains(p)) {
				Iterator<String> pkgIt = packages.iterator();
				boolean replaced = false;
				while (pkgIt.hasNext()) {
					String pkg = pkgIt.next();
					if (pkg.contains(p)) {
						//we want the top most packages only
						pkgIt.remove();
						packages.add(p);
						replaced = true;
						break;
					} 
				}
				if (!replaced) {
					packages.add(p);
				}
			}
		}
	}
	
	public Set<String> get() {
		return Collections.unmodifiableSet(packages);
	}
}
