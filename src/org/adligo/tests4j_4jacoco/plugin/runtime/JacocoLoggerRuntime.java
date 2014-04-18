package org.adligo.tests4j_4jacoco.plugin.runtime;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.adligo.tests4j.models.shared.system.I_CoverageRecorder;
import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * This {@link IRuntime} implementation uses the Java logging API to report
 * coverage data.
 * <p>
 * 
 * The implementation uses a dedicated log channel. Instrumented classes call
 * {@link Logger#log(Level, String, Object[])} with the class identifier in the
 * first slot of the parameter array. The runtime implements a {@link Handler}
 * for this channel that puts the probe data structure into the first slot of
 * the parameter array.
 */
public class JacocoLoggerRuntime extends JacocoAbstractRuntime {

	private static final String CHANNEL = "jacoco-runtime";
	private static PrintStream OUT = System.out;
	
	private final String key;

	private final Logger logger;

	private final Handler handler;

	/**
	 * Creates a new runtime.
	 */
	public JacocoLoggerRuntime() {
		super();
		this.key = Integer.toHexString(hashCode());
		this.logger = configureLogger();
		this.handler = new RuntimeHandler();
	}

	private Logger configureLogger() {
		final Logger l = Logger.getLogger(CHANNEL);
		l.setUseParentHandlers(false);
		l.setLevel(Level.ALL);
		return l;
	}

	public int generateDataAccessor(final long classid, final String classname,
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

		RuntimeData.generateArgumentArray(classid, classname, probecount, mv);

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
		mv.visitTypeInsn(Opcodes.CHECKCAST, InstrSupport.DATAFIELD_DESC);

		// Stack[0]: [Z

		return 5; // Maximum local stack size is 5
	}

	@Override
	public void startup() throws SecurityException {
		super.startup();
		this.logger.addHandler(handler);
	}
	

	@Override
	public void shutdown() {
		this.logger.removeHandler(handler);
	}

	private class RuntimeHandler extends Handler {

		@Override
		public void publish(final LogRecord record) {
			try {
				if (key.equals(record.getMessage())) {
					Object [] params = record.getParameters();
					Long classId = (Long) params[0];
					String vmClassName = (String) params[1];
					Integer probeCount = (Integer) params[2];
					
					Iterator<String> it = currentScopes.iterator();
					while (it.hasNext()) {
						String key = it.next();
						I_JacocoRuntimeData rtData = data.get(key);
						if (I_CoverageRecorder.TRIAL_RUN.equals(key)) {
							rtData.getProbes(params);
						} else {
							Object [] paramsCopy = new Object [3];
							paramsCopy[0] = classId;
							paramsCopy[1] = vmClassName;
							paramsCopy[2] = probeCount;
							rtData.getProbes(paramsCopy);
						}
					}
				}
			} catch (Exception x) {
				x.printStackTrace(OUT);
			}
		}

		@Override
		public void flush() {
			// nothing to do
		}

		@Override
		public void close() throws SecurityException {
			// The Java logging framework removes and closes all handlers on JVM
			// shutdown. As soon as our handler has been removed, all classes
			// that might get instrumented during shutdown (e.g. loaded by other
			// shutdown hooks) will fail to initialize. Therefore we add ourself
			// again here. This is a nasty hack that might fail in some Java
			// implementations.
			logger.addHandler(handler);
		}
	}
}

