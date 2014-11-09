package org.adligo.tests4j_4jacoco.plugin.factories;

import org.adligo.tests4j_4jacoco.plugin.whitelists.MockitoList;
import org.adligo.tests4j_4jacoco.plugin.whitelists.RequiredList;

import java.util.HashSet;
import java.util.Set;

public class MockitoPluginFactory extends BasePluginFactory {
  RequiredList list =  new RequiredList();
  MockitoList mlist = new MockitoList();
  
  @Override
  protected Set<String> getWhitelist() {
    Set<String> classWhitelist = new HashSet<String>();
    classWhitelist.addAll(list.getWhitelist());
    classWhitelist.addAll(mlist.getWhitelist());
    return classWhitelist;
  }

  @Override
  protected Set<String> getNonInstrumentedClasses() {
    Set<String> pkgList = new HashSet<String>();
    pkgList.addAll(list.getNonInstrumentedPackages());
    pkgList.addAll(mlist.getNonInstrumentedPackages());
    return pkgList;
  }
}
