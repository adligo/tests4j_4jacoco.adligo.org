package org.adligo.tests4j_4jacoco.plugin.data.map;

import org.adligo.tests4j_4jacoco.plugin.data.I_ExecutionDataStore;
import org.adligo.tests4j_4jacoco.plugin.runtime.I_JacocoRuntimeData;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MapRuntimeData implements I_JacocoRuntimeData {
	/** store for execution data */
	protected final MapDataStore store;
	
	private long startTimeStamp;

	private String sessionId;

	/**
	 * Creates a new runtime.
	 */
	public MapRuntimeData() {
		store = new MapDataStore();
		sessionId = "<none>";
		startTimeStamp = System.currentTimeMillis();
	}

	/**
	 * Sets a session identifier for this runtime. The identifier is used when
	 * execution data is collected. If no identifier is explicitly set a
	 * identifier is generated from the host name and a random number. This
	 * method can be called at any time.
	 * 
	 * @see #collect(IExecutionDataVisitor, ISessionInfoVisitor, boolean)
	 * @param id
	 *            new session identifier
	 */
	public void setSessionId(final String id) {
		sessionId = id;
	}

	/**
	 * Get the current a session identifier for this runtime.
	 * 
	 * @see #setSessionId(String)
	 * @return current session identifier
	 */
	public String getSessionId() {
		return sessionId;
	}


	/**
	 * Resets all coverage information.
	 */
	public final void reset() {
		synchronized (store) {
			store.reset();
			startTimeStamp = System.currentTimeMillis();
		}
	}

	/**
	 * Returns the coverage data for the class with the given identifier. If
	 * there is no data available under the given id a new entry is created.
	 * This is a synchronized access to the underlying store.
	 * 
	 * @param id
	 *            class identifier
	 * @param name
	 *            VM name of the class
	 * @param probecount
	 *            probe data length
	 * @return execution data
	 */
	public MapExecutionData getExecutionData(final Long id, final String name,
			final int probecount) {
		synchronized (store) {
			return store.get(id, name, probecount);
		}
	}

	/**
	 * Retrieves the execution probe array for a given class. The passed
	 * {@link Object} array instance is used for parameters and the return value
	 * as follows. Call parameters:
	 * 
	 * <ul>
	 * <li>args[0]: class id ({@link Long})
	 * <li>args[1]: vm class name ({@link String})
	 * <li>args[2]: probe count ({@link Integer})
	 * </ul>
	 * 
	 * Return value:
	 * 
	 * <ul>
	 * <li>args[0]: probe array (<code>boolean[]</code>)
	 * </ul>
	 * 
	 * @param args
	 *            parameter array of length 3
	 */
	public void getProbes(final Object[] args) {
		final Long classid = (Long) args[0];
		final String name = (String) args[1];
		final int probecount = ((Integer) args[2]).intValue();
		args[0] = getExecutionData(classid, name, probecount).getProbes();
	}

	/**
	 * In violation of the regular semantic of {@link Object#equals(Object)}
	 * this implementation is used as the interface to the execution data store.
	 * 
	 * @param args
	 *            the arguments as an {@link Object} array
	 * @return has no meaning
	 */
	@Override
	public boolean equals(final Object args) {
		if (args instanceof Object[]) {
			getProbes((Object[]) args);
		}
		return super.equals(args);
	}
	
	/**
	 * Generates code that creates the argument array for the
	 * {@link #getProbes(Object[])} method. The array instance is left on the
	 * operand stack. The generated code requires a stack size of 5.
	 * 
	 * @param classid
	 *            class identifier
	 * @param classname
	 *            VM class name
	 * @param probecount
	 *            probe count for this class
	 * @param mv
	 *            visitor to emit generated code
	 */
	public static void generateArgumentArray(final long classid,
			final String classname, final int probecount, final String scope, 
			final MethodVisitor mv) {
		mv.visitInsn(Opcodes.ICONST_4);
		mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");

		// Class Id:
		mv.visitInsn(Opcodes.DUP);
		mv.visitInsn(Opcodes.ICONST_0);
		mv.visitLdcInsn(Long.valueOf(classid));
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf",
				"(J)Ljava/lang/Long;");
		mv.visitInsn(Opcodes.AASTORE);

		// Class Name:
		mv.visitInsn(Opcodes.DUP);
		mv.visitInsn(Opcodes.ICONST_1);
		mv.visitLdcInsn(classname);
		mv.visitInsn(Opcodes.AASTORE);

		// Probe Count:
		mv.visitInsn(Opcodes.DUP);
		mv.visitInsn(Opcodes.ICONST_2);
		InstrSupport.push(mv, probecount);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer",
				"valueOf", "(I)Ljava/lang/Integer;");
		mv.visitInsn(Opcodes.AASTORE);
		
		// Coverage Scope Name:
		mv.visitInsn(Opcodes.DUP);
		mv.visitInsn(Opcodes.ICONST_3);
		mv.visitLdcInsn(scope);
		mv.visitInsn(Opcodes.AASTORE);
	}

	@Override
	public I_ExecutionDataStore getDataStore() {
		return store;
	}
}
