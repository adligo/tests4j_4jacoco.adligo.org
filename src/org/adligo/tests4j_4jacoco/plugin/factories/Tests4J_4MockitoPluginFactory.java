package org.adligo.tests4j_4jacoco.plugin.factories;

import org.adligo.tests4j_4jacoco.plugin.whitelists.MockitoList;
import org.adligo.tests4j_4jacoco.plugin.whitelists.RequiredList;
import org.adligo.tests4j_4jacoco.plugin.whitelists.Tests4J_4MockitoList;

import java.util.HashSet;
import java.util.Set;

public class Tests4J_4MockitoPluginFactory extends BasePluginFactory {
  RequiredList list =  new RequiredList();
  MockitoList mlist = new MockitoList();
  Tests4J_4MockitoList tfjmlist = new Tests4J_4MockitoList();
  
  @Override
  protected Set<String> getWhitelist() {
    Set<String> classWhitelist = new HashSet<String>();
    classWhitelist.addAll(list.getWhitelist());
    classWhitelist.addAll(mlist.getWhitelist());
    classWhitelist.addAll(tfjmlist.getWhitelist());
    
    return classWhitelist;
  }

  @Override
  protected Set<String> getNonInstrumentedClasses() {
    Set<String> pkgList = new HashSet<String>();
    pkgList.addAll(list.getNonInstrumentedPackages());
    pkgList.addAll(mlist.getNonInstrumentedPackages());
    pkgList.addAll(tfjmlist.getNonInstrumentedPackages());
    return pkgList;
  }
}
