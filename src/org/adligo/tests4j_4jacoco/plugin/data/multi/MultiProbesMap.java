package org.adligo.tests4j_4jacoco.plugin.data.multi;

import org.adligo.tests4j.models.shared.coverage.I_ClassProbes;
import org.adligo.tests4j.models.shared.coverage.I_Probes;
import org.adligo.tests4j.run.common.I_InitalValueFactory;
import org.adligo.tests4j.run.common.I_ThreadGroupFilter;
import org.adligo.tests4j.run.common.I_ThreadGroupLocal;
import org.adligo.tests4j.run.common.I_ThreadingFactory;
import org.adligo.tests4j.run.common.ThreadingFactory;
import org.adligo.tests4j.run.memory.Tests4J_ThreadFactory;
import org.adligo.tests4j.shared.i18n.I_Tests4J_Constants;
import org.adligo.tests4j.shared.i18n.I_Tests4J_LogMessages;
import org.adligo.tests4j.shared.i18n.I_Tests4J_ReportMessages;
import org.adligo.tests4j.shared.output.DefaultLog;
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
	
	public static final String THE_I_CLASS_PROBES_MUST_CONTAIN_THE_CLASS_NAME_OF_THE_COVERED_CLASS =
      "The I_ClassProbes must contain the class name of the covered class.";
  public static final String METHOD_NOT_IMPLEMENTED = "Method not implemented";
  private final I_Tests4J_Constants constants_;
  
  private static boolean[] getEmptyProbes(int pProbeCount) {
    boolean[] toRet = new boolean[pProbeCount];
    for (int i = 0; i < toRet.length; i++) {
      toRet[i] = false;
    }
    return toRet;
  }
  /** end static start instance */
  
  /**
	 * each entry in the list pertains to a different recorder
	 */
	private final boolean[] probes_;
	private final I_ThreadGroupLocal<CascadingProbeMap> threadGroupProbes_;
	private final I_ThreadGroupFilter threadGroupFilter_;
	private final String clazzCovered_;
	private final int probeCount_;
	private final long classId_;
	private final I_Tests4J_Log log_;
	private final I_Runtime runtime_;
	private final MultiContext ctx_;
	private final I_ThreadingFactory factory_;
	
	public MultiProbesMap(I_ClassProbes cp, MultiContext ctx,  I_ThreadingFactory factory) {
	  ctx_ = ctx;
	  constants_ = ctx_.getConstants();
	  if (constants_ == null) {
	    throw new NullPointerException();
	  }
	  if (factory == null) {
	    factory_ = ThreadingFactory.INSTANCE;
	  } else {
	    factory_ = factory;
	  }
	  runtime_ = ctx_.getRuntime();
    classId_ = cp.getClassId();
    log_ = ctx_.getLog();
    clazzCovered_ = cp.getClassName();
    if (clazzCovered_ == null) {
      throw new NullPointerException(THE_I_CLASS_PROBES_MUST_CONTAIN_THE_CLASS_NAME_OF_THE_COVERED_CLASS);
    }
    I_Probes pc = cp.getProbes();
    probeCount_ = pc.size();
    
    probes_ = getEmptyProbes(probeCount_);
    threadGroupFilter_ = factory_.createThreadGroupFilter(Tests4J_ThreadFactory.TRIAL_THREAD_GROUP_PREFIX); 
    threadGroupProbes_ = factory_.createThreadGroupLocal(threadGroupFilter_,createInitalValueFactory());
	}
	
	/**
	 * change to have I_FilteredRecorderMemory
	 * @param pClazzToCover
	 * @param pProbeCount
	 * @param pReporter
	 */
	public MultiProbesMap(I_ClassProbes cp, MultiContext ctx) {
	  this(cp, ctx, null);
	}
	

	@Override
	public int size() {
		return probes_.length;
	}

	@Override
	public boolean isEmpty() {
		if (probes_.length == 0) {
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
			  
			  CascadingProbeMap local = null;
			  if (!filtered) {
  				local = threadGroupProbes_.getValue();
				}
			  if (isLoggable(key, local)) {
          if (log_.isLogEnabled(MultiProbesMap.class)) {
              logPut(key, local);
          }
        }
			  if (local != null) {
          local.put(key, value);
          runtime_.putClassCovered(threadGroupName, classId_);
        }
				probes_[key] = value;
				
				toRet = true;
			}
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
	
	public String getClassCovered() {
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
		}
		if (log_.isLogEnabled(MultiProbesMap.class)) {
		  I_Tests4J_LogMessages messages = constants_.getLogMessages();
		  I_Tests4J_ReportMessages reportMessages = constants_.getReportMessages();
		  log_.log(DefaultLog.orderLine(constants_.isLeftToRight(), 
			    MultiProbesMap.class.getSimpleName(), " ", log_.getThreadWithGroupNameMessage()) + log_.getLineSeperator() + 
					DefaultLog.orderLine(constants_.isLeftToRight(),
					    reportMessages.getIndent(), messages.getIsGettingTheFollowingThreadGroupLocalProbes()) + 
					    log_.getLineSeperator() + 
			    DefaultLog.orderLine(constants_.isLeftToRight(),
              reportMessages.getIndent(),  probesToString(threadGroupLocalProbes)) + 
              log_.getLineSeperator() + 
					DefaultLog.orderLine(constants_.isLeftToRight(),
              reportMessages.getIndent(), messages.getForTheFollowingClass()) + 
					log_.getLineSeperator() + 
					DefaultLog.orderLine(constants_.isLeftToRight(),
              reportMessages.getIndent(), clazzCovered_));
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
  
  public I_ThreadingFactory getThreadingFactory() {
    return factory_;
  }
  
  private I_InitalValueFactory<CascadingProbeMap> createInitalValueFactory() {
    return new I_InitalValueFactory<CascadingProbeMap>() {
      volatile AtomicBoolean set = new AtomicBoolean(false);
      volatile CascadingProbeMap first;
      @Override
      public synchronized CascadingProbeMap createNew() {
        if (!set.get()) {
          //a certain amount of code coverage
          //can occur on the main thread, before the trial
          //run, this passes the current code coverage 
          // (recorded from the main thread)
          // down to the trial and test threads
          if (log_.isLogEnabled(MultiProbesMap.class)) {
            I_Tests4J_LogMessages messages = constants_.getLogMessages();
            I_Tests4J_ReportMessages reportMessages = constants_.getReportMessages();
            log_.log(DefaultLog.orderLine(constants_.isLeftToRight(),
                MultiProbesMap.class.getSimpleName(), " ",
                log_.getThreadWithGroupNameMessage()) + 
                log_.getLineSeperator() + 
                DefaultLog.orderLine(constants_.isLeftToRight(),
                    reportMessages.getIndent(), messages.getIsCreatingNewProbesForTheFollowingClass()) + 
                log_.getLineSeperator() + 
                DefaultLog.orderLine(constants_.isLeftToRight(),
                    reportMessages.getIndent(), clazzCovered_));
          }
          first =  new CascadingProbeMap(probes_);
          set.set(true);
          return first;
        } else {
          //pass the first thread local, to the other
          //thread locals, since it is from a setup thread;
          return new CascadingProbeMap(first.get());
        }
      }
    };
  }
  
  @SuppressWarnings("boxing")
  private void logPut(Integer key, CascadingProbeMap local) {
    StringBuilder sb = new StringBuilder();
    if (!probes_[key]) {
      sb.append("probes");
    }
    if (local != null) {
      if (!local.get(key)) {
        if (sb.length() >= 1) {
          sb.append(" ");
        }
        sb.append("threadGroupLocalProbes");
      }
    }
    int whichProbe = key + 1;
    I_Tests4J_LogMessages messages = constants_.getLogMessages();
    I_Tests4J_ReportMessages reportMessages = constants_.getReportMessages();
    log_.log(DefaultLog.orderLine(constants_.isLeftToRight(),MultiProbesMap.class.getSimpleName(), " ",
        log_.getThreadWithGroupNameMessage()) + log_.getLineSeperator() +
        DefaultLog.orderLine(constants_.isLeftToRight(),
            reportMessages.getIndent(),messages.getDetectedTheFollowingProbeHit()) + 
        log_.getLineSeperator() + 
        DefaultLog.orderLine(constants_.isLeftToRight(),
            reportMessages.getIndent(), clazzCovered_) + log_.getLineSeperator() +
        DefaultLog.orderLine(constants_.isLeftToRight(),
            reportMessages.getIndent(), sb.toString(), " ", whichProbe + "/" + probeCount_));
  }
  
  @SuppressWarnings("boxing")
  private boolean isLoggable(int probe, CascadingProbeMap local) {
    if (!probes_[probe]) {
      return true;
    }
    if (local != null) {
      if (!local.get(probe)) {
        return true;
      }
    }
    return false;
  }
}
