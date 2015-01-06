package org.adligo.tests4j_4jacoco.plugin.instrumentation.common;

import java.lang.reflect.Method;

public class CallJacocoInit {

  public static void callJacocoInit(Class<?> c) {
    if (c != null) {
      try {
        Method jacocoInit = c.getMethod("$jacocoInit", new Class[] {});
        
        if (jacocoInit != null) {
          jacocoInit.invoke(c, new Object[] {});
        }
      } catch (NoSuchMethodException x) {
        //interfaces don't have it
      } catch (Throwable e) {
        throw new RuntimeException("Class " + c + " failed to call $jacocoInit.", e);
      }
    }
  }
  
}
