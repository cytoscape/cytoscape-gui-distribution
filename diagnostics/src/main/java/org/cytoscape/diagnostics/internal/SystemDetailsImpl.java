package org.cytoscape.diagnostics.internal;

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
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.diagnostics.SystemDetails;
import org.cytoscape.diagnostics.internal.heuristics.Heuristic;
import org.cytoscape.diagnostics.internal.heuristics.ProcHeuristic;
import org.cytoscape.diagnostics.internal.heuristics.Sw_VersHeuristic;
import org.cytoscape.diagnostics.internal.heuristics.SysctlHeuristic;
import org.cytoscape.diagnostics.internal.heuristics.UnameHeuristic;
import org.cytoscape.diagnostics.internal.heuristics.Vm_StatHeuristic;
import org.cytoscape.diagnostics.internal.heuristics.WmicHeuristic;

public class SystemDetailsImpl implements SystemDetails {
	Map<Class<? extends Heuristic<?>>, Heuristic<?>> singletonCache;
	
	public SystemDetailsImpl() {
		singletonCache = new HashMap<Class<? extends Heuristic<?>>, Heuristic<?>>();
	}
	
	public static void main(String[] args) {
		SystemDetails details = new SystemDetailsImpl();
		System.out.println(details.getOSName());
		System.out.println(details.getProcessorName());
		System.out.println(details.getTotalMemory());
		System.out.println(details.getFreeMemory());
		System.out.println(details.getTotalSwap());
		System.out.println(details.getFreeSwap());
	}

	@Override
	@SuppressWarnings("unchecked")
	public Long getFreeSwap() {
		return tryHeuristics(
			WmicHeuristic.FreeSwap.class,
			SysctlHeuristic.FreeSwap.class,
			ProcHeuristic.FreeSwap.class
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Long getTotalSwap() {
		return tryHeuristics(
			WmicHeuristic.TotalSwap.class,
			SysctlHeuristic.TotalSwap.class,
			ProcHeuristic.TotalSwap.class
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Long getFreeMemory() {
		return tryHeuristics(
			WmicHeuristic.FreeMemory.class,
			Vm_StatHeuristic.FreeMemory.class,
			ProcHeuristic.FreeMemory.class
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Long getTotalMemory() {
		return tryHeuristics(
			WmicHeuristic.TotalMemory.class,
			SysctlHeuristic.TotalMemory.class,
			ProcHeuristic.TotalMemory.class
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public String getProcessorName() {
		return tryHeuristics(
			WmicHeuristic.ProcessorName.class,
			SysctlHeuristic.ProcessorName.class,
			ProcHeuristic.ProcessorName.class
		);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public String getOSName() {
		return tryHeuristics(
			WmicHeuristic.OSName.class,
			Sw_VersHeuristic.class,
			UnameHeuristic.class
		);
	}
	
	@SuppressWarnings("unchecked")
	private <T> T tryHeuristics(Class<? extends Heuristic<T>>...heuristicTypes) {
		for (Class<? extends Heuristic<T>> type : heuristicTypes) {
			try {
				// Lazily instantiate singletons
				Heuristic<T> heuristic = (Heuristic<T>) singletonCache.get(type);
				if (heuristic == null) {
					try {
						heuristic = type.newInstance();
					} catch (InstantiationException e) {
						throw new RuntimeException(e);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
					singletonCache.put(type, heuristic);
				}
				
				T value = heuristic.computeValue();
				if (value != null) {
					return value;
				}
			} catch (IOException e) {
			}
		}
		return null;
	}
}
