package org.cytoscape.launcher.internal;

import org.apache.karaf.main.Main;

public class Launcher {
	private static String[] startupArguments;

	public static void main(String[] args) throws Exception {
		startupArguments = args;
		Main.main(args);
	}
	
	public static String[] getStartupArguments() {
		String[] result = new String[startupArguments.length];
		System.arraycopy(startupArguments, 0, result, 0, startupArguments.length);
		return result;
	}
}
