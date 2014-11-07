package org.adligo.tests4j_4jacoco.plugin.factories;

import org.adligo.tests4j_4jacoco.plugin.whitelists.MockitoClassList;
import org.adligo.tests4j_4jacoco.plugin.whitelists.RequiredClassList;

import java.util.HashSet;
import java.util.Set;

public class MockitoPluginFactory extends BasePluginFactory {
	
  @Override
  protected Set<String> getWhitelist() {
    Set<String> classWhitelist = new HashSet<String>();
    classWhitelist.addAll(new RequiredClassList().getWhitelist());
    classWhitelist.addAll(new MockitoClassList().getWhitelist());
    return classWhitelist;
  }
}
