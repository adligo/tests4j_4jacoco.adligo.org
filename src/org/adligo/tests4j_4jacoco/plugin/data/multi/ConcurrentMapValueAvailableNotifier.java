package org.adligo.tests4j_4jacoco.plugin.data.multi;

import java.util.concurrent.ConcurrentMap;

public class ConcurrentMapValueAvailableNotifier<K,V> {
	private ConcurrentMap<K,V> entries;
	
	public ConcurrentMapValueAvailableNotifier(ConcurrentMap<K,V> pEntries) {
		entries = pEntries;
	}
	
	/**
	 * @param o
	 * @return if the put was successful return the KeyedAtomicBoolean<T>
	 *  null otherwise 
	 */
	public synchronized boolean put(K o, I_ValueCreator<V> v) {
		if (!entries.containsKey(o)) {
			entries.put(o, v.create());
			notifyAll();
			return true;
		}
		return false;
	}
	
	
	public boolean containsKey(K o) {
		return entries.containsKey(o);
	}
	
	
	public synchronized void await(K o) {
		while (!entries.containsKey(o)) {
			try {
				wait(25);
			} catch (InterruptedException e) {
				//do nothing
			}
		}
		while (entries.get(o) != null) {
			try {
				wait(25);
			} catch (InterruptedException e) {
				//do nothing
			}
		}
	}
}
