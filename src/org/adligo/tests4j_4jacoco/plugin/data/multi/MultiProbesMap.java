package org.adligo.tests4j_4jacoco.plugin.data.multi;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.adligo.tests4j.models.shared.system.I_Tests4J_Reporter;
import org.adligo.tests4j.run.helpers.Tests4J_ThreadFactory;
import org.adligo.tests4j.run.helpers.ThreadLogMessageBuilder;

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
	private final boolean[] probes;
	private final ThreadGroupLocal<SynchronizedProbeMap> threadGroupProbes;
	/*
	private InheritableThreadLocal<ConcurrentHashMap<Integer, Boolean>> localProbes = 
			new InheritableThreadLocal<ConcurrentHashMap<Integer, Boolean>>();
	*/
	private final String clazzCovered;
	private final int probeCount;
	private final I_Tests4J_Reporter reporter;
	
	public MultiProbesMap(String pClazzToCover, final int pProbeCount, final I_Tests4J_Reporter pReporter) {
		reporter = pReporter;
		clazzCovered = pClazzToCover;
		if (pClazzToCover == null) {
			throw new NullPointerException("pClazzToCover can't be null!");
		}
		probeCount = pProbeCount;
		probes = getEmptyProbes(probeCount);
		threadGroupProbes = 
				new ThreadGroupLocal<SynchronizedProbeMap>(Tests4J_ThreadFactory.TRIAL_THREAD_NAME,
						new I_InitalValueFactory<SynchronizedProbeMap>() {

							@Override
							public SynchronizedProbeMap createNew() {
								return new SynchronizedProbeMap(getEmptyProbes(probeCount));
							}
					
						}, reporter, pClazzToCover);
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
	public Boolean put(Integer key, Boolean value) {
		if (reporter.isLogEnabled(MultiProbesMap.class)) {

			reporter.log("" + super.toString() + " " + ThreadLogMessageBuilder.getThreadWithGroupNameForLog() +
					"\n set the probes " + key + "/" + probeCount + " " + value + " on " + clazzCovered + "\n" +
					threadGroupProbes.get());
		}
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
				
				
				SynchronizedProbeMap local = threadGroupProbes.getValue();
				local.put(key, value);
				toRet = true;
			}
		}
		return toRet;
	}

	private static boolean[] getEmptyProbes(int pProbeCount) {
		boolean[] toRet = new boolean[pProbeCount];
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
		/*
		MultiProbesMap delegate = threadGroupLocalDelegate.get();
		if (delegate != null) {
			delegate.probes = getEmptyProbes(probeCount);
		}
		*/
		if (reporter.isLogEnabled(MultiProbesMap.class)) {
			reporter.log("" + super.toString() + " " + ThreadLogMessageBuilder.getThreadWithGroupNameForLog() +
					" is clearing probes \n" +
					toString());
		}
		threadGroupProbes.getValue().clear();
	}

	public String getClazzCovered() {
		return clazzCovered;
	}
	
	public boolean[] getProbes() {
		return probes;
	}
	
	public boolean[] getThreadGroupProbes() {
		SynchronizedProbeMap map = threadGroupProbes.getValue();
		boolean [] toRet = null;
		if (map != null) {
			toRet = map.get();
		}
		if (toRet == null) {
			toRet = getEmptyProbes(probeCount);
			if (reporter.isLogEnabled(MultiProbesMap.class)) {
				reporter.log("" + super.toString() + " " + ThreadLogMessageBuilder.getThreadWithGroupNameForLog() +
						" is getting empty probes for class \n" +
						clazzCovered + "\n" +
						map);
			}
		}
		return toRet;
	}
	
	public String toString() {
		SynchronizedProbeMap threadGroupLocalProbesMap = threadGroupProbes.getValue();
		boolean [] threadGroupLocalProbes = null;
		if (threadGroupLocalProbesMap != null) {
			threadGroupLocalProbes = threadGroupLocalProbesMap.get();
		}
		return "MultiProbesMap [classCovered=" + clazzCovered +
				", probes=" + probesToString(probes) + ",threadGroupLocalProbes=" + 
				probesToString(threadGroupLocalProbes)+ "]";
	}

	private String probesToString(boolean [] p) {
		if (p == null) {
			return null;
		}
		StringBuilder sbProbes = new StringBuilder();
		sbProbes.append("[");
		for (int i = 0; i < p.length; i++) {
			if (i != 0) {
				sbProbes.append(",");
			}
			if (probes[i]) {
				sbProbes.append("t");
			} else {
				sbProbes.append("f");
			}
		}
		sbProbes.append("]");
		return sbProbes.toString();
	}
}
