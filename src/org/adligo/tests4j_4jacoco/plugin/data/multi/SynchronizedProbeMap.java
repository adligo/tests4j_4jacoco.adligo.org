package org.adligo.tests4j_4jacoco.plugin.data.multi;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class SynchronizedProbeMap implements Map<Integer, Boolean> {
	private boolean [] backed;
	
	public SynchronizedProbeMap(boolean [] p) {
		backed = p;
	}
	
	@Override
	public synchronized int size() {
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

	public synchronized boolean[] get() {
		return backed;
	}
	
	@Override
	public synchronized Boolean get(Object key) {
		return backed[(Integer) key];
	}

	@Override
	public synchronized Boolean put(Integer key, Boolean value) {
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
	public synchronized void clear() {
		for (int i = 0; i < backed.length; i++) {
			backed[i] = false;
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
