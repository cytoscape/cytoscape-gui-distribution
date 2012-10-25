package org.cytoscape.diagnostics.internal.heuristics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Heuristics for obtaining information via the "/proc"
 * pseudo-filesystem on Linux systems.
 */
public abstract class ProcHeuristic<T> extends AbstractHeuristic<T> {
	static final Pattern CPUINFO_MODEL_NAME_PATTERN = Pattern.compile("model name\\s*[:]\\s+(.*)\\s*");
	
	static final Pattern MEMINFO_MEMTOTAL_PATTERN = Pattern.compile("MemTotal:\\s+(.*)\\s*");
	static final Pattern MEMINFO_MEMFREE_PATTERN = Pattern.compile("MemFree:\\s+(.*)\\s*");
	static final Pattern MEMINFO_SWAPTOTAL_PATTERN = Pattern.compile("SwapTotal:\\s+(.*)\\s*");
	static final Pattern MEMINFO_SWAPFREE_PATTERN = Pattern.compile("SwapFree:\\s+(.*)\\s*");

	protected Long parseMemTotal(String value) {
		if (value == null) {
			return null;
		}
		String[] values = value.split(" ");
		long baseValue = Long.parseLong(values[0]);
		if (values.length > 1) {
			if ("kB".equals(values[1])) {
				baseValue *= 1024;
			}
		}
		return baseValue;
	}

	protected <S> S getValueFromProc(String fileName, Pattern pattern, Class<S> type) throws IOException {
		File file = new File("/proc/" + fileName);
		if (!file.exists()) {
			return null;
		}
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			String line = reader.readLine();
			while (line != null) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.matches()) {
					return convert(matcher.group(1), type);
				}
				line = reader.readLine();
			}
			return null;
		} finally {
			reader.close();
		}		
	}

	public static class TotalMemory extends ProcHeuristic<Long> {
		@Override
		public Long computeValue() throws IOException {
			return parseMemTotal(getValueFromProc("meminfo", MEMINFO_MEMTOTAL_PATTERN, String.class));
		}
	}
	
	public static class FreeMemory extends ProcHeuristic<Long> {
		@Override
		public Long computeValue() throws IOException {
			return parseMemTotal(getValueFromProc("meminfo", MEMINFO_MEMFREE_PATTERN, String.class));
		}
	}
	
	public static class ProcessorName extends ProcHeuristic<String> {
		@Override
		public String computeValue() throws IOException {
			return getValueFromProc("cpuinfo", CPUINFO_MODEL_NAME_PATTERN, String.class);
		}
	}
	
	public static class TotalSwap extends ProcHeuristic<Long> {
		@Override
		public Long computeValue() throws IOException {
			return parseMemTotal(getValueFromProc("meminfo", MEMINFO_SWAPTOTAL_PATTERN, String.class));
		}
	}

	public static class FreeSwap extends ProcHeuristic<Long> {
		@Override
		public Long computeValue() throws IOException {
			return parseMemTotal(getValueFromProc("meminfo", MEMINFO_SWAPFREE_PATTERN, String.class));
		}
	}
}
