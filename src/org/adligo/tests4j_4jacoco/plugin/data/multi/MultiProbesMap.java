package org.adligo.tests4j_4jacoco.plugin.data.multi;

import org.adligo.tests4j.models.shared.coverage.I_ClassProbes;
import org.adligo.tests4j.models.shared.coverage.I_Probes;
import org.adligo.tests4j.run.common.I_InitalValueFactory;
import org.adligo.tests4j.run.common.ThreadGroupFilter;
import org.adligo.tests4j.run.common.ThreadGroupLocal;
import org.adligo.tests4j.run.helpers.ThreadLogMessageBuilder;
import org.adligo.tests4j.run.memory.Tests4J_ThreadFactory;
import org.adligo.tests4j.shared.common.ClassMethods;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;
import org.adligo.tests4j_4jacoco.plugin.common.I_Runtime;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

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
	
	public static final String METHOD_NOT_IMPLEMENTED = "Method not implemented";
  /**
	 * each entry in the list pertains to a different recorder
	 */
	private final boolean[] probes_;
	private final ThreadGroupLocal<CascadingProbeMap> threadGroupProbes_;
	private final ThreadGroupFilter threadGroupFilter_;
	private final String clazzCovered_;
	private final int probeCount_;
	private final long classId_;
	private final I_Tests4J_Log logger_;
	private final I_Runtime runtime_;
	private final MultiContext ctx_;
	
	/**
	 * change to have I_FilteredRecorderMemory
	 * @param pClazzToCover
	 * @param pProbeCount
	 * @param pReporter
	 */
	public MultiProbesMap(I_ClassProbes cp, MultiContext ctx) {
	  ctx_ = ctx;
	  runtime_ = ctx_.getRuntime();
	  classId_ = cp.getClassId();
		logger_ = ctx_.getLog();
		clazzCovered_ = cp.getClassName();
		if (clazzCovered_ == null) {
			throw new NullPointerException("pClazzToCover can't be null!");
		}
		I_Probes pc = cp.getProbes();
		probeCount_ = pc.size();
		
		probes_ = getEmptyProbes(probeCount_);
		threadGroupFilter_ = new ThreadGroupFilter(Tests4J_ThreadFactory.TRIAL_THREAD_GROUP_PREFIX); 
		threadGroupProbes_ = 
				new ThreadGroupLocal<CascadingProbeMap>(
				    threadGroupFilter_,
						new I_InitalValueFactory<CascadingProbeMap>() {
							volatile AtomicBoolean set = new AtomicBoolean(false);
							volatile CascadingProbeMap first;
							@Override
							public synchronized CascadingProbeMap createNew() {
								if (!set.get()) {
									//a certain amout of code coverage
									//can occur on the main thread, before the trial
									//run, this passes the current code coverage 
									// (recorded from the main thread)
									// down to the trial and test threads
								  if (logger_.isLogEnabled(MultiProbesMap.class)) {
								    logger_.log("" + this + " " + ThreadLogMessageBuilder.getThreadWithGroupNameForLog() +
						            "\n created new CascadingProbeMap for " + clazzCovered_);
						      }
									first =  new CascadingProbeMap(probes_);
									return first;
								} else {
									//pass the first thread local, to the other
									//thread locals, since it is from a setup thread;
									return new CascadingProbeMap(first.get());
								}
								
							}
					
						});
	}

	@Override
	public int size() {
		throw new IllegalStateException(METHOD_NOT_IMPLEMENTED);
	}

	@Override
	public boolean isEmpty() {
		throw new IllegalStateException(METHOD_NOT_IMPLEMENTED);
	}

	@Override
	public boolean containsKey(Object key) {
		throw new IllegalStateException(METHOD_NOT_IMPLEMENTED);
	}

	@Override
	public boolean containsValue(Object value) {
		throw new IllegalStateException(METHOD_NOT_IMPLEMENTED);
	}

	@Override
	public Boolean get(Object key) {
		throw new IllegalStateException(METHOD_NOT_IMPLEMENTED);
	}

	@SuppressWarnings("boxing")
  @Override
	public Boolean put(Integer key, Boolean value) {
		
		if (key == null || value == null) {
			return false;
		}
		int keyInt = key.intValue();
		if (keyInt < 0) {
			return false;
		}
		boolean toRet = false;
		
		if (value) {
			if (key < probeCount_) {
			  boolean filtered = false;
			  String threadGroupName = threadGroupFilter_.getThreadGroupNameMatchingFilter();
			  if (threadGroupName != null) {
  			  String filter = runtime_.getThreadGroupFilter(threadGroupName);
  			  if (filter != null) {
  			    if (clazzCovered_.indexOf(filter) != 0) {
  			      filtered = true;
  			    }
  			  }
			  }
			  if (!filtered) {
  				CascadingProbeMap local = threadGroupProbes_.getValue();
  				if (local != null) {
    				if (!probes_[key] || !local.get(key)) {
    					if (logger_.isLogEnabled(MultiProbesMap.class)) {
    							StringBuilder sb = new StringBuilder();
    							if (!probes_[key]) {
    								sb.append(" probes");
    							}
    							if (!local.get(key)) {
    								sb.append(" threadGroupLocalProbes");
    							}
    							logger_.log("" + super.toString() + " " + ThreadLogMessageBuilder.getThreadWithGroupNameForLog() +
    									"\n set the probes " + key + "/" + probeCount_ + " " + value + " on " + clazzCovered_ + "\n" +
    									sb.toString());
    					}
    				}
    				local.put(key, value);
    				runtime_.putClassCovered(threadGroupName, classId_);
  				}
				}
				probes_[key] = value;
				
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
		throw new IllegalStateException(METHOD_NOT_IMPLEMENTED);
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends Boolean> m) {
		throw new IllegalStateException(METHOD_NOT_IMPLEMENTED);
	}

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
	public Set<Entry<Integer, Boolean>> entrySet() {
		throw new IllegalStateException(METHOD_NOT_IMPLEMENTED);
	}
	
	public String getClazzCovered() {
		return clazzCovered_;
	}
	
	public boolean[] getProbes() {
		return probes_;
	}
	
	public boolean[] getThreadGroupProbes() {
		CascadingProbeMap threadGroupLocalProbesMap = threadGroupProbes_.getValue();
		boolean [] threadGroupLocalProbes = null;
		if (threadGroupLocalProbesMap != null) {
			threadGroupLocalProbes = threadGroupLocalProbesMap.get();
		}
		if (threadGroupLocalProbes == null) {
			threadGroupLocalProbes = getEmptyProbes(probeCount_);
			if (logger_.isLogEnabled(MultiProbesMap.class)) {
				logger_.log("" + super.toString() + " " + ThreadLogMessageBuilder.getThreadWithGroupNameForLog() +
						" is getting empty probes for class \n" +
						clazzCovered_ + "\n" +
						threadGroupLocalProbes);
			}
		}
		if (logger_.isLogEnabled(MultiProbesMap.class)) {
			logger_.log("" + super.toString() + " " + ThreadLogMessageBuilder.getThreadWithGroupNameForLog() +
					" is getting threadGropupLocalProbes for class \n" +
					clazzCovered_ + "\n threadGroupLocalProbes" +
					probesToString(threadGroupLocalProbes));
		}
		return threadGroupLocalProbes;
	}
	
	public String toString() {
		CascadingProbeMap threadGroupLocalProbesMap = threadGroupProbes_.getValue();
		boolean [] threadGroupLocalProbes = null;
		if (threadGroupLocalProbesMap != null) {
			threadGroupLocalProbes = threadGroupLocalProbesMap.get();
		}
		return "MultiProbesMap [classCovered=" + clazzCovered_ +
				", probes=" + probesToString(probes_) + ",threadGroupLocalProbes=" + 
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
			if (p[i]) {
				sbProbes.append("t");
			} else {
				sbProbes.append("f");
			}
		}
		sbProbes.append("]");
		return sbProbes.toString();
	}

  public long getClassId() {
    return classId_;
  }
}
