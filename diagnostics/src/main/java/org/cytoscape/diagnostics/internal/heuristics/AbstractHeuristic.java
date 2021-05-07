package org.cytoscape.diagnostics.internal.heuristics;

/*
 * #%L
 * Cytoscape Diagnostics
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class AbstractHeuristic<T> implements Heuristic<T> {
	static boolean isWindows = isWindows();
	
	private static boolean isWindows() {
		return System.getProperty("os.name").startsWith("Windows");
	}

	protected boolean commandExists(String commandName) throws IOException {
		String command = isWindows ? "where" : "which";
		Process process = createProcess(command, commandName);
		if (isWindows) {
			process.getInputStream().close();
		}
		try {
			return process.waitFor() == 0;
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}
	
	
	protected String runAndGetOutput(String...arguments) throws IOException {
		Process process = createProcess(arguments);
		InputStream stream = process.getInputStream();
		String result;
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		try {
			StringWriter writer = new StringWriter();
			String line = reader.readLine();
			
			boolean firstLine = true;
			while (line != null) {
				if (!firstLine) {
					writer.write("\n");
				} else {
					firstLine = false;
				}
				writer.write(line);
				line = reader.readLine();
			}
			result = writer.toString();
			if (result != null && result.isEmpty()) {
				return null;
			}
			try {
				if (process.waitFor() != 0) {
					return null;
				}
			} catch (InterruptedException e) {
				throw new IOException(e);
			}
			if (process.exitValue() != 0) {
				return null;
			}
			return result;
		} finally {
			reader.close();
		}
	}

	private Process createProcess(String...arguments) throws IOException {
		ProcessBuilder builder = new ProcessBuilder(arguments);
		Process process = builder.start();
		if (isWindows) {
			process.getErrorStream().close();
			process.getOutputStream().close();
		}
		return process;
	}


	@SuppressWarnings("unchecked")
	protected <S> S convert(String value, Class<S> type) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		
		if (type.equals(String.class)) {
			return (S) value;
		}
		try {
			Method method = type.getMethod("valueOf", String.class);
			return (S) method.invoke(null, value.trim());
		} catch (SecurityException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}
	

	protected String join(String delimiter, String...items) {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		
		for (String item : items) {
			if (!first) {
				builder.append(delimiter);
			} else {
				first = false;
			}
			builder.append(item);
		}
		return builder.toString();
	}
}
