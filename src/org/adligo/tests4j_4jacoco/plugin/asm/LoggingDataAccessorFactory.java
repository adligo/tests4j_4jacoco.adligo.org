package org.adligo.tests4j_4jacoco.plugin.asm;

import org.adligo.tests4j_4jacoco.plugin.runtime.I_DataAccessorFactory;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_LoggerDataAccessorFactory;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class LoggingDataAccessorFactory implements I_LoggerDataAccessorFactory, IExecutionDataAccessorGenerator {
	private static final String CHANNEL = "tests4j_4jacoco_channel";
	private final String key;
	private String dataTypeDesc;
	
	public LoggingDataAccessorFactory(String pDataTypeDesc) {
		dataTypeDesc = pDataTypeDesc;
		
		this.key = Integer.toHexString(hashCode());
	}
	
	/**
	 * a unique key used for filtering the logging messages.
	 * @return
	 */
	public String getKey() {
		return key;
	}
	
	@Override
	public String getChannel() {
		return CHANNEL;
	}
	
	@Override
	public int generateDataAccessor(long classid, String classname, int probecount,
			MethodVisitor mv) {
		return create(classid, classname, probecount, mv);
	}
	
	/**
	 * push a insance of the dataTypeDesc onto the 
	 * stack 
	 * @see I_DataAccessorFactory#create(long, String, int, MethodVisitor)
	 */
	public int create(final long classid, final String classname,
			final int probecount, final MethodVisitor mv) {

		// The data accessor performs the following steps:
		//
		// final Object[] args = new Object[3];
		// args[0] = Long.valueOf(classid);
		// args[1] = classname;
		// args[2] = Integer.valueOf(probecount);
		// Logger.getLogger(CHANNEL).log(Level.INFO, key, args);
		// final byte[] probedata = (byte[]) args[0];
		//
		// Note that local variable 'args' is used at two places. As were not
		// allowed to allocate local variables we have to keep this value with
		// DUP and SWAP operations on the operand stack.

		// 1. Create parameter array:
		ClassCoverageDataParamFactory paramFactory = new ClassCoverageDataParamFactory(mv);
		paramFactory.create(classid, classname, probecount);

		// Stack[0]: [Ljava/lang/Object;

		mv.visitInsn(Opcodes.DUP);

		// Stack[1]: [Ljava/lang/Object;
		// Stack[0]: [Ljava/lang/Object;

		// 2. Call Logger:

		mv.visitLdcInsn(CHANNEL);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/logging/Logger",
				"getLogger", "(Ljava/lang/String;)Ljava/util/logging/Logger;");

		// Stack[2]: Ljava/util/logging/Logger;
		// Stack[1]: [Ljava/lang/Object;
		// Stack[0]: [Ljava/lang/Object;

		mv.visitInsn(Opcodes.SWAP);

		// Stack[2]: [Ljava/lang/Object;
		// Stack[1]: Ljava/util/logging/Logger;
		// Stack[0]: [Ljava/lang/Object;

		mv.visitFieldInsn(Opcodes.GETSTATIC, "java/util/logging/Level", "INFO",
				"Ljava/util/logging/Level;");

		// Stack[3]: Ljava/util/logging/Level;
		// Stack[2]: [Ljava/lang/Object;
		// Stack[1]: Ljava/util/logging/Logger;
		// Stack[0]: [Ljava/lang/Object;

		mv.visitInsn(Opcodes.SWAP);

		// Stack[3]: [Ljava/lang/Object;
		// Stack[2]: Ljava/util/logging/Level;
		// Stack[1]: Ljava/util/logging/Logger;
		// Stack[0]: [Ljava/lang/Object;

		mv.visitLdcInsn(key);

		// Stack[4]: Ljava/lang/String;
		// Stack[3]: [Ljava/lang/Object;
		// Stack[2]: Ljava/util/logging/Level;
		// Stack[1]: Ljava/util/logging/Logger;
		// Stack[0]: [Ljava/lang/Object;

		mv.visitInsn(Opcodes.SWAP);

		// Stack[4]: [Ljava/lang/Object;
		// Stack[3]: Ljava/lang/String;
		// Stack[2]: Ljava/util/logging/Level;
		// Stack[1]: Ljava/util/logging/Logger;
		// Stack[0]: [Ljava/lang/Object;

		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/logging/Logger",
				"log",
				"(Ljava/util/logging/Level;Ljava/lang/String;[Ljava/lang/Object;)V");

		// Stack[0]: [Ljava/lang/Object;

		// 3. Load data structure from parameter array:

		mv.visitInsn(Opcodes.ICONST_0);
		mv.visitInsn(Opcodes.AALOAD);
		mv.visitTypeInsn(Opcodes.CHECKCAST, dataTypeDesc);

		// Stack[0]: [Z

		return 5; // Maximum local stack size is 5
	}

	

	
}
