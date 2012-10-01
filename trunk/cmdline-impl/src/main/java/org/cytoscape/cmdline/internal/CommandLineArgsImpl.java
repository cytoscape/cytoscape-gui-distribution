package org.cytoscape.cmdline.internal;

import org.cytoscape.cmdline.CommandLineArgs;
import org.cytoscape.launcher.internal.Launcher;

public class CommandLineArgsImpl implements CommandLineArgs {
	@Override
	public String[] getArgs() {
		return Launcher.getStartupArguments();
	}
}
