package org.adligo.tests4j_4jacoco.plugin.runtime;

import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.objectweb.asm.MethodVisitor;

/**
 * Note this class is fairly similar to 
 * @see IExecutionDataAccessorGenerator.
 * 
 * The only difference here is the comments about
 * what gets pushed onto the stack from the create method.
 * It can really be anything, although the two know implementations use;
 * SimpleJacocoPlugin uses boolean[] for data
 * TieredJacocoPlugin uses a Map
 * 
 * @author scott
 *
 */
public interface I_DataAccessorFactory {

	/**
	 * @see IExecutionDataAccessorGenerator#generateDataAccessor(long, String, int, MethodVisitor)
	 * this can push anything onto the stack
	 */
	public int create(final long classid, final String classname,
			final int probecount, MethodVisitor mv);
	
}
