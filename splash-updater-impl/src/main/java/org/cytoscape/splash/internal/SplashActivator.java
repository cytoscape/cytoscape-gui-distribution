package org.cytoscape.splash.internal;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

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

import java.util.Properties;

import org.cytoscape.application.events.CyStartListener;
import org.cytoscape.launcher.internal.Launcher;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Meant to be run at startLevel 1 so that the splash screen pops up before all
 * other bundles start loading.  
 */
public final class SplashActivator implements BundleActivator {

    /**
     * Called whenever the OSGi framework starts our bundle
     */
    public void start( BundleContext bc ) throws Exception {
		SplashManipulator splash = new SplashManipulator(bc, Launcher.getSplashPanel());
		bc.addBundleListener(splash);
		Dictionary dictionary = new Hashtable();
		bc.registerService(CyStartListener.class.getName(), splash, dictionary);
    }

    /**
     * Called whenever the OSGi framework stops our bundle
     */
    public void stop( BundleContext bc ) throws Exception {
    }
}

