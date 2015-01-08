package org.adligo.tests4j_4jacoco.plugin.instrumentation;

import org.adligo.tests4j.models.shared.association.I_ClassAssociationsLocal;
import org.adligo.tests4j.run.discovery.I_PackageDiscovery;
import org.adligo.tests4j.run.discovery.PackageDiscovery;
import org.adligo.tests4j.system.shared.api.I_Tests4J_CoverageTrialInstrumentation;
import org.adligo.tests4j.system.shared.api.Tests4J_CoverageTrialInstrumentation;
import org.adligo.tests4j.system.shared.trials.AdditionalInstrumentation;
import org.adligo.tests4j.system.shared.trials.I_AbstractTrial;
import org.adligo.tests4j.system.shared.trials.PackageScope;
import org.adligo.tests4j.system.shared.trials.SourceFileScope;
import org.adligo.tests4j_4jacoco.plugin.common.I_CoveragePluginMemory;
import org.adligo.tests4j_4jacoco.plugin.common.I_InstrumentedClassDependencies;
import org.adligo.tests4j_4jacoco.plugin.common.I_TrialInstrumenter;

import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

public class TrialInstrumenter extends ClassAndDependenciesInstrumenter implements I_TrialInstrumenter {
	 private I_CoveragePluginMemory pluginMemory_;
	
	public TrialInstrumenter(ClassInstrumenterSharedMemory memoryIn, I_CoveragePluginMemory mem) {
	  super(memoryIn);
	  pluginMemory_ = mem;
	}
	
	/* (non-Javadoc)
	 * @see org.adligo.tests4j_4jacoco.plugin.I_TrialInstrumenter#instrument(java.lang.Class)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public I_Tests4J_CoverageTrialInstrumentation instrument(Class<? extends I_AbstractTrial> trial) throws IOException {
		if (log_.isLogEnabled(TrialInstrumenter.class)) {
			log_.log(this.getClass().getSimpleName() + 
					log_.getCurrentThreadName() + log_.getLineSeperator() +
					" instrumenting trial " + trial);
		}
		/**
		 * always to additional instrumentation first
		 * so that trials can have their own
		 * package dependency tree
		 */
		AdditionalInstrumentation additional = trial.getAnnotation(AdditionalInstrumentation.class);
		if (additional != null) {
			String pkgs = additional.javaPackages();
			StringTokenizer st = new StringTokenizer(pkgs, ",");
			while (st.hasMoreElements()) {
				String pkg = st.nextToken();
				checkPackageInstrumented(pkg);
			}
		}
		
		SourceFileScope sourceScope =  trial.getAnnotation(SourceFileScope.class);
		I_ClassAssociationsLocal sourceClassDependencies = null;
		Class<?> sourceClass = null;
		String packageName = null;
		if (sourceScope != null) {
		//get allowed dependencies
		  /*
      AllowedReferences ad = trial.getAnnotation(AllowedReferences.class);
      
      if (ad == null) {
        if (log.isLogEnabled(TrialInstrumenter.class)) {
          log.log(this.getClass().getSimpleName() +  " " + trial.getClass().getSimpleName() + " has NO AllowedReferences ");
        }
      } else {
        Class<? extends I_ReferenceGroup>[] grps = ad.groups();
        if (grps != null) {
          if (log.isLogEnabled(TrialInstrumenter.class)) {
            log.log(this.getClass().getSimpleName() +  " " + trial.getName() + " has " + grps.length + " AllowedReferences ");
          }
          for (Class<? extends I_ReferenceGroup> grp: grps) {
            instrumentClass(grp);
          }
        }
      }
      */
			sourceClass = sourceScope.sourceClass();
			String sourceClassName = sourceClass.getName();
			pluginMemory_.addSourceFileScope(sourceClassName);
			I_InstrumentedClassDependencies icd = instrumentClass(sourceClass);
			sourceClassDependencies = icd.getClassDependencies();
			
			
		} else {
			PackageScope packageScope = trial.getAnnotation(PackageScope.class);
			
			if (packageScope != null) {
				packageName = packageScope.packageName();
				pluginMemory_.addPackageScope(packageName);
			}
		}	
		
		checkPackageInstrumented(packageName);
		
		classStart.set(true);
		I_InstrumentedClassDependencies icd = instrumentClass(trial);
		return new Tests4J_CoverageTrialInstrumentation(
				(Class<? extends I_AbstractTrial>) icd.getInstrumentedClass(), sourceClassDependencies);
		
	}

	public void checkPackageInstrumented(String packageName) throws IOException {
		if (packageName != null) {
			if (log_.isLogEnabled(TrialInstrumenter.class)) {
				log_.log(this.getClass().getSimpleName() +  " instrument package " + packageName);
			}
			if ( !memory_.hasStarted(packageName)) {
				//only one thread needs to do each package,
				//as they link up later before the start of the test
			  memory_.start(packageName);
				I_PackageDiscovery pd = new PackageDiscovery(packageName);
				todo.addAndGet(pd.getClassCount());
				instrumentPackageClasses(pd);
				memory_.finish(packageName);
			}
		}
	}

	protected void instrumentPackageClasses(I_PackageDiscovery pd)
			throws IOException {
		if (log_.isLogEnabled(TrialInstrumenter.class)) {
			log_.log(this.getClass().getSimpleName() +  " instrumentPackageClasses " + pd.getPackageName());
		}
		try {
			List<String> classes =  pd.getClassNames();
			for (String name: classes) {
				if ( !classFilter.isFiltered(name)) {
					Class<?> c;
					c = Class.forName(name);
					instrumentClass(c);
				}
			}
			List<I_PackageDiscovery> subs =  pd.getSubPackages();
			for (I_PackageDiscovery sub: subs) {
				instrumentPackageClasses(sub);
			}
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

}
