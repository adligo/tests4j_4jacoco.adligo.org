package org.adligo.tests4j_4jacoco.plugin.data.multi;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * this class cascades the probe states from one thread to another,
 * this is because some code for a particular class
 * can not be re-executed.   For instance consider the following;
 * 
 * public static final List<String> foo = getList();
 * 
 * private List<String> getList() {
 *    return Collections.unmodifiableList(new ArrayList());
 * }
 * 
 * The method getList() is only executed once per class instance 
 * (where a class instance is a class in a class loader).  This
 * class just saves the information that methods like getList()
 * were executed.
 * 
 * @author scott
 *
 */
public class CascadingProbeMap implements Map<Integer, Boolean> {
	private boolean [] initalProbes;
	private boolean [] backed;
	
	public CascadingProbeMap(int size) {
		initalProbes = new boolean[size];
	}
	
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
	
	@SuppressWarnings("boxing")
  @Override
	public Boolean get(Object key) {
		return backed[(Integer) key];
	}

	@SuppressWarnings("boxing")
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
	  throw new IllegalStateException("method not implemented");
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
