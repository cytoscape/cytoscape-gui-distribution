package org.cytoscape.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class SimpleApiTest {
	/**
	 * Ensure the exported API packages match the package-list file in our API
	 * docs.
	 */
	@Test
	public void testApiManifest() throws IOException {
		File root = new File(join(File.separator, "target", "dependencies"));
		File appJar = findJar(root, "app-impl-.*.jar");
		File packageFile = new File(root, "package-list");
		
		Set<String> packages = parsePackages(packageFile);
		Set<String> imports = parseManifest(appJar);
		packages.removeAll(imports);
		
		Assert.assertEquals("The following packages are not accessible to simple apps: " + packages.toString(), 0, packages.size());
	}
	
	private File findJar(File root, String regex) {
		Pattern pattern = Pattern.compile(regex);
		for (File file : root.listFiles()) {
			Matcher matcher = pattern.matcher(file.getName());
			if (!matcher.matches()) {
				continue;
			}
			return file;
		}
		return null;
	}

	String join(String delimiter, String...strings) {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (String string : strings) {
			if (first) {
				first = false;
			} else {
				builder.append(delimiter);
			}
			builder.append(string);
		}
		return builder.toString();
	}
	
	Set<String> parsePackages(File path) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(path));
		String line = reader.readLine();
		try {
			Set<String> results = new HashSet<String>();
			while (line != null) {
				results.add(line.trim());
				line = reader.readLine();
			}
			return results;
		} finally {
			reader.close();
		}
	}
	
	Set<String> parseManifest(File path) throws IOException {
		JarFile jarFile = new JarFile(path);
		try {
			Set<String> results = new HashSet<String>();
			Manifest manifest = jarFile.getManifest();
			Attributes attributes = manifest.getMainAttributes();
			String imports = attributes.getValue("Import-Package");
			imports = imports.replaceAll(";version=\".*?\"", "");
			String[] parts = imports.split(",");
			for (String part : parts) {
				if (!part.startsWith("org.cytoscape")) {
					continue;
				}
				results.add(part);
			}
			return results;
		} finally {
			jarFile.close();
		}
	}
}
