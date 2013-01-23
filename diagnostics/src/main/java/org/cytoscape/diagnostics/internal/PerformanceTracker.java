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
	    		finishedStarting = true;
	    	}
	    	context.ungetService(reference);
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
