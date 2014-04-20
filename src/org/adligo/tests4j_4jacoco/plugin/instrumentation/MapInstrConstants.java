package org.adligo.tests4j_4jacoco.plugin.instrumentation;

//import org.objectweb.asm.Type;

public class MapInstrConstants {
	public static final String INIT_METHOD_DESC = "()Ljava/util/Map;";
	public static final String DATAFIELD_DESC = "java/util/Map";
	public static final String DATAFIELD_CLAZZ = "Ljava/util/Map;";
	/**
	 * Ok a note on how this interacts with tests4j, which 
	 * is heavly multithreaded, however most of the 
	 * class file instrumentation happens on the original 
	 * thread before any thread pools are created.
	 *    I chose Map<Integer, Boolean> to replace boolean[] because;
	 *  1) it is a JRE class (which is required as far as I can tell, 
	 *  		I did try a custom class which didn't seem to work)
	 *  2) it is a interface with put and get, which I am guessing
	 *          I can replace at runtime from a custom alteration of ExecutionData
	 *          and use my own Map implementation to 
	 *          handle 3 levels of recording coverage
	 *          2.a)   All Coverage by the list of Trials
	 *          2.b)   Coverage from running specific Trial
	 *          2.c)   Coverage from running a specific Test (in a Trial)
	 *          
	 *           
	 */
	public static final Object[] DATAFIELD_INSTANCE = new Object[] { 
		"java/util/Map"};
}
