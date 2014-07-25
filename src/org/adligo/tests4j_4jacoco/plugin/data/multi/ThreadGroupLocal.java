package org.adligo.tests4j_4jacoco.plugin.data.multi;

import java.util.concurrent.ConcurrentHashMap;

import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;
import org.adligo.tests4j.run.helpers.ThreadLogMessageBuilder;

public class ThreadGroupLocal<T> extends ThreadLocal<Holder<T>>{
	private final ConcurrentHashMap<String, Holder<T>> threadGroupLocalMap = new ConcurrentHashMap<String, Holder<T>>();
	private final ThreadGroupFilter filter;
	private final I_InitalValueFactory<T> factory;
	private final I_Tests4J_Log reporter;
	private final String instrumentedClassName;
	
	public ThreadGroupLocal(String pFilterNamePart, I_InitalValueFactory<T> pFactory, 
			I_Tests4J_Log pReporter, String pInstrumentedClassName) {
		filter = new ThreadGroupFilter(pFilterNamePart);
		factory = pFactory;
		reporter = pReporter;
		instrumentedClassName = pInstrumentedClassName;
		//reporter.onError(new Exception("Created " + this));
	}
	
	@Override 
    protected Holder<T> initialValue() {
		String groupName = filter.getThreadGroupNameMatchingFilter();
		Holder<T> holder = threadGroupLocalMap.get(groupName);
		if (holder == null) {
			holder = new Holder<T>();
			if (reporter.isLogEnabled(ThreadGroupLocal.class)) {
				reporter.log("" + this + " " + ThreadLogMessageBuilder.getThreadWithGroupNameForLog() +
						"\n created new holder " + holder +
						"\n for class " + instrumentedClassName);
			}
			holder.setHeld(factory.createNew());
			threadGroupLocalMap.putIfAbsent(groupName,holder);
			holder = threadGroupLocalMap.get(groupName);
		}
		return holder;
    }
	
    public T getValue() { 
    	Holder<T> holder = super.get();
    	T toRet = holder.getHeld();
    	if (toRet == null) {
    		toRet = factory.createNew();
    		holder.setHeld(toRet);
    	}
    	return toRet;
    }
	
    
    public void clear() {
    	Holder<T> holder = super.get();
    	holder.setHeld(null);
    }
	
	
}
