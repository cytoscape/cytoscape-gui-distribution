package org.cytoscape.cmdline.internal;
import java.util.Properties;

import org.cytoscape.cmdline.CommandLineArgs;
import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;


public class CyActivator extends AbstractCyActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		Properties properties = new Properties();
		registerService(context, new CommandLineArgsImpl(), CommandLineArgs.class, properties);
	}

}
