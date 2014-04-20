package org.adligo.tests4j_4jacoco.plugin.analysis;

import org.adligo.tests4j_4jacoco.plugin.instrumentation.JacocoMethodInstrumenter;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.ASMifier;

/**
 * run this to have the ASMifier veify your ASM code if your 
 * working on this project.
 * 
 * @author scott
 *
 */
public class AsmVerifier {

	public static void main(String [] args) {
		try {
			String internalName = Type.getInternalName(JacocoMethodInstrumenter.class);
			internalName = "./bin/" + internalName + ".class";
			
			ASMifier.main(new String[]{
					internalName});
		} catch (Exception x) {
			x.printStackTrace();
		}
	}
}
