package org.adligo.tests4j_4jacoco.plugin.discovery;

import org.adligo.tests4j.models.shared.association.I_ClassAssociationsLocal;
import org.adligo.tests4j.run.helpers.I_ClassFilter;
import org.adligo.tests4j.shared.asserts.reference.I_ClassAliasLocal;
import org.adligo.tests4j.shared.i18n.I_Tests4J_Constants;
import org.adligo.tests4j.shared.output.I_Tests4J_Log;

import java.util.HashMap;

/**
 * This a factory for extracted code from OrderedClassDiscovery,
 * for mock instance of the calculators later on.
 * @author scott
 *
 */
public class OrderedCalculatorFactory {

  public OrderedDependenciesCalculator createOrderedDependenciesCalculator() {
    return new OrderedDependenciesCalculator();
  }
  
  public OrderedReferenceCalculator createReferenceOrderCalculator(I_ClassFilter filter,
      I_Tests4J_Log log, I_Tests4J_Constants constants,
      HashMap<I_ClassAliasLocal, I_ClassAssociationsLocal> refMap, Class<?> clazz) {
    return new OrderedReferenceCalculator(filter, log, constants, refMap, clazz);
  }
}
