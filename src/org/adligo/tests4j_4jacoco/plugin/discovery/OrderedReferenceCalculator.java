package org.adligo.tests4j_4jacoco.plugin.discovery;

import org.adligo.tests4j.models.shared.association.I_ClassAssociationsLocal;
import org.adligo.tests4j.models.shared.association.I_ClassParentsLocal;
import org.adligo.tests4j.run.helpers.I_ClassFilter;
import org.adligo.tests4j.shared.asserts.reference.ClassAlias;
import org.adligo.tests4j.shared.asserts.reference.I_ClassAlias;
import org.adligo.tests4j.shared.asserts.reference.I_ClassAliasLocal;
import org.adligo.tests4j.shared.asserts.reference.I_Dependency;
import org.adligo.tests4j.shared.common.StringMethods;
import org.adligo.tests4j.shared.i18n.I_Tests4J_Constants;
import org.adligo.tests4j.shared.i18n.I_Tests4J_CoveragePluginMessages;
import org.adligo.tests4j.shared.i18n.I_Tests4J_ReportMessages;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class was extracted from OrderedClassDependencies
 * to make it easier to test, since it was the cause of 
 * a more then a few late night debugging sessions.
 * 
 * @author scott
 *
 */
public class OrderedReferenceCalculator {
  private final I_ClassFilter classFilter_;
  private final HashMap<I_ClassAliasLocal, I_ClassAssociationsLocal> refMap_;
  private Class<?> clazz_;
  private String topName_;
  private Set<I_Dependency> deps_;
  private HashMap<String,I_Dependency> depsMap_;
  private I_Tests4J_Constants constants_;
  private I_Tests4J_Log log_;
  
  public OrderedReferenceCalculator(I_ClassFilter filter, I_Tests4J_Log log, I_Tests4J_Constants constants,
      HashMap<I_ClassAliasLocal, I_ClassAssociationsLocal> refMap, Class<?> clazz) {
    if (filter == null) {
      throw new NullPointerException();
    }
    classFilter_ = filter;
    if (log == null) {
      throw new NullPointerException();
    }
    log_ = log;
    
    if (constants == null) {
      throw new NullPointerException();
    }
    constants_ = constants;
    
    if (refMap == null) {
      throw new NullPointerException();
    }
    refMap_ = refMap;
    
    clazz_ = clazz;
    topName_ = clazz.getName();
  }
  
  /**
   * Calculate the reference order to load classes 
   * into the class loader.
   * @param c
   * @param deps
   * @return
   */
  public List<String> calculateOrder(TreeSet<I_Dependency> deps) {
    deps_ = deps;
    
    List<String> toRet = new ArrayList<String>();
    depsMap_ = new HashMap<String,I_Dependency>();
    
    Iterator<I_Dependency> depsIt = deps_.iterator();
    while (depsIt.hasNext()) {
      I_Dependency dep = depsIt.next();
      I_ClassAlias alias =  dep.getAlias();
      depsMap_.put(alias.getName(), dep);
    }
    HashSet<I_Dependency> hashDeps = new HashSet<I_Dependency>(deps_);
    Map<String,Set<String>> lastMissing = new HashMap<String,Set<String>>();
    int orderTry = 0;
    //10000 trys is rather arbitrary, but it should complete in less than 10 for most classes
    while (deps.size() >= 1 && orderTry <= 10000) {
      orderTry++;
      //use tree set for ordering
      if (orderTry >= 1) {
        deps = new TreeSet<I_Dependency>(hashDeps);
      }
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
                I_Dependency pdep = depsMap_.get(parentName);
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
      throwUnableToCalculateOrderException(toRet, hashDeps, lastMissing, depsMap_.keySet());
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
   * This method is public for ease of testing only.
   * @param toRet
   * @param hashDeps
   * @param lastMissing
   */
  public void throwUnableToCalculateOrderException(List<String> toRet,
      Set<? extends I_Dependency> hashDeps, Map<String, Set<String>> lastMissing,
          Set<String> initialDeps) {
    
    I_Tests4J_CoveragePluginMessages messages =  constants_.getCoveragePluginMessages();
    I_Tests4J_ReportMessages reportMessages = constants_.getReportMessages();
    StringBuilder sb = new StringBuilder();
    sb.append(messages.getUnableToFineDependencyOrderForTheFollowingClass() + log_.lineSeparator() +
        clazz_.getName() + log_.lineSeparator());
    Iterator<? extends I_Dependency> dit = hashDeps.iterator();
    sb.append(messages.getTheDependenciesAreAsFollows() + log_.lineSeparator());

    String toAdd = StringMethods.orderLine(constants_.isLeftToRight(), reportMessages.getIndent(),
        "" + initialDeps) + log_.lineSeparator();
    sb.append(toAdd);
    
    toAdd = StringMethods.orderLine(constants_.isLeftToRight(), reportMessages.getIndent(),
        messages.getTheFollowingDependenciesCouldNotBeOrdered()) + log_.lineSeparator();
    sb.append(toAdd);
    while (dit.hasNext()) {
      I_Dependency dep = dit.next();
      I_ClassAliasLocal ca = (I_ClassAliasLocal) dep.getAlias();
      String name = ca.getName();
      toAdd = StringMethods.orderLine(constants_.isLeftToRight(), reportMessages.getIndent(),
          name, "   ", "" + lastMissing.get(name)) + log_.lineSeparator();
      sb.append(toAdd);
      I_ClassAssociationsLocal associations = refMap_.get(ca);
      if (associations != null) {
        toAdd = StringMethods.orderLine(constants_.isLeftToRight(), reportMessages.getIndent(),
            reportMessages.getIndent(),
            "" + associations.getDependencyNames()) + log_.lineSeparator();
      }
    }
    toAdd = StringMethods.orderLine(constants_.isLeftToRight(), reportMessages.getIndent(),
        messages.getTheFollowingDependenciesWereOrderedSuccessfully()) + log_.lineSeparator();
    sb.append(toAdd);
    toAdd = StringMethods.orderLine(constants_.isLeftToRight(), reportMessages.getIndent(),
        "" + toRet) + log_.lineSeparator();
    sb.append(toAdd);
    throw new IllegalStateException(sb.toString());
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
      HashSet<I_Dependency> hashDeps, I_Dependency dependency) {
    I_ClassParentsLocal classDepAlias = (I_ClassParentsLocal) dependency.getAlias();
    String classDepName = classDepAlias.getName();
    I_ClassAssociationsLocal local = refMap_.get(classDepAlias);
    if (local == null) {
      throw new NullPointerException(classDepName + log_.lineSeparator() + clazz_);
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
