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

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.karaf.main.Main;

public class Launcher {
	public static String[] startupArguments;
	private static long startTime;
	private static SplashPanel splashPanel;
	
	public static void main(String[] args) throws Exception {
		startTime = System.currentTimeMillis();
		startupArguments = args;
		
		showSplashPanel();
		
		// Intercept session file double-clicked on Mac OS, passed by file association set by install4j
		if (isMac()) {
		    MacHelper.handleStartupArguments();
		}
				
		setDefaultSystemProperties();
		createConfigurationDirectory();
		if (isLocked()) {
			// The main data directory is locked.  We should create a new one.
			final String dataPath = String.format("%s.%d", System.getProperty("karaf.data"), System.currentTimeMillis());
			System.setProperty("karaf.data", dataPath);

			// Delete this on shutdown, otherwise it uses up a lot of space.
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					File root = new File(dataPath);
					deleteDirectory(root);
				}
			});
		}
		Main.main(args);
	}
	
	private static void showSplashPanel() throws IOException {
		File karafBase = new File(System.getProperty("karaf.base"));
		BufferedImage background = ImageIO.read(new File(karafBase, "CytoscapeSplashScreen.png"));
		splashPanel = new SplashPanel(background);
		
		final JFrame frame = new JFrame();
		frame.setAlwaysOnTop(true);
		frame.add(splashPanel);
		frame.setUndecorated(true);
		
		int width = background.getWidth();
		int height = background.getHeight();
		frame.setSize(width, height);
		
		// Center the frame in the current screen.
		Rectangle bounds = frame.getGraphicsConfiguration().getBounds();
		frame.setLocation((bounds.width - width) / 2, (bounds.height - height) / 2);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				frame.setVisible(true);
			}
		});
	}

	/**
	 * Recursively deletes a directory.  This method resolves and follows
	 * symlinks, so use with caution!
	 */
	private static void deleteDirectory(File root) {
		LinkedList<File> pathStack = new LinkedList<File>();
		pathStack.push(root);
		
		// Track directories we've seen so we can detect cycles and
		// stubborn files.
		Set<File> seen = new HashSet<File>();
		
		while (!pathStack.isEmpty()) {
			File directory = pathStack.pop();
			File[] files = directory.listFiles();
			if (files.length == 0) {
				seen.add(directory);
				directory.delete();
			} else {
				if (seen.contains(directory)) {
					System.err.printf("Couldn't delete bundle cache: %s\n", directory.getAbsolutePath());
					 continue;
				}
				seen.add(directory);
				
				// Re-add the directory because we want to delete it after we
				// delete its contents. 
				pathStack.push(directory);
				
				for (File file : files) {
					if (file.isDirectory()) {
						pathStack.push(file);
					} else if (file.isFile()) {
						file.delete();
					}
				}
			}
		}
	}
	
	private static boolean isLocked() throws Exception {
		// Warning: This only works when Karaf is configured to use
		// SimpleFileLock (default).
		String lockPath = join(File.separator, System.getProperty("karaf.data"), "lock");

		try {
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
		} catch (FileNotFoundException e) {
			return false;
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
		
		File karafTmp = new File(karafData, "tmp");
		karafTmp.mkdirs();
	}

	public static String[] getStartupArguments() {
		String[] result = new String[startupArguments.length];
		System.arraycopy(startupArguments, 0, result, 0, startupArguments.length);
		return result;
	}
	
	public static long getStartTime() {
		return startTime;
	}
	
	private static boolean isMac(){
		return System.getProperty("os.name").startsWith("Mac OS X");
	}

	public static SplashPanel getSplashPanel() {
		return splashPanel;
	}
}
