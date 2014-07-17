package org.adligo.tests4j_4jacoco.plugin.instrumentation.map;

import org.objectweb.asm.Opcodes;

//import org.objectweb.asm.Type;

public class MapInstrConstants {
	public static final int DATAFIELD_ACC = Opcodes.ACC_SYNTHETIC
			| Opcodes.ACC_PUBLIC 
			//| Opcodes.ACC_PRIVATE 
			| Opcodes.ACC_STATIC 
			//| Opcodes.ACC_VOLATILE 
			| Opcodes.ACC_TRANSIENT;
	
	public static final int INITMETHOD_ACC = Opcodes.ACC_SYNTHETIC |
			Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | 
			//Opcodes.ACC_SYNCHRONIZED |
			Opcodes.ACC_FINAL;
	public static final String INIT_METHOD_DESC = "()Ljava/util/Map;";
	public static final String DATAFIELD_DESC = "java/util/Map";
	public static final String DATAFIELD_CLAZZ = "Ljava/util/Map;";

	public static final Object[] DATAFIELD_INSTANCE = new Object[] { 
		DATAFIELD_DESC};
}
