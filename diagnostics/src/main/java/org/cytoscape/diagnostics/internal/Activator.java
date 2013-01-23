package org.cytoscape.diagnostics.internal;

/*
 * #%L
 * Cytoscape Diagnostics
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

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
