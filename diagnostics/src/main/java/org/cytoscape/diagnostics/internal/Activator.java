package org.cytoscape.diagnostics.internal;

import java.util.Properties;

import org.cytoscape.diagnostics.PerformanceDetails;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.startlevel.StartLevel;

public class Activator implements BundleActivator {

	static final int INITIAL_START_LEVEL = 200;
	
	@Override
	public void start(BundleContext context) throws Exception {
		applyStartLevelHack(context);
		
		PerformanceDetailsBuilder details = new PerformanceDetailsBuilder();
		context.registerService(PerformanceDetails.class.getName(), details, new Properties());
		
		PerformanceTracker performanceTracker = new PerformanceTracker(context, details);
		context.addFrameworkListener(performanceTracker);
		context.addBundleListener(performanceTracker);
	}
	
    private void applyStartLevelHack(BundleContext context) {
    	// See ticket #1494.  This hack needs to remain in place until Karaf
    	// is patched.
    	ServiceReference reference = context.getServiceReference(StartLevel.class.getName());
    	StartLevel level = (StartLevel) context.getService(reference);
    	level.setStartLevel(INITIAL_START_LEVEL);
    	context.ungetService(reference);
	}
    
    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
