package org.adligo.tests4j_4jacoco.plugin.factories;

import org.adligo.tests4j_4jacoco.plugin.whitelists.RequiredClassList;

import java.util.Set;

public class DefaultPluginFactory extends BasePluginFactory  {
  
  @Override
  protected Set<String> getWhitelist() {
    return new RequiredClassList().getWhitelist();
  }
}
