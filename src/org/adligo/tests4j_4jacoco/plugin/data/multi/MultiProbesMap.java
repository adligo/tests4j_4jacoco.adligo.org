package org.adligo.tests4j_4jacoco.plugin.data.multi;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.adligo.tests4j.models.shared.system.I_Tests4J_Reporter;
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
	private ConcurrentHashMap<String,boolean[]> scopesToProbes = 
				new ConcurrentHashMap<String, boolean[]>();
	private ConcurrentMapValueAvailableNotifier<String, boolean[]> scopesBlock = 
				new ConcurrentMapValueAvailableNotifier<String, boolean[]>(scopesToProbes);
	private I_CoverageRecoderStates states;
	private String clazzCovered;
	private int probeCount;
	private static I_Tests4J_Reporter REPORTER;
	private static ConcurrentHashMap<String, Throwable> CREATION_LOCATIONS = new ConcurrentHashMap<String, Throwable>();
	
	public MultiProbesMap(I_CoverageRecoderStates pStates, String pClazzToCover, int pProbeCount) {
		states = pStates;
		if (pStates == null) {
			throw new NullPointerException("pStates can't be null!");
		}
		clazzCovered = pClazzToCover;
		if (pClazzToCover == null) {
			throw new NullPointerException("pClazzToCover can't be null!");
		}
		probeCount = pProbeCount;
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
		List<String> activeScopes = states.getCurrentRecordingScopes();
		if (REPORTER != null) {
			if (value) {
				if (REPORTER.isLogEnabled(MultiProbesMap.class)) {
					if (clazzCovered.contains("AssertType")) {
						REPORTER.log("setting probe " + key + " for class " + 
								clazzCovered + " scope \n\t" + activeScopes);
					}
				}
			}
		}
		Iterator<String> it = activeScopes.iterator();
		while (it.hasNext()) {
			String scope = it.next();
			boolean [] probes = scopesToProbes.get(scope);
			if (probes == null) {
				if (!scopesBlock.containsKey(scope)) {
					scopesBlock.put(scope, new I_ValueCreator<boolean[]>() {
						
						@Override
						public boolean[] create() {
							return getFalseProbes();
						}
					});
					probes = scopesToProbes.get(scope);
					if (probes == null) {
						//block until available
						scopesBlock.await(scope);
						probes = scopesToProbes.get(scope);
					}
					
				} 
			}
			if (value) {
				if (probes == null) {
					probes = scopesToProbes.get(scope);
				}
				if (key < probeCount) {
					if (REPORTER != null) {
						if (REPORTER.isLogEnabled(MultiProbesMap.class)) {
							if (clazzCovered.contains("AssertType")) {
								
								if ("org.adligo.tests4j_tests.models.shared.asserts.common.AssertTypeTrial".equals(scope)) {
									REPORTER.log("setting probe " + key + "/" + probeCount + " for class " + clazzCovered + " scope " + scope);
								}
							}
						}
					}
					probes[key] = value;
					toRet = true;
				}
			}
		}
		return toRet;
	}

	private boolean[] getFalseProbes() {
		
		if (REPORTER != null) {
			if (REPORTER.isLogEnabled(MultiProbesMap.class)) {
				Throwable trace = new Throwable("Tracking where false probes are created \n" +
						"classCovered=" + clazzCovered );
				String p = clazzCovered.replaceAll("/", ".");
				CREATION_LOCATIONS.put(p, trace);
			}
		}
		boolean[] probes;
		probes = new boolean[probeCount];
		for (int i = 0; i < probes.length; i++) {
			probes[i] = false;
		}
		return probes;
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
	
	public void releaseRecording(String scope) {
		scopesToProbes.remove(scope);
	}

	public String getClazzCovered() {
		return clazzCovered;
	}
	
	public boolean[] getProbes(String scope) {
		boolean[] probes = scopesToProbes.get(scope);
		if (probes == null) {
			probes = getFalseProbes();
		}
		return probes;
	}
	
	public String toString() {
		return "MultiProbesMap [classCovered=" + clazzCovered +
				", scopesToProbes=" + scopesToProbes + "]";
	}

	public static I_Tests4J_Reporter getREPORTER() {
		return REPORTER;
	}

	public synchronized static void setREPORTER(I_Tests4J_Reporter rEPORTER) {
		REPORTER = rEPORTER;
	}
	
	public static void flushLocations(String clazzCovered) {
		Throwable t = CREATION_LOCATIONS.get(clazzCovered);
		if (t != null) {
			REPORTER.onError(t);
		}
	}
}
