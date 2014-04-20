package org.adligo.tests4j_4jacoco.plugin.data.map;

import java.util.concurrent.ConcurrentHashMap;

public class LineNumberMapMutant implements I_LineNumberMap {
	/**
	 * map the the instrumented line numbers back to the originals.
	 */
	private ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>>
		lineNumberMap = new ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>>();
	
	public LineNumberMapMutant() {}
	
	public synchronized void put(String clazzName, Integer instrumentedLineNumber, Integer sourceLineNumber) {
		ConcurrentHashMap<Integer, Integer> classLineNumbers = lineNumberMap.get(clazzName);
		if (classLineNumbers == null) {
			classLineNumbers = new ConcurrentHashMap<Integer, Integer>();
			lineNumberMap.put(clazzName, classLineNumbers);
		}
		classLineNumbers.put(instrumentedLineNumber, sourceLineNumber);
	}
	
	/* (non-Javadoc)
	 * @see org.adligo.tests4j_4jacoco.plugin.data.map.I_LineNumberMap#get(java.lang.String, java.lang.Integer)
	 */
	@Override
	public Integer get(String clazzName, Integer instrumentedLineNumber) {
		ConcurrentHashMap<Integer, Integer> classLineNumbers = lineNumberMap.get(clazzName);
		if (classLineNumbers == null) {
			return null;
		}
		return classLineNumbers.get(instrumentedLineNumber);
	}
}
