package org.adligo.tests4j_4jacoco.plugin.discovery;

import org.adligo.tests4j.models.shared.association.I_ClassAssociationsLocal;
import org.adligo.tests4j.models.shared.association.I_ClassParentsLocal;
import org.adligo.tests4j.run.helpers.I_ClassFilter;
import org.adligo.tests4j.shared.asserts.reference.ClassAlias;
import org.adligo.tests4j.shared.asserts.reference.I_ClassAlias;
import org.adligo.tests4j.shared.asserts.reference.I_ClassAliasLocal;
import org.adligo.tests4j.shared.asserts.reference.I_Dependency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ReferenceOrderCalculator {
  private final I_ClassFilter classFilter_;
  private final Map<I_ClassAliasLocal, I_ClassAssociationsLocal> refMap_;
  private Class<?> clazz_;
  private String topName_;
  private Set<I_Dependency> deps_;
  
  public ReferenceOrderCalculator(I_ClassFilter filter, Map<I_ClassAliasLocal, I_ClassAssociationsLocal> refMap) {
    classFilter_ = filter;
    refMap_ = refMap;
  }
  
  public List<String> calculateOrder(Class<?> c, TreeSet<I_Dependency> deps) {
    clazz_ = c;
    deps_ = deps;
    topName_ = c.getName();
    List<String> toRet = new ArrayList<String>();
    Map<String,I_Dependency> depsMap = new HashMap<String,I_Dependency>();
    Iterator<I_Dependency> depsIt = deps_.iterator();
    while (depsIt.hasNext()) {
      I_Dependency dep = depsIt.next();
      I_ClassAlias alias =  dep.getAlias();
      depsMap.put(alias.getName(), dep);
    }
    HashSet<I_Dependency> hashDeps = new HashSet<I_Dependency>(deps_);
    Map<String,Set<String>> lastMissing = new HashMap<String,Set<String>>();
    int orderTry = 0;
    //10000 trys is rather arbitrary, but it should complete in less than 10 for most classes
    while (deps.size() >= 1 && orderTry <= 10000) {
      orderTry++;
      //use tree set for ordering
      deps = new TreeSet<I_Dependency>(hashDeps);
      Iterator<I_Dependency> it = deps.iterator();
      while (it.hasNext()) {
        I_Dependency classDep = it.next();
        I_ClassParentsLocal classDepAlias = (I_ClassParentsLocal) classDep.getAlias();
        String classDepName = classDepAlias.getName();
       
        if (toRet.contains(classDepName)) {
          hashDeps.remove(classDep);
        } else {
          List<String> parentNames = classDepAlias.getParentNames();
          if (parentNames.size() == 0 || toRet.containsAll(parentNames)) {
            Class<?> dc = classDepAlias.getTarget();
            if (classFilter_.isFiltered(dc)) {
              //add the filtered class but not it's dependencies
              if (!toRet.contains(classDepName)) {
                toRet.add(classDepName);
                hashDeps.remove(classDep);
              }
              
            } else {
              Set<String> depDepsNotInResult = addClassWithDependenciesAlreadyInResult(toRet, hashDeps, classDep);
              if (!depDepsNotInResult.isEmpty()) {
                lastMissing.put(classDepName, depDepsNotInResult);
              }
            }
          } else {
            //add the parents
            //add all of the jse stuff
            List<I_ClassParentsLocal> parents =  classDepAlias.getParentsLocal();
            for (I_ClassParentsLocal parent: parents) {
              String parentName = parent.getName();
              Class<?> pc = parent.getTarget();
              if (classFilter_.isFiltered(pc)) {
                if (!toRet.contains(parentName)) {
                   toRet.add(parentName);
                 }
              } else {
                I_Dependency pdep = depsMap.get(parentName);
                Set<String> depDepsNotInResult = addClassWithDependenciesAlreadyInResult(toRet, hashDeps, pdep);  
                if (!depDepsNotInResult.isEmpty()) {
                  lastMissing.put(parentName, depDepsNotInResult);
                }
              }
            }
          }
        }
      }
    }
    
    if (hashDeps.size() >= 1) {
      StringBuilder sb = new StringBuilder();
      sb.append("Unable to find dependency order for the following class;" + System.lineSeparator() +
          clazz_.getName() + System.lineSeparator());
      Iterator<I_Dependency> dit = hashDeps.iterator();
      sb.append("The inital dependencies are;" + System.lineSeparator());
      sb.append("\t" + depsMap.keySet() +  System.lineSeparator());
      sb.append("The remaining dependencies are;" + System.lineSeparator());
      while (dit.hasNext()) {
        I_Dependency dep = dit.next();
        I_ClassAliasLocal ca = (I_ClassAliasLocal) dep.getAlias();
        String name = ca.getName();
        sb.append("\t" + name + "   " + lastMissing.get(name) + System.lineSeparator());
        I_ClassAssociationsLocal associations = refMap_.get(ca);
        if (associations != null) {
          sb.append("\t\t" + associations.getDependencyNames() + System.lineSeparator());
        }
      }
      sb.append("The dependency order not returned at exception time is as follows;" + System.lineSeparator() +
          toRet);
      throw new IllegalStateException(sb.toString());
    }
    boolean adding = true;
    int count = 1;
    while (adding) {
      String inName = topName_ + "$" + count;
      if (refMap_.containsKey( new ClassAlias(inName))) {
        if (!toRet.contains(inName)) {
          toRet.add(inName);
        }
      } else {
        adding = false;
      }
      count++;
    }
    if (!toRet.contains(topName_)) {
      toRet.add(topName_);
    }
    return toRet;
  }

  /**
   * @param result
   * @param it
   * @param dependency
   * @return the references that 
   * are not in the result, if this is a empty
   * set then the dependency was added to the result.
   */
  public Set<String> addClassWithDependenciesAlreadyInResult(List<String> result, 
      HashSet<I_Dependency> hashDeps,
      I_Dependency dependency) {
    I_ClassParentsLocal classDepAlias = (I_ClassParentsLocal) dependency.getAlias();
    String classDepName = classDepAlias.getName();
    I_ClassAssociationsLocal local = refMap_.get(classDepAlias);
    if (local == null) {
      throw new NullPointerException("problem finding refs for " + classDepName 
          + " on " + clazz_);
    }
    Set<String> refNames =  local.getDependencyNames();
    refNames = new HashSet<String>(refNames);
    if (local.hasCircularDependencies()) {
      refNames.removeAll(local.getCircularDependenciesNames());
    } 
    refNames.remove(classDepName);
    
    refNames.removeAll(result);
    Iterator<String> rit = refNames.iterator();
    while (rit.hasNext()) {
      String name = rit.next();
      if (classFilter_.isFiltered(name)) {
        rit.remove();
      }
    }
    if (refNames.size() == 0) {
       if (!result.contains(classDepName)) {
         result.add(classDepName);
         hashDeps.remove(dependency);
       }
       return Collections.emptySet();
    } else {
      return refNames;
    }
  }
}
