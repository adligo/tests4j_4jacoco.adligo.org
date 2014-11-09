package org.adligo.tests4j_4jacoco.plugin.factories;

import org.adligo.tests4j_4jacoco.plugin.whitelists.RequiredList;

import java.util.Set;

public class DefaultPluginFactory extends BasePluginFactory  {
  RequiredList list = new RequiredList();
  
  @Override
  protected Set<String> getWhitelist() {
    return list.getWhitelist();
  }

  @Override
  protected Set<String> getNonInstrumentedClasses() {
    return list.getNonInstrumentedPackages();
  }
}
