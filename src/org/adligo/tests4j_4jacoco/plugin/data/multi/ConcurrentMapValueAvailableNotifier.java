package org.adligo.tests4j_4jacoco.plugin.data.multi;

import org.adligo.tests4j.run.common.I_Notifier;
import org.adligo.tests4j.run.common.NotifierDelegate;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class ConcurrentMapValueAvailableNotifier<K,V> {
	private ConcurrentMap<K,V> entries_;
	private I_Notifier notifier_;
	
	public ConcurrentMapValueAvailableNotifier(ConcurrentMap<K,V> pEntries) {
		this(pEntries, null);
	}
	
	/**
	 * 
	 * @param pEntries
	 * @param notifier generally only used for test stubbing
	 * for wait/notify calls
	 */
	public ConcurrentMapValueAvailableNotifier(ConcurrentMap<K,V> pEntries, I_Notifier notifier) {
    entries_ = pEntries;
    if (notifier != null) {
      notifier_ = notifier;
    } else {
      notifier_ = new NotifierDelegate(this);
    }
  }
	/**
	 * @param o
	 * @return if the put was successful return the KeyedAtomicBoolean<T>
	 *  null otherwise 
	 */
	public synchronized boolean putIfAbsent(K o, V v) {
    if (!entries_.containsKey(o)) {
      entries_.putIfAbsent(o, v);
      notifier_.notifyAllDelegate();
      return true;
    }
    return false;
  }
	/**
	 * check if the backing map 
	 * contains the key, 
	 * with no blocking/synchronization
	 * @param o
	 * @return
	 */
	public boolean containsKey(K o) {
		return entries_.containsKey(o);
	}
	
	/**
	 * get the value, 
   * with no blocking/synchronization
	 * @param o
	 * @return
	 */
	public V get(K o) {
    return entries_.get(o);
  }
	/**
	 * get the value from the backing
	 * map, if the backing map is null
	 * then wait for it to be put in the 
	 * map and return the value put in the map.
	 * @param o
	 * @return
	 */
	public V obtain(K o) {
	  if (!containsKey(o)) {
	    await(o);
	  }
    return get(o);
  }
	
	public synchronized void await(K o) {
		while (!entries_.containsKey(o)) {
			try {
			  notifier_.waitDelegate(25);
			} catch (InterruptedException e) {
			  //http://www.ibm.com/developerworks/library/j-jtp05236/
			  Thread.currentThread().interrupt();
			}
		}
		while (entries_.get(o) != null) {
			try {
			  notifier_.waitDelegate(25);
			} catch (InterruptedException e) {
			  //http://www.ibm.com/developerworks/library/j-jtp05236/
			  Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * return the backing maps entry set
	 * no synchronization
	 * @return
	 */
	public Set<Entry<K,V>> entrySet() {
	  return entries_.entrySet();
	}
	
  public I_Notifier getNotifier() {
    return notifier_;
  }
}
