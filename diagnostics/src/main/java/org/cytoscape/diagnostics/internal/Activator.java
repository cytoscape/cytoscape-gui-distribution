package org.cytoscape.diagnostics.internal;

import java.util.Properties;

import org.cytoscape.diagnostics.PerformanceDetails;
import org.cytoscape.diagnostics.SystemDetails;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.startlevel.StartLevel;

public class Activator implements BundleActivator {

	static final int INITIAL_START_LEVEL = 200;
	
	@Override
	public void start(BundleContext context) throws Exception {
		applyStartLevelHack(context);
		
		PerformanceDetailsBuilder performanceDetails = new PerformanceDetailsBuilder();
		context.registerService(PerformanceDetails.class.getName(), performanceDetails, new Properties());
		
		PerformanceTracker performanceTracker = new PerformanceTracker(context, performanceDetails);
		context.addFrameworkListener(performanceTracker);
		context.addBundleListener(performanceTracker);
		
		SystemDetailsImpl systemDetails = new SystemDetailsImpl();
		context.registerService(SystemDetails.class.getName(), systemDetails, new Properties());
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
