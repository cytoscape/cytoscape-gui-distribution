package org.cytoscape.diagnostics.internal;

/*
 * #%L
 * Cytoscape Diagnostics
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.cytoscape.diagnostics.PerformanceDetails;
import org.osgi.framework.Bundle;

public class PerformanceDetailsBuilder implements PerformanceDetails {

	private long systemLaunchStartTime;
	private long systemLaunchEndTime;
	private long frameworkLaunchEndTime;
	
	private Map<Long, String> bundleNames;
	private Map<Long, Long> bundleActivationStartTimes;
	private Map<Long, Long> bundleActivationEndTimes;
	
	public PerformanceDetailsBuilder() {
		bundleNames = new HashMap<Long, String>();
		bundleActivationStartTimes = new HashMap<Long, Long>();
		bundleActivationEndTimes = new HashMap<Long, Long>();
	}
	
	@Override
	public long getFrameworkLaunchDuration() {
		return frameworkLaunchEndTime - systemLaunchStartTime;
	}
	
	@Override
	public long getTotalLaunchDuration() {
		return systemLaunchEndTime - systemLaunchStartTime;
	}

	@Override
	public long getBundleLaunchDuration(long bundleId) {
		Long startTime = bundleActivationStartTimes.get(bundleId);
		Long endTime = bundleActivationEndTimes.get(bundleId);
		if (startTime == null || endTime == null) {
			return -1;
		}
		return endTime - startTime;
	}
	
	@Override
	public Set<Long> getObservedBundleIds() {
		Set<Long> bundleIds = new HashSet<Long>(bundleActivationStartTimes.keySet());
		bundleIds.addAll(bundleActivationEndTimes.keySet());
		return bundleIds;
	}
	
	@Override
	public Map<String, Long> getAllBundleLaunchDurations() {
		Map<String, Long> durations = new HashMap<String, Long>();
		for (Entry<Long, Long> entry : bundleActivationStartTimes.entrySet()) {
			Long bundleId = entry.getKey();
			Long endTime = bundleActivationEndTimes.get(bundleId);
			if (endTime == null) {
				continue;
			}
			durations.put(bundleNames.get(bundleId), endTime - entry.getValue());
		}
		return durations;
	}

	@Override
	public long getBundleLaunchLatency(long bundleId) {
		Long startTime = bundleActivationStartTimes.get(bundleId);
		if (startTime == null) {
			return -1;
		}
		return startTime - systemLaunchStartTime;
	}

	@Override
	public String getBundleDescription(long bundleId) {
		return bundleNames.get(bundleId);
	}
	
	void setSystemLaunchStartTime(long time) {
		systemLaunchStartTime = time;
	}
	
	void setSystemLaunchEndTime(long time) {
		systemLaunchEndTime = time;
	}
	
	void setFrameworkLaunchEndTime(long time) {
		frameworkLaunchEndTime = time;
	}
	
	void logBundleStarting(Bundle bundle) {
		logBundleName(bundle);
		
		// Only log the first activation
		Long startTime = bundleActivationStartTimes.get(bundle.getBundleId());
		if (startTime != null) {
			return;
		}
		bundleActivationStartTimes.put(bundle.getBundleId(), System.currentTimeMillis());
	}

	private void logBundleName(Bundle bundle) {
		String name = bundleNames.get(bundle.getBundleId());
		if (name == null) {
			name = String.format("%s (%s)", bundle.getSymbolicName(), bundle.getVersion().toString());
			bundleNames.put(bundle.getBundleId(), name);
		}
	}

	void logBundleStarted(Bundle bundle) {
		logBundleName(bundle);
		
		// Only log the first activation
		Long endTime = bundleActivationEndTimes.get(bundle.getBundleId());
		if (endTime != null) {
			return;
		}
		bundleActivationEndTimes.put(bundle.getBundleId(), System.currentTimeMillis());
	}
}
