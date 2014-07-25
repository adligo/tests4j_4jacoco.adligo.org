package org.adligo.tests4j_4jacoco.plugin.data.multi;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class CascadingProbeMap implements Map<Integer, Boolean> {
	private boolean [] initalProbes;
	private boolean [] backed;
	
	public CascadingProbeMap(boolean [] p) {
		initalProbes = new boolean[p.length];
		for (int i = 0; i < p.length; i++) {
			initalProbes[i] = p[i];
		}
		backed = new boolean[p.length];
		for (int i = 0; i < p.length; i++) {
			backed[i] = p[i];
		}
	}
	
	@Override
	public int size() {
		return backed.length;
	}

	@Override
	public boolean isEmpty() {
		throw new IllegalStateException("method not implemented");
	}

	@Override
	public boolean containsKey(Object key) {
		throw new IllegalStateException("method not implemented");
	}

	@Override
	public boolean containsValue(Object value) {
		throw new IllegalStateException("method not implemented");
	}

	public boolean[] get() {
		return backed;
	}
	
	@Override
	public Boolean get(Object key) {
		return backed[(Integer) key];
	}

	@Override
	public Boolean put(Integer key, Boolean value) {
		backed[(Integer) key] = value;
		return true;
	}

	@Override
	public Boolean remove(Object key) {
		throw new IllegalStateException("method not implemented");
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends Boolean> m) {
		throw new IllegalStateException("method not implemented");	}

	@Override
	public void clear() {
		for (int i = 0; i < initalProbes.length; i++) {
			backed[i] = initalProbes[i];
		}
	}

	@Override
	public Set<Integer> keySet() {
		throw new IllegalStateException("method not implemented");
	}

	@Override
	public Collection<Boolean> values() {
		throw new IllegalStateException("method not implemented");
	}

	@Override
	public Set<java.util.Map.Entry<Integer, Boolean>> entrySet() {
		throw new IllegalStateException("method not implemented");
	}

}
