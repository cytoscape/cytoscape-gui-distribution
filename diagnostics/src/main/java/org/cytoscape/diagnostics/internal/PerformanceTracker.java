package org.cytoscape.diagnostics.internal;

import org.cytoscape.launcher.internal.Launcher;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.service.startlevel.StartLevel;

public class PerformanceTracker implements FrameworkListener, SynchronousBundleListener {
	private BundleContext context;
	private PerformanceDetailsBuilder performanceDetails;
	private boolean finishedStarting;

	public PerformanceTracker(BundleContext context, PerformanceDetailsBuilder performanceDetails) {
		this.context = context;
		this.performanceDetails = performanceDetails;
		
		performanceDetails.setSystemLaunchStartTime(Launcher.getStartTime());
		performanceDetails.setFrameworkLaunchEndTime(System.currentTimeMillis());
	}
	
	@Override
	public void frameworkEvent(FrameworkEvent event) {
		if (event.getType() == FrameworkEvent.STARTLEVEL_CHANGED && !finishedStarting) {
			ServiceReference reference = context.getServiceReference(StartLevel.class.getName());
	    	StartLevel level = (StartLevel) context.getService(reference);
	    	if ( level.getStartLevel() == Activator.INITIAL_START_LEVEL ) {
	    		performanceDetails.setSystemLaunchEndTime(System.currentTimeMillis());
	    		dump();
	    		finishedStarting = true;
	    	}
	    	context.ungetService(reference);
		}
	}
	
	private void dump() {
		for (Long id : performanceDetails.getObservedBundleIds()) {
			String description = performanceDetails.getBundleDescription(id);
			long latency = performanceDetails.getBundleLaunchLatency(id);
			long duration = performanceDetails.getBundleLaunchDuration(id);
			System.out.printf("%s\t%d\t%d\n", description, latency, duration);
		}
	}

	@Override
	public void bundleChanged(BundleEvent event) {
		switch (event.getType()) {
		case BundleEvent.STARTING:
			performanceDetails.logBundleStarting(event.getBundle());
			break;
		case BundleEvent.STARTED:
			performanceDetails.logBundleStarted(event.getBundle());
			break;
		}
	}
}
