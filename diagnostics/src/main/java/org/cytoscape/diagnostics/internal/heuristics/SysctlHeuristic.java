package org.cytoscape.diagnostics.internal.heuristics;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Heuristics for obtaining information via the "sysctl" command
 * typically found on POSIX systems.
 */
public abstract class SysctlHeuristic<T> extends AbstractHeuristic<T> {
	static final Pattern VM_SWAPUSAGE_PATTERN = Pattern.compile(".*total\\s*=\\s*([0-9.]+)M\\s*used\\s*=\\s*([0-9.]+)M\\s*free\\s*=\\s*([0-9.]+)M.*");
	
	public static class ProcessorName extends SysctlHeuristic<String> {
		@Override
		public String computeValue() throws IOException {
			if (!commandExists("sysctl")) {
				return null;
			}
			
			return runAndGetOutput("sysctl", "-n", "machdep.cpu.brand_string");
		}
	}
	
	protected Long parseVmSwapUsage(String data, int field) {
		Matcher matcher = VM_SWAPUSAGE_PATTERN.matcher(data);
		if (!matcher.matches()) {
			return null;
		}
		double megaBytes = Double.parseDouble(matcher.group(field));
		return (long) (megaBytes * 1048576);
	}
	
	protected Long getVmSwapUsage(int field) throws IOException {
		if (!commandExists("sysctl")) {
			return null;
		}
		
		String output = runAndGetOutput("sysctl", "-n", "vm.swapusage");
		if (output == null) {
			return null;
		}
		
		return parseVmSwapUsage(output, field);
	}

	public static class TotalMemory extends SysctlHeuristic<Long> {
		@Override
		public Long computeValue() throws IOException {
			if (!commandExists("sysctl")) {
				return null;
			}
			
			String output = runAndGetOutput("sysctl", "-n", "hw.memsize");
			if (output == null) {
				return null;
			}
			return Long.parseLong(output);
		}
	}
	
	public static class FreeSwap extends SysctlHeuristic<Long> {
		public Long computeValue() throws IOException {
			return getVmSwapUsage(3);
		}
	}

	public static class TotalSwap extends SysctlHeuristic<Long> {
		public Long computeValue() throws IOException {
			return getVmSwapUsage(1);
		}
	}
}
