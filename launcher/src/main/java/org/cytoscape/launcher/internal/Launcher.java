package org.cytoscape.launcher.internal;

import java.io.File;

import org.apache.karaf.main.Main;

public class Launcher {
	private static String[] startupArguments;

	public static void main(String[] args) throws Exception {
		startupArguments = args;
		createConfigurationDirectory();
		Main.main(args);
	}
	
	private static void createConfigurationDirectory() {
		String userHome = System.getProperty("user.home");
		File karafData = new File(userHome, String.format("%s%s%s", "CytoscapeConfiguration", File.separator, "3"));
		karafData.mkdirs();
	}

	public static String[] getStartupArguments() {
		String[] result = new String[startupArguments.length];
		System.arraycopy(startupArguments, 0, result, 0, startupArguments.length);
		return result;
	}
}
