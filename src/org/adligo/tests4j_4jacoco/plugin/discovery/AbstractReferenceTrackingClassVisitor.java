package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.util.List;

import org.adligo.tests4j.models.shared.dependency.ClassAttributes;
import org.objectweb.asm.ClassVisitor;

/**
 * a abstraction of what the ReferenceTrackingClassVisitor
 * needs to do, I would have used a interface, but ASM isn't built that way.
 * 
 * @author scott
 *
 */
public abstract class AbstractReferenceTrackingClassVisitor extends ClassVisitor {

	public AbstractReferenceTrackingClassVisitor(int version) {
		super(version);
	}
	
	public abstract void reset();
	
	/**
	 * returns the type ie ('Ljava.lang.Object;')
	 * of the classes referenced in the byte code methods/fields
	 * @return
	 */
	public abstract List<ClassAttributes> getClassCalls();
}
