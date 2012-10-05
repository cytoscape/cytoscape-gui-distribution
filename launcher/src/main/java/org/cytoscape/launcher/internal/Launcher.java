package org.cytoscape.launcher.internal;

import java.io.File;

import org.apache.karaf.main.Main;

public class Launcher {
	private static String[] startupArguments;

	public static void main(String[] args) throws Exception {
		startupArguments = args;
		setDefaultSystemProperties();
		createConfigurationDirectory();
		Main.main(args);
	}
	
	private static void setDefaultSystemProperties() {
		String userHome = System.getProperty("user.home");
		if (System.getProperty("karaf.data") == null) {
			System.setProperty("karaf.data", join(File.separator, userHome, "CytoscapeConfiguration", "3", "karaf_data"));
		}
	}

	private static String join(String separator, String... parts) {
		StringBuilder builder = new StringBuilder();
		boolean isFirst = true;
		for (String part : parts) {
			if (!isFirst) {
				builder.append(separator);
			} else {
				isFirst = false;
			}
			builder.append(part);
		}
		return builder.toString();
	}
	
	private static void createConfigurationDirectory() {
		String userHome = System.getProperty("user.home");
		File karafData = new File(join(File.separator, userHome, "CytoscapeConfiguration", "3"));
		karafData.mkdirs();
	}

	public static String[] getStartupArguments() {
		String[] result = new String[startupArguments.length];
		System.arraycopy(startupArguments, 0, result, 0, startupArguments.length);
		return result;
	}
}
