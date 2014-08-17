package org.adligo.tests4j_4jacoco.plugin.asm;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.adligo.tests4j_4jacoco.plugin.common.I_StackHelper;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AnalyzerAdapter;
/**
 * This class assumes a single thread is 
 * using asm.
 * 
 * @author scott
 *
 */
public class BytecodeInjectionDebuger {
	private static boolean enabled = false;
	private static Stack<String> stackDebug = new Stack<String>();
	
	public static void log(I_StackHelper sh, MethodVisitor mv, String p) {
		
		
		mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		putInStackDebug("out");
		//Stack[+0]  out (PrintStream)
		StringBuilder sb = new StringBuilder();
		sb.append(p + " \n ");
		appendStackDebug(sb);
		
 		mv.visitLdcInsn(sb.toString());
 		putInStackDebug(p);
 		sh.incrementStackSize(2);
 		//Stack[+1] String p
 		//Stack[+0] out (PrintStream)
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
		sh.decrementStackSize(2);
		popOffStackDebug(2);
		
		if (sh.getCurrentStackSize() != stackDebug.size()) {
			StringBuilder sbe = new StringBuilder();
			sbe.append("The stackDebug " + stackDebug.size() + " must match the current stack size " + sh.getCurrentStackSize()+ "!\n");
			appendStackDebug(sbe);
			throw new IllegalStateException(sbe.toString());
		}
	}

	public static void logStack(I_StackHelper sh, AnalyzerAdapter aa, MethodVisitor mv) {
		
		mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		putInStackDebug("out");
		//Stack[+0]  out (PrintStream)
		StringBuilder sb = new StringBuilder();
		appendStackDebug(sb,aa.stack);
		
 		mv.visitLdcInsn(sb.toString());
 		putInStackDebug(sb.toString());
 		sh.incrementStackSize(2);
 		//Stack[+1] String p
 		//Stack[+0] out (PrintStream)
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
		sh.decrementStackSize(2);
		popOffStackDebug(2);
		
		if (sh.getCurrentStackSize() != stackDebug.size()) {
			StringBuilder sbe = new StringBuilder();
			sbe.append("The stackDebug must match the current stack size " + sh.getCurrentStackSize()+ "!\n");
			appendStackDebug(sbe);
			throw new IllegalStateException(sbe.toString());
		}
	}

	private static void appendStackDebug(StringBuilder sb) {
		for (int i = 0; i < stackDebug.size(); i++) {
			String db = stackDebug.get(i);
			sb.append("Stack[" + i + "] = " + db + "\n");
		}
	}
	
	private static void appendStackDebug(StringBuilder sb, List<Object> stacks) {
		Iterator<Object> it = stacks.iterator();
		int i = 0;
		while (it.hasNext()) {
			Object db = it.next();
			sb.append("Stack[" + i++ + "] = " + db + "\n");
		}
	}
	
	public static void putInStackDebug(String p) {
		stackDebug.add(p);
	}
	
	public static String popOffStackDebug() {
		return stackDebug.pop();
	}
	
	public static void dupStackDebug() {
		String top = stackDebug.lastElement();
		stackDebug.add(top);
	}
	
	public static void popOffStackDebug(int p) {
		for (int i = 0; i < p; i++) {
			popOffStackDebug();
		}
	}
	
	public static boolean isEnabled() {
		return enabled;
	}
	
	public static void setEnabled(boolean p) {
		enabled = p;
	}
	
	public static void logStackTopElement(I_StackHelper sh,  MethodVisitor mv, String message) {
		
		mv.visitInsn(Opcodes.DUP);
		putInStackDebug("?logStackTopElement");
		sh.incrementStackSize();
		
		//stack[0/top] = ?
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false);
		//stack[0/top] = String
		
		mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		putInStackDebug("out");
		sh.incrementStackSize();
		//stack[0/top] = String
		//Stack[1]  out (PrintStream)
		mv.visitInsn(Opcodes.SWAP);
		
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
		sh.decrementStackSize();
		sh.decrementStackSize();
		popOffStackDebug(2);
	}
}
