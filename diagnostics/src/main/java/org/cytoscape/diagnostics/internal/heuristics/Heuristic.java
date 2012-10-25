package org.cytoscape.diagnostics.internal.heuristics;

import java.io.IOException;

/**
 * A heuristic that computes a value of type T.
 */
public interface Heuristic<T> {
	/**
	 * Returns the value computed by this heuristic, or null, if this
	 * heuristic cannot compute the value successfully.
	 * @return
	 * @throws IOException
	 */
	T computeValue() throws IOException;
}