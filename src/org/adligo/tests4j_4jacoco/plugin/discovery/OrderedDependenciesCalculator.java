package org.adligo.tests4j_4jacoco.plugin.discovery;

import org.adligo.tests4j.models.shared.association.I_ClassAssociationsLocal;
import org.adligo.tests4j.models.shared.association.I_ClassParentsLocal;
import org.adligo.tests4j.shared.asserts.reference.DependencyMutant;
import org.adligo.tests4j.shared.asserts.reference.I_ClassAliasLocal;
import org.adligo.tests4j.shared.asserts.reference.I_Dependency;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

/**
 * This class contains code that was extracted from OrderedClassDiscovery
 * for ease of testing.
 * @author scott
 *
 */
public class OrderedDependenciesCalculator {

  /**
   * This was
   * @param topName
   * @param refMap
   * @return
   */
  public TreeSet<I_Dependency> count(String topName, 
      Map<I_ClassAliasLocal, I_ClassAssociationsLocal> refMap) {
    
    Map<String,DependencyMutant> refCounts = new HashMap<String,DependencyMutant>();
    
    Set<Entry<I_ClassAliasLocal, I_ClassAssociationsLocal>> refs =  refMap.entrySet();
    for (Entry<I_ClassAliasLocal,I_ClassAssociationsLocal> e: refs) {
      I_ClassAliasLocal key = e.getKey();
      String className = key.getName();
      I_ClassAssociationsLocal crs = e.getValue();
      Set<I_ClassParentsLocal> classes = crs.getDependenciesLocal();
      
      DependencyMutant count = null;
      if (isNotClass(crs, topName)) {
        
        
        for (I_ClassParentsLocal ref: classes) {
          if (isNotClass(ref, className)) {
            String refName = ref.getName();
            count = refCounts.get(refName);
            if (count == null) {
              count = new DependencyMutant();
              count.setAlias(ref);
              count.addReference();
            } else {
              count.addReference();
            }
            refCounts.put(ref.getName(), count);
          }
        }
      } else {
        for (I_ClassParentsLocal ref: classes) {
          if (isNotClass(ref, className)) {
            count = refCounts.get(ref.getName());
            if (count == null) {
              count = new DependencyMutant();
              count.setAlias(ref);
              count.addReference();
            } else {
              count.addReference();
            }
            refCounts.put(ref.getName(), count);
          }
        }
      }
    }
    
    TreeSet<I_Dependency> deps = new TreeSet<I_Dependency>(refCounts.values());
    return deps;
  }
  
  private boolean isNotClass(I_ClassParentsLocal ref, String topName) {
    String className = ref.getName();
    if (className.equals(topName)) {
      return false;
    } 
    return true;
  }
}
