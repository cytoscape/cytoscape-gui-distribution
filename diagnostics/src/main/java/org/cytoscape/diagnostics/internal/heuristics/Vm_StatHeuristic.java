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
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Heuristics for obtaining information via the "vm_stat" command
 * typically found on BSD systems running the Mach kernel (e.g. Mac OS X).
 */
public abstract class Vm_StatHeuristic<T> extends AbstractHeuristic<T> {
	static final Pattern VMSTAT_PAGE_SIZE_PATTERN = Pattern.compile(".*\\(page size of (\\d+) bytes\\).*");
	static final Pattern VMSTAT_ENTRY_PATTERN = Pattern.compile("([^:]+):\\s+(\\d+)[.]");
	
	protected Map<String, Long> getValuesFromVmstat() throws IOException {
		if (!commandExists("vm_stat")) {
			return Collections.emptyMap();
		}
		
		Map<String, Long> values = new HashMap<String, Long>();

		Process process = new ProcessBuilder("vm_stat").start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		try {
			String line = reader.readLine();
			while (line != null) {
				try {
					Matcher matcher = VMSTAT_PAGE_SIZE_PATTERN.matcher(line);
					if (matcher.matches()) {
						Long pageSize = convert(matcher.group(1), Long.class);
						values.put("page size", pageSize);
						continue;
					}
					matcher = VMSTAT_ENTRY_PATTERN.matcher(line);
					if (matcher.matches()) {
						String key = matcher.group(1);
						Long value = convert(matcher.group(2), Long.class);
						values.put(key,  value);
						continue;
					}
				} finally {
					line = reader.readLine();
				}
			}
			return values;
		} finally {
			reader.close();
		}		
	}
	
	public static class FreeMemory extends Vm_StatHeuristic<Long> {
		public Long computeValue() throws IOException {
			Map<String, Long> values = getValuesFromVmstat();
			Long pageSize = values.get("page size");
			if (pageSize == null) {
				return null;
			}
			
			Long freeMemory1 = values.get("Pages free");
			Long freeMemory2 = values.get("Pages speculative");
			if (freeMemory1 == null || freeMemory2 == null) {
				return null;
			}
			return (freeMemory1 + freeMemory2) * pageSize;
		}
	}
}
