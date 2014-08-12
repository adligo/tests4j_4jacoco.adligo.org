package org.adligo.tests4j_4jacoco.plugin;

import java.io.IOException;
import java.util.List;

import org.adligo.tests4j.models.shared.system.I_Tests4J_Log;
import org.adligo.tests4j.models.shared.trials.I_AbstractTrial;
import org.adligo.tests4j.models.shared.trials.PackageScope;
import org.adligo.tests4j.models.shared.trials.SourceFileScope;
import org.adligo.tests4j.run.discovery.PackageDiscovery;
import org.adligo.tests4j.run.helpers.ThreadLogMessageBuilder;
import org.adligo.tests4j_4jacoco.plugin.discovery.ClassInstrumenter;
import org.adligo.tests4j_4jacoco.plugin.discovery.I_DiscoveryMemory;

public class TrialInstrumenter {
	private ClassInstrumenter instrumenter;
	private I_Tests4J_Log log;
	private I_DiscoveryMemory memory;
	
	public TrialInstrumenter(ClassInstrumenter pInstrumenter, I_Tests4J_Log pLog, I_DiscoveryMemory pMemory) {
		instrumenter = pInstrumenter;
		log = pLog;
		memory = pMemory;
		instrumenter.setup();
	}
	
	@SuppressWarnings("unchecked")
	public Class<? extends I_AbstractTrial> instrument(Class<? extends I_AbstractTrial> trial) throws ClassNotFoundException, IOException {
		if (log.isLogEnabled(TrialInstrumenter.class)) {
			log.log(ThreadLogMessageBuilder.getThreadForLog() + " instrumenting trial " + trial);
		}
		SourceFileScope sourceScope =  trial.getAnnotation(SourceFileScope.class);
		if (sourceScope != null) {
			Class<?> clazz = sourceScope.sourceClass();
			if ( !memory.isFiltered(clazz)) {
				instrumenter.instrumentClass(clazz);
			}
		}
		PackageScope packageScope = trial.getAnnotation(PackageScope.class);
		if (packageScope != null) {
			String packageName = packageScope.packageName();
			PackageDiscovery pd = new PackageDiscovery(packageName);
			instrumentPackageClasses(pd);
		}
		return (Class<? extends I_AbstractTrial>) instrumenter.instrumentClass(trial);
		
	}

	protected void instrumentPackageClasses(PackageDiscovery pd)
			throws ClassNotFoundException, IOException {
		
		List<String> classes =  pd.getClassNames();
		for (String name: classes) {
			if ( !memory.isFiltered(name)) {
				Class<?> c = Class.forName(name);
				instrumenter.instrumentClass(c);
			}
		}
		List<PackageDiscovery> subs =  pd.getSubPackages();
		for (PackageDiscovery sub: subs) {
			instrumentPackageClasses(sub);
		}
	}
}
