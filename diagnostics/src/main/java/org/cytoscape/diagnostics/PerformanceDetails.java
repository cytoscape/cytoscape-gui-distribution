package org.cytoscape.diagnostics;

import java.util.Map;
import java.util.Set;

public interface PerformanceDetails {
	long getFrameworkLaunchDuration();
	long getTotalLaunchDuration();
	
	Set<Long> getObservedBundleIds();
	long getBundleLaunchDuration(long bundleId);
	long getBundleLaunchLatency(long bundleId);
	String getBundleDescription(long bundleId);
	Map<String, Long> getAllBundleLaunchDurations();
}
