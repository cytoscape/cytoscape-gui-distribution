package org.cytoscape.diagnostics.internal.heuristics;

import java.io.IOException;

/**
 * Heuristics for obtaining information via the "uname" command
 * typically found on POSIX systems.
 */
public class UnameHeuristic extends AbstractHeuristic<String> {
	@Override
	public String computeValue() throws IOException {
		if (!commandExists("uname")) {
			return null;
		}
		
		return runAndGetOutput("uname", "-a");
	}
}
