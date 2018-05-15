package com.github.jmartty;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonDiffTest {

	@Test
	public void testJsonDiff() throws IOException {
		final File[] testFiles = testFiles();
		System.out.println("Running " + testFiles.length + " tests.");
		for (File file : testFiles) {
			String expected = expectedFromResultFile(file);
			String actual = actualFromResultFile(file);
			String result = resultFromResultFile(file);
			System.out.print("Test: '" + testNameFromResultFile(file) + "' ... ");
			test(result, parser.parse(expected), parser.parse(actual));
		}
	}

	private String testNameFromResultFile(File file) {
		return file.getName().replaceAll("\\.result$", "");
	}

	public String baseFileFromResultFile(File file) {
		return file.getPath().replaceAll("\\.result$", "");
	}

	private String resultFromResultFile(File file) throws IOException {
		return FileUtils.readFileToString(file, ENCODING);
	}

	private String actualFromResultFile(File file) throws IOException {
		return FileUtils.readFileToString(new File(baseFileFromResultFile(file) + ".actual.json"), ENCODING);
	}

	private String expectedFromResultFile(File file) throws IOException {
		return FileUtils.readFileToString(new File(baseFileFromResultFile(file) + ".expected.json"), ENCODING);
	}

	private void test(String expectedOutput, JsonElement expectedJson, JsonElement actualJson) {
		StringWriter sw = new StringWriter();
		JsonDiff.diff(sw, expectedJson, actualJson);
		Assert.assertEquals(expectedOutput.trim(), sw.toString().trim());
		System.out.println("Ok!");
	}

	private static File[] testFiles() throws IOException {
		File dir = new File("src/test/resources");
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.matches(".*\\.result$");
			}
		});
		return files;
	}

	private static JsonParser parser = new JsonParser();
	private static final String ENCODING = "UTF-8";
}
