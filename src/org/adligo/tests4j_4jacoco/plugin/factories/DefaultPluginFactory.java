package org.adligo.tests4j_4jacoco.plugin.factories;

import org.adligo.tests4j_4jacoco.plugin.whitelists.RequiredList;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DefaultPluginFactory extends BasePluginFactory  {
  RequiredList list = new RequiredList();
  
  public DefaultPluginFactory() {}
  
  @Override
  protected Set<String> getWhitelist() {
    return list.getWhitelist();
  }

  @Override
  protected Set<String> getNonInstrumentedClasses() {
    Set<String> toRet = new HashSet<String>();
    toRet.addAll(list.getNonInstrumentedPackages());
    return toRet;
  }
}
