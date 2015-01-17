package org.adligo.tests4j_4jacoco.plugin.discovery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.adligo.tests4j.models.shared.association.I_ClassAssociationsCache;
import org.adligo.tests4j.models.shared.association.I_ClassAssociationsLocal;
import org.adligo.tests4j.models.shared.association.I_ClassParentsLocal;
import org.adligo.tests4j.run.helpers.I_ClassFilter;
import org.adligo.tests4j.shared.asserts.reference.ClassAlias;
import org.adligo.tests4j.shared.asserts.reference.ClassAliasLocal;
import org.adligo.tests4j.shared.asserts.reference.DependencyMutant;
import org.adligo.tests4j.shared.asserts.reference.I_ClassAliasLocal;
import org.adligo.tests4j.shared.asserts.reference.I_Dependency;
import org.adligo.tests4j.shared.i18n.I_Tests4J_Constants;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;
import org.adligo.tests4j_4jacoco.plugin.common.I_OrderedClassDependencies;
import org.adligo.tests4j_4jacoco.plugin.common.I_OrderedClassDiscovery;

/**
 * a model like (non thread safe) class that loads classes into
 * the class loader, discovers 
 * references.
 * 
 * references and dependencies are very similar concepts in this package as follows;
 * references illuminate that one class references another.
 * dependencies illuminate that one class depends on another, 
 *     and indicate 
 *    The I_CachedClassBytesClassLoader is shared memory between threads.
 * Also this model keeps a cache of the references for classes
 * it reads, so it doesn't need to re-ASM byte code read them.
 * 
 * 
 * @author scott
 *
 */
public class OrderedClassDiscovery implements I_OrderedClassDiscovery {
  private I_Tests4J_Constants constants_;
	private I_Tests4J_Log log_;
	private I_ClassFilter classFilter_;
	private I_ClassAssociationsCache cache_;
	private OrderedCalculatorFactory orderedCalculatorFactory_ = new OrderedCalculatorFactory();
	private HashMap<I_ClassAliasLocal, I_ClassAssociationsLocal> refMap_ = new HashMap<I_ClassAliasLocal,I_ClassAssociationsLocal>();
	private I_ClassDependenciesDiscovery fullDependenciesDiscovery_;
	private I_ClassDependenciesDiscovery circularDependenciesDiscovery_;
	
	public OrderedClassDiscovery() {
	}
	
	/**
	 * @diagram_sync with InstrumentationOverview.seq on 8/20/2014
	 * @diagram_sync with DiscoveryOverview.seq on 8/20/2014
	 * 
	 * @see org.adligo.tests4j_4jacoco.plugin.discovery.I_OrderedClassDependenciesDiscovery#findOrLoad(java.lang.Class)
	 */
	@Override
	public I_OrderedClassDependencies findOrLoad(Class<?> c) throws IOException, ClassNotFoundException {
		if (log_.isLogEnabled(OrderedClassDiscovery.class)) {
			log_.log("ClassReferencesDiscovery.discoverAndLoad " + c.getName());
		}
		String className = c.getName();
		refMap_.clear();
		I_ClassAssociationsLocal crefs =  cache_.getDependencies(className);
		if (crefs != null) {
			refMap_.put(new ClassAliasLocal(crefs), crefs);
			fillRefMapFromFullRef(crefs);
		} else {
			crefs = fillRefMapFromClass(c);
		}
		//@diagram_sync with DiscoveryOverview.seq on 8/17/2014
		List<String> refOrder = calculateRefOrder(c);
		return new OrderedClassDependencies(crefs, refOrder);
	}

	private void fillRefMapFromFullRef(I_ClassAssociationsLocal full) throws ClassNotFoundException, IOException {
		Set<I_ClassParentsLocal> refs = full.getDependenciesLocal();
		for (I_ClassParentsLocal ref: refs) {
			I_ClassAssociationsLocal refLoc = circularDependenciesDiscovery_.findOrLoad(ref.getTarget());
			refMap_.put(refLoc, refLoc);
		}
	}

	/**
	 * @diagram_sync with DiscoveryOverview.seq on 8/17/2014
	 * @param c
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private I_ClassAssociationsLocal fillRefMapFromClass(Class<?> c) throws ClassNotFoundException, IOException {
		//@diagram_sync with DiscoveryOverview.seq on 8/17/2014
		I_ClassAssociationsLocal initial = fullDependenciesDiscovery_.findOrLoad(c);
		
		List<I_ClassParentsLocal> parents =  initial.getParentsLocal();
		for (I_ClassParentsLocal cpl : parents) {
			//@diagram_sync with DiscoveryOverview.seq on 8/17/2014
			I_ClassAssociationsLocal parentFull = circularDependenciesDiscovery_.findOrLoad(cpl.getTarget());
			refMap_.put(parentFull, parentFull);
		}
		
		Set<I_ClassParentsLocal> refs = initial.getDependenciesLocal();
		Set<I_ClassParentsLocal> refsCopy = new HashSet<I_ClassParentsLocal>(refs);
		refsCopy.removeAll(parents);
		for (I_ClassParentsLocal ref : refsCopy) {
			//@diagram_sync with DiscoveryOverview.seq on 8/17/2014
			I_ClassAssociationsLocal refFull = circularDependenciesDiscovery_.findOrLoad(ref.getTarget());
			refMap_.put(refFull, refFull);
		}
		
		//@diagram_sync with DiscoveryOverview.seq on 8/17/2014
		I_ClassAssociationsLocal full = circularDependenciesDiscovery_.findOrLoad(c);
		refMap_.put(full, full);
		Set<I_ClassParentsLocal> fullRefs = full.getDependenciesLocal();
		Set<I_ClassParentsLocal> fullRefsCopy = new HashSet<I_ClassParentsLocal>(fullRefs);
		fullRefsCopy.removeAll(parents);
		fullRefsCopy.removeAll(refsCopy);
		for (I_ClassParentsLocal ref : fullRefsCopy) {
			//@diagram_sync with DiscoveryOverview.seq on 8/17/2014
			I_ClassAssociationsLocal refFull = circularDependenciesDiscovery_.findOrLoad(ref.getTarget());
			refMap_.put(refFull, refFull);
		}
		return full;
	}
	
	/**
	 * @diagram_sync with DiscoveryOverview.seq on 8/17/2014
	 * 
	 * ok at this point either we have 
	 * just references, or a mix of references and 
	 * cached dependencies.  
	 * Calculate the most referenced in the group and order
	 * 
	 * This calculates a rough order of dependencies
	 * @return
	 */
	private List<String> calculateRefOrder(Class<?> c) {
	  String topName = c.getName();
	  TreeSet<I_Dependency> deps = toDependencies(topName);
	  OrderedReferenceCalculator calc = orderedCalculatorFactory_.createReferenceOrderCalculator(
	      classFilter_, log_, constants_, refMap_, c);
	  return calc.calculateOrder(deps);
	}

	public TreeSet<I_Dependency> toDependencies(String topName) {
	  OrderedDependenciesCalculator odc = orderedCalculatorFactory_.createOrderedDependenciesCalculator();
    return odc.count(topName, refMap_);
	}
	/**
	 * @diagram_sync with Discovery_ClassReferenceDiscovery.seq on 8/1/2014
	 * @diagram_sync with Discovery_ClassInstrumenter.seq on 8/1/2014
	 * 
	 * @param className
	 * @return
	 */
	public I_ClassAssociationsLocal getReferences(I_ClassAliasLocal alias) {
		return refMap_.get(alias);
	}

	public I_Tests4J_Log getLog() {
		return log_;
	}

	public I_ClassFilter getClassFilter() {
		return classFilter_;
	}

	public I_ClassAssociationsCache getCache() {
		return cache_;
	}

	public I_ClassDependenciesDiscovery getFullDependenciesDiscovery() {
		return fullDependenciesDiscovery_;
	}

	public I_ClassDependenciesDiscovery getCircularDependenciesDiscovery() {
		return circularDependenciesDiscovery_;
	}

	public void setLog(I_Tests4J_Log log) {
		this.log_ = log;
	}

	public void setClassFilter(I_ClassFilter classFilter) {
		this.classFilter_ = classFilter;
	}

	public void setCache(I_ClassAssociationsCache cache) {
		this.cache_ = cache;
	}

	public void setFullDependenciesDiscovery(
			I_ClassDependenciesDiscovery classDependenciesDiscovery) {
		this.fullDependenciesDiscovery_ = classDependenciesDiscovery;
	}

	public void setCircularDependenciesDiscovery(
			I_ClassDependenciesDiscovery circularDependenciesDiscovery) {
		this.circularDependenciesDiscovery_ = circularDependenciesDiscovery;
	}

  public I_Tests4J_Constants getConstants() {
    return constants_;
  }

  public void setConstants(I_Tests4J_Constants constants) {
    this.constants_ = constants;
  }

}
