package org.adligo.tests4j_4jacoco.plugin.data.multi;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.adligo.tests4j_4jacoco.plugin.data.common.I_CoverageRecoderStates;

/**
 * a wrapper around probe data for different recorders.
 * mutates the boolean [][] passed in with put and remove only.
 * clear removes the reference to the probeData, for quicker 
 * garbage collection.
 * 
 * @author scott
 *
 */
public class MultiProbesMap implements Map<Integer, Boolean>{
	
	/**
	 * each entry in the list pertains to a different recorder
	 */
	private boolean[] probes;
	private ThreadLocal<boolean []> localProbes = new ThreadLocal<boolean[]>();
	
	/**
	 * The thread that created this instance
	 * which is used to determine which boolean [] of
	 * probes to use 
	 * 1) the probes member variable (for the thread that created this instance)
	 * 2) the localProbes ThreadLocal (for the other threads)
	 * 
	 */
	private Thread creationThread = Thread.currentThread();
	private String clazzCovered;
	private int probeCount;
	
	public MultiProbesMap(String pClazzToCover, int pProbeCount) {
		
		clazzCovered = pClazzToCover;
		if (pClazzToCover == null) {
			throw new NullPointerException("pClazzToCover can't be null!");
		}
		probeCount = pProbeCount;
		probes = getEmptyProbes();
	}

	@Override
	public int size() {
		throw new IllegalStateException("Method not implemented");
	}

	@Override
	public boolean isEmpty() {
		throw new IllegalStateException("Method not implemented");
	}

	@Override
	public boolean containsKey(Object key) {
		throw new IllegalStateException("Method not implemented");
	}

	@Override
	public boolean containsValue(Object value) {
		throw new IllegalStateException("Method not implemented");
	}

	@Override
	public Boolean get(Object key) {
		throw new IllegalStateException("Method not implemented");
	}

	@Override
	public  Boolean put(Integer key, Boolean value) {
		if (key == null || value == null) {
			return false;
		}
		int keyInt = key.intValue();
		if (keyInt < 0) {
			return false;
		}
		boolean toRet = false;
		
		if (value) {
			if (key < probeCount) {
				probes[key] = value;
				if ( !creationThread.equals(Thread.currentThread())) {
					if (localProbes.get() == null) {
						boolean [] local = getEmptyProbes();
						local[key] = value;
						localProbes.set(local);
					}
				}
				toRet = true;
			}
		}
		return toRet;
	}

	private boolean[] getEmptyProbes() {
		boolean[] toRet = new boolean[probeCount];
		for (int i = 0; i < toRet.length; i++) {
			toRet[i] = false;
		}
		return toRet;
	}

	@Override
	public Boolean remove(Object key) {
		throw new IllegalStateException("Method not implemented");
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends Boolean> m) {
		throw new IllegalStateException("Method not implemented");
	}

	@Override
	public void clear() {
		throw new IllegalStateException("Method not implemented");
	}

	@Override
	public Set<Integer> keySet() {
		throw new IllegalStateException("Method not implemented");
	}

	@Override
	public Collection<Boolean> values() {
		throw new IllegalStateException("Method not implemented");
	}

	@Override
	public Set<Entry<Integer, Boolean>> entrySet() {
		throw new IllegalStateException("Method not implemented");
	}
	
	public void releaseRecording() {
		/*
		if (creationThread.equals(Thread.currentThread())) {
			probes = null;
		}
		*/
		localProbes.set(null);
	}

	public String getClazzCovered() {
		return clazzCovered;
	}
	
	public boolean[] getProbes() {
		boolean[] toRet = localProbes.get();
		if (toRet == null) {
			toRet = probes;
		}
		if (toRet == null) {
			return getEmptyProbes();
		}
		return toRet;
	}
	
	public String toString() {
		return "MultiProbesMap [classCovered=" + clazzCovered +
				", scopesToProbes=" + probes + ", localProbes=" + localProbes.get() + "]";
	}

}
