package org.cytoscape.splash.internal;

/*
 * #%L
 * Splash Updater
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

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.application.events.CyStartEvent;
import org.cytoscape.application.events.CyStartListener;
import org.cytoscape.launcher.internal.SplashPanel;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;

public class SplashManipulator implements
	BundleListener,
	FrameworkListener,
	CyStartListener {

	private Set<Long> resolved;
	private Set<Long> started;
	private BundleContext context;
	private SplashPanel splashPanel;

    public SplashManipulator(BundleContext context, SplashPanel splashPanel) {
    	this.context = context;
    	resolved = new HashSet<Long>();
    	started = new HashSet<Long>();
    	
    	for (Bundle bundle : context.getBundles()) {
    		long id = bundle.getBundleId();
    		resolved.add(id);
    		if (bundle.getState() == Bundle.ACTIVE)
    			started.add(id);
    	}

    	this.splashPanel = splashPanel;
	}

	public void bundleChanged(BundleEvent event) {
		if ( event.getType() == BundleEvent.RESOLVED )
			resolved.add(event.getBundle().getBundleId());
		
		if ( event.getType() == BundleEvent.STARTED ) {
			started.add(event.getBundle().getBundleId());
			splashPanel.updateMessage(event.getBundle().getSymbolicName() + " started", getProgress());
		}
	}

    public void frameworkEvent(FrameworkEvent event) {
		if ( event.getType() == FrameworkEvent.STARTED ) { 
			splashPanel.updateMessage("OSGi finished.", getProgress());
			context.removeBundleListener(this);
			context.removeFrameworkListener(this);
			resolved.clear();
			started.clear();
		}
	}

    double getProgress() {
        int totalResolved = resolved.size();
        int totalStarted = started.size();
        return totalResolved == 0 ? 0 : totalStarted / (double) totalResolved;
    }
    
    @Override
    public void handleEvent(CyStartEvent e) {
    	splashPanel.close();
    }
}
