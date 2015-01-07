package org.adligo.tests4j_4jacoco.plugin.data.multi;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * This class cascades the probe states from one thread to another,<br/>
 * this is because some code for a particular class<br/>
 * can not be re-executed.   For instance consider the following;<br/>
 * <br/>
 * public static final List<String> foo = getList();<br/>
 * <br/>
 * private List<String> getList() {<br/>
 *    return Collections.unmodifiableList(new ArrayList());<br/>
 * }<br/>
 * <br/>
 * The method getList() is only executed once per class instance <br/>
 * (where a class instance is a class in a class loader).  This<br/>
 * class just saves the information that methods like getList()<br/>
 * were executed.<br/>
 * <br/>
 * @author scott
 *
 */
public class CascadingProbeMap implements Map<Integer, Boolean> {
	public static final String METHOD_NOT_IMPLEMENTED = "Method not implemented.";
  private boolean [] backed;
	
	public CascadingProbeMap(boolean [] p) {
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
	  if (backed.length == 0) {
	    return true;
	  } 
	  return false;
	}

	@Override
	public boolean containsKey(Object key) {
		throw new IllegalStateException(METHOD_NOT_IMPLEMENTED);
	}

	@Override
	public boolean containsValue(Object value) {
		throw new IllegalStateException(METHOD_NOT_IMPLEMENTED);
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
	  if (backed[key]) {
	    //only allows changing to true.
	    return false;
	  }
	  if (value == true) {
  		backed[(Integer) key] = value;
  		return true;
	  }
	  return false;
	}

	@Override
	public Boolean remove(Object key) {
		throw new IllegalStateException(METHOD_NOT_IMPLEMENTED);
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends Boolean> m) {
		throw new IllegalStateException(METHOD_NOT_IMPLEMENTED);	}

	@Override
	public void clear() {
	  throw new IllegalStateException(METHOD_NOT_IMPLEMENTED);
	}

	@Override
	public Set<Integer> keySet() {
		throw new IllegalStateException(METHOD_NOT_IMPLEMENTED);
	}

	@Override
	public Collection<Boolean> values() {
		throw new IllegalStateException(METHOD_NOT_IMPLEMENTED);
	}

	@Override
	public Set<java.util.Map.Entry<Integer, Boolean>> entrySet() {
		throw new IllegalStateException(METHOD_NOT_IMPLEMENTED);
	}

}
