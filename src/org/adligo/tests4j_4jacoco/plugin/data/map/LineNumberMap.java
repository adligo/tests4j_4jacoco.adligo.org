package org.adligo.tests4j_4jacoco.plugin.data.map;

public class LineNumberMap implements I_LineNumberMap{
	private LineNumberMapMutant mutant;
	
	public LineNumberMap(LineNumberMapMutant p) {
		mutant = p;
	}

	public Integer get(String clazzName, Integer instrumentedLineNumber) {
		return mutant.get(clazzName, instrumentedLineNumber);
	}
}
