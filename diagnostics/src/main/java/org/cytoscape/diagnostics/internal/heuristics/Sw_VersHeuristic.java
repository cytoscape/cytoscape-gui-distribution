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

/**
 * Heuristics for obtaining information via the "sw_vers" command
 * typically found on Mac OS X.
 */
public class Sw_VersHeuristic extends AbstractHeuristic<String> {
	@Override
	public String computeValue() throws IOException {
		if (!commandExists("sw_vers")) {
			return null;
		}
		
		String output = runAndGetOutput("sw_vers");
		
		StringBuilder builder = new StringBuilder();
		boolean firstLine = true;
		for (String line : output.split("\n")) {
			String[] parts = line.split("[:]", 2);
			if (parts.length < 2) {
				continue;
			}
			if (!firstLine) {
				builder.append(" ");
			} else {
				firstLine = false;
			}
			builder.append(parts[1].trim());
		}
		String result = builder.toString().trim();
		if (result.isEmpty()) {
			return null;
		}
		return result;
	}
}
