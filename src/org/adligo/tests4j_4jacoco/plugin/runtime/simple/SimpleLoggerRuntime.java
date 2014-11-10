package org.adligo.tests4j_4jacoco.plugin.runtime.simple;

import org.adligo.tests4j.models.shared.coverage.I_SourceFileCoverageBrief;
import org.adligo.tests4j.models.shared.coverage.SourceFileCoverageBriefMutant;
import org.adligo.tests4j_4jacoco.plugin.common.I_LoggerDataAccessorFactory;
import org.adligo.tests4j_4jacoco.plugin.common.I_Runtime;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStore;
import org.adligo.tests4j_4jacoco.plugin.data.common.I_ProbesDataStoreAdaptor;
import org.jacoco.core.internal.instr.InstrSupport;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleLoggerRuntime implements I_Runtime {
	private final I_LoggerDataAccessorFactory factory;

	private final Logger logger_;
	private final String key_;
	private final Handler handler_;
	/** access to the runtime data */
	protected I_ProbesDataStoreAdaptor data_;
	private ConcurrentHashMap<String,String> threadGroupsToProbeModificationFilters_ =
    new ConcurrentHashMap<String,String>();
	private ConcurrentHashMap<String,ConcurrentSkipListSet<Long>> threadGroupClassIds_ =
    new ConcurrentHashMap<String,ConcurrentSkipListSet<Long>>();
	
	/**
	 * Creates a new runtime.
	 */
	public SimpleLoggerRuntime(I_LoggerDataAccessorFactory pFactory) {
		super();
		this.factory = pFactory;
		key_ = factory.getKey();
		this.logger_ = configureLogger();
		this.handler_ = new RuntimeLoggingHandler(this);
	}

	private Logger configureLogger() {
		final Logger l = Logger.getLogger(factory.getChannel());
		l.setUseParentHandlers(false);
		l.setLevel(Level.ALL);
		return l;
	}

	@Override
	public void startup() throws SecurityException {
		if (data_ == null) {
			throw new IllegalStateException("Null data at startup.");
		}
		data_.startup();
		this.logger_.addHandler(handler_);
	}
	
	@Override
	public void shutdown() {
		this.logger_.removeHandler(handler_);
	}
	
	public void disconnect(final Class<?> type) throws Exception {
		if (!type.isInterface()) {
			final Field dataField = type
					.getDeclaredField(InstrSupport.DATAFIELD_NAME);
			dataField.setAccessible(true);
			dataField.set(null, null);
		}
	}

	public String getKey() {
		return key_;
	}

	public Logger getLogger() {
		return logger_;
	}

	public Handler getHandler() {
		return handler_;
	}

	public I_ProbesDataStoreAdaptor getData() {
		return data_;
	}

	public void setup(I_ProbesDataStoreAdaptor p) {
		data_ = p;
	}

	@Override
	public I_ProbesDataStore end(boolean root) {
		return data_.getRecordedProbes(root);
	}

  @Override
  public void putThreadGroupFilter(String threadGroupName, String javaProbeFilter) {
    threadGroupsToProbeModificationFilters_.put(threadGroupName, javaProbeFilter);
  }

  @Override
  public String getThreadGroupFilter(String threadGroupName) {
    return threadGroupsToProbeModificationFilters_.get(threadGroupName);
  }

  @Override
  public Set<Long> getClassesCovered(String threadGroupName) {
    return getClassIds(threadGroupName);
  }

  @SuppressWarnings("boxing")
  @Override
  public void putClassCovered(String threadGroupName, long classId) {
    ConcurrentSkipListSet<Long> set = getClassIds(threadGroupName);
    set.add(classId);
  }

  private ConcurrentSkipListSet<Long> getClassIds(String threadGroupName) {
    ConcurrentSkipListSet<Long> toRet =  threadGroupClassIds_.get(threadGroupName);
    if (toRet == null) {
      return createClassIds(threadGroupName);
    }
    return toRet;
  }
  
  private synchronized ConcurrentSkipListSet<Long> createClassIds(String threadGroupName) {
    ConcurrentSkipListSet<Long> toRet = new ConcurrentSkipListSet<Long>();
    threadGroupClassIds_.putIfAbsent(threadGroupName, toRet);
    return threadGroupClassIds_.get(threadGroupName);
  }

  @Override
  public void clearClassesCovered(String threadGroupName) {
    threadGroupClassIds_.put(threadGroupName, new ConcurrentSkipListSet<Long>());
  }

  @Override
  public I_SourceFileCoverageBrief getSourceFileCoverage(String threadGroupName, String sourceFileClassName) {
    ConcurrentSkipListSet<Long> classIds = threadGroupClassIds_.get(threadGroupName);
    Iterator<Long> it = null;
    if (classIds != null) {
      it = classIds.iterator();
    }
    return data_.getSourceFileProbes(threadGroupName, sourceFileClassName, it);
  }
}
