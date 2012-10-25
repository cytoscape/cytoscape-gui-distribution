package org.cytoscape.diagnostics.internal.heuristics;

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
