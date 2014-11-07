package org.adligo.tests4j_4jacoco.plugin.whitelists;

import org.adligo.tests4j.run.common.I_Classes;

import java.util.Collections;
import java.util.Set;

public class BaseClassList {
  private Set<String> whitelist_;

  /**
   * extracted so it can be tested for the runtime exception
   * @param clazz
   */
  public static void checkClass(String clazz, I_Classes classes) {
    try {
      //actually load all of the classes using the default classloader here
      classes.forName(clazz, false, ClassLoader.getSystemClassLoader());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
  
  public BaseClassList(Set<String> whitelist, I_Classes classes) {
    whitelist_ = Collections.unmodifiableSet(whitelist);
    for (String clazz: whitelist_) {
      checkClass(clazz, classes);
    }
  }
  
  public Set<String> getWhitelist() {
    return whitelist_;
  }
}
