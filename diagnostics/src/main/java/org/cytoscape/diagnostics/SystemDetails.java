package org.cytoscape.diagnostics;

public interface SystemDetails {

	Long getFreeSwap();

	Long getTotalSwap();

	Long getFreeMemory();

	Long getTotalMemory();

	String getProcessorName();

	String getOSName();

}
