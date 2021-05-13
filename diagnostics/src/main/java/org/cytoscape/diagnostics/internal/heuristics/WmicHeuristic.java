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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Heuristics for obtaining information via the "wmic" command
 * typically found on Windows systems since Windows 2000.
 */
public abstract class WmicHeuristic<T> extends AbstractHeuristic<T> {
	protected String getValue(String alias, String key) throws IOException {
		if (!commandExists("wmic")) {
			return null;
		}
		String output = runAndGetOutput("wmic", alias, "get", key);
		String[] lines = output.split("\n");
		if (lines.length < 3) {
			return null;
		}
		return lines[2];
	}
	
	protected Map<String, String> getValues(String alias, String...keys) throws IOException {
		if (!commandExists("wmic")) {
			return Collections.emptyMap();
		}
		
		List<String> arguments = new ArrayList<String>();
		arguments.add("wmic");
		arguments.add(alias);
		
		if (keys.length > 0) {
			arguments.add("get");
			arguments.add(join(",", keys));
		}
		
		Map<String, String> result = new HashMap<String, String>();
		String output = runAndGetOutput(arguments.toArray(new String[arguments.size()]));
		if (output == null) {
			return Collections.emptyMap();
		}
		
		String[] lines = output.split("\n");
		if (lines.length < 3) {
			return Collections.emptyMap();
		}
		
		String[] keyNames = lines[0].split("\t");
		String[] values = lines[2].split("\t");
		
		for (int i = 0; i < keyNames.length; i++) {
			result.put(keyNames[i], values[i]);
		}
		return result;
	}
	
	protected Long parseMemory(String data) {
		Long value = convert(data, Long.class);
		if (value == null) {
			return null;
		}
		return value * 1024;
	}
	
	public static class ProcessorName extends WmicHeuristic<String> {
		@Override
		public String computeValue() throws IOException {
			return getValue("cpu", "name");
		}
	}

	public static class TotalMemory extends WmicHeuristic<Long> {
		@Override
		public Long computeValue() throws IOException {
			return parseMemory(getValue("os", "TotalVisibleMemorySize"));
		}
	}
	
	public static class FreeMemory extends WmicHeuristic<Long> {
		@Override
		public Long computeValue() throws IOException {
			return parseMemory(getValue("os", "FreePhysicalMemory"));
		}
	}
	
	public static class TotalSwap extends WmicHeuristic<Long> {
		@Override
		public Long computeValue() throws IOException {
			return parseMemory(getValue("os", "TotalVirtualMemorySize"));
		}
	}

	public static class FreeSwap extends WmicHeuristic<Long> {
		@Override
		public Long computeValue() throws IOException {
			return parseMemory(getValue("os", "FreeVirtualMemory"));
		}
	}
	
	public static class OSName extends WmicHeuristic<String> {
		@Override
		public String computeValue() throws IOException {
			return getValue("os", "caption");
		}
	}

}

