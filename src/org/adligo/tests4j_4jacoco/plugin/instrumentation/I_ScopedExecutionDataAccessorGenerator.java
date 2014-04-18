package org.adligo.tests4j_4jacoco.plugin.instrumentation;

import org.jacoco.core.runtime.IRuntime;
import org.objectweb.asm.MethodVisitor;

/**
 * The instrumented classes need a piece of code that obtains a
 * <code>boolean[]</code> instance from the runtime. The mechanism is runtime
 * specific and therefore abstracted by this interface. Implementations are
 * provided by {@link IRuntime} implementations and are used by the
 * instrumentation process.
 */
public interface I_ScopedExecutionDataAccessorGenerator {

		/**
		 * This method generates the byte code required to obtain the coverage data
		 * structure for the class with the given id. Typically the instrumentation
		 * process will embed this code into a method that is called on class
		 * initialization. This method can be called at any time even outside the
		 * target VM.
		 * 
		 * The generated code must push a <code>boolean[]</code> instance to the
		 * operand stack. Except this result object the generated code must not make
		 * any assumptions about the structure of the embedding method or class. The
		 * generated code must not use or allocate local variables.
		 * 
		 * @param classid
		 *            identifier of the class
		 * @param classname
		 *            VM class name
		 * @param probecount
		 *            probe count for this class
		 * @param scope
		 *            a string to represent different levels of
		 *            coverage recording
		 * @param mv
		 *            code output
		 * @return additional stack size required by the implementation, including
		 *         the instance pushed to the stack
		 */
		public int generateDataAccessor(final long classid, final String classname,
				final int probecount, final String scope, MethodVisitor mv);

	}
