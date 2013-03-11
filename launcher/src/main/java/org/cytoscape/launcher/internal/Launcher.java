package org.cytoscape.launcher.internal;

/*
 * #%L
 * Cytoscape Launcher
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

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

import org.apache.karaf.main.Main;

public class Launcher {
	private static String[] startupArguments;
	private static long startTime;

	public static void main(String[] args) throws Exception {
		startTime = System.currentTimeMillis();
		startupArguments = args;
		setDefaultSystemProperties();
		createConfigurationDirectory();
		if (isLocked()) {
			System.err.println("Cannot start Cytoscape: Another instance of Cytoscape is already running.");
			System.exit(1);
		}
		Main.main(args);
	}
	
	private static boolean isLocked() throws Exception {
		// Warning: This only works when Karaf is configured to use
		// SimpleFileLock (default).
		String lockPath = join(File.separator, System.getProperty("user.home"), "CytoscapeConfiguration", "3", "lock");

        RandomAccessFile lockFile = new RandomAccessFile(new File(lockPath), "rw");
        try {
	        FileLock lock = lockFile.getChannel().tryLock();
	    	if (lock != null) {
	    		lock.release();
	    		return false;
	    	}
	    	return true;
        } finally {
        	lockFile.close();
        }
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
	
	public static long getStartTime() {
		return startTime;
	}
}
