package com.github.jmartty;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonDiff {

	private final Writer out;

	public JsonDiff() {
		this.out = new PrintWriter(System.out);
	}

	public JsonDiff(Writer writer) {
		this.out = writer;
	}

	static boolean diff(JsonElement expected, JsonElement actual) {
		return (new JsonDiff()).diff(expected, actual, new JsonContext());
	}

	static boolean diff(Writer writer, JsonElement expected, JsonElement actual) {
		return (new JsonDiff(writer)).diff(expected, actual, new JsonContext());
	}

	private boolean diff(JsonElement expected, JsonElement actual, JsonContext context, String previousContextElement) {
		return diff(expected, actual, new JsonContext(context, previousContextElement));
	}

	private boolean diff(JsonElement expected, JsonElement actual, JsonContext context) {
		String expectedClass = expected.getClass().getSimpleName();
		String actualClass = actual.getClass().getSimpleName();
		if (expectedClass.equals(actualClass)) {
			if (expected.isJsonPrimitive()) {
				if (!expected.getAsJsonPrimitive().equals(actual.getAsJsonPrimitive())) {
					log(context, "JsonPrimitive", expected, actual);
					return true;
				} else {
					return false;
				}
			} else if (expected.isJsonArray()) {
				JsonArray expectedArray = expected.getAsJsonArray();
				JsonArray actualArray = actual.getAsJsonArray();
				if (actualArray.size() != expectedArray.size()) {
					log(context, "JsonArrays of different size", actualArray, expectedArray);
					return true;
				} else {
					boolean hasDiff = false;
					for (int i = 0; i < expectedArray.size(); i++) {
						JsonElement expectedElement = expectedArray.get(i);
						JsonElement actualElement = actualArray.get(i);
						hasDiff = hasDiff || diff(expectedElement, actualElement, context, "Array[" + i + "]");
					}
					return hasDiff;
				}
			} else if (expected.isJsonObject()) {
				JsonObject expectedObject = expected.getAsJsonObject();
				JsonObject actualObject = actual.getAsJsonObject();
				Set<String> expectedObjectKeys = keySet(expectedObject);
				Set<String> actualObjectKeys = keySet(actualObject);
				if (expectedObjectKeys.size() != actualObjectKeys.size()) {
					log(context, "JsonObject keys size", actualObjectKeys, expectedObjectKeys);
					return true;
				} else {
					Set<String> extraProperties = setDifference(actualObjectKeys, expectedObjectKeys);
					Set<String> missingProperties = setDifference(expectedObjectKeys, actualObjectKeys);
					final boolean hasMissingProperties = !missingProperties.isEmpty();
					final boolean hasExtraProperties = !extraProperties.isEmpty();
					if (hasMissingProperties || hasExtraProperties) {
						log(context, "Properties", expectedObjectKeys, actualObjectKeys, extraProperties, missingProperties);
						return false;
					}
					boolean hasDiff = false;
					for (Entry<String, JsonElement> e : expectedObject.entrySet()) {
						String expectedProperty = e.getKey();
						hasDiff = hasDiff || diff(e.getValue(), actualObject.get(expectedProperty), context, "Object[\"" + expectedProperty + "\"]");
					}

					return hasDiff;
				}
			}
		} else {
			log(context, "JsonElements", expectedClass, actualClass);
			return true;
		}
		return false;
	}

	public static Set<String> setDifference(Set<String> lhs, Set<String> rhs) {
		Set<String> result = new HashSet<>(lhs);
		result.removeAll(rhs);
		return result;
	}

	public static Set<String> keySet(JsonObject jObj) {
		Set<String> keys = new HashSet<String>();
		for (Entry<String, JsonElement> e : jObj.entrySet()) {
			keys.add(e.getKey());
		}
		return keys;
	}

	private void log(JsonContext context, String type, Set<String> expected, Set<String> actual, Set<String> extra, Set<String> missing) {
		StringBuilder msg = new StringBuilder();
		msg.append(type + " diff, expected: " + expected.toString() + " but was " + actual.toString() + ".");
		if (extra.size() > 0)
			msg.append(" Extra: " + extra.toString());
		if (missing.size() > 0)
			msg.append(" Missing: " + missing.toString());
		log(msg.toString());
		printContextInfo(context);
	}

	private void log(JsonContext context, String type, Object expected, Object actual) {
		log(type + " diff, expected: " + expected.toString() + " but was " + actual.toString());
		printContextInfo(context);
	}

	public void printContextInfo(JsonContext context) {
		printContextInfo(context, context.depth());
	}

	public void printContextInfo(JsonContext context, int depth) {
		if (context.value() != null)
			log("Stack[" + depth + "]: " + context.value());
		if (context.previous() != null)
			printContextInfo(context.previous(), depth - 1);
	}

	private void log(String msg) {
		try {
			this.out.write(msg);
			this.out.write("\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	static class JsonContext {

		public JsonContext() {
			previous = null;
			this.value = null;
		}

		public int depth() {
			if (previous == null) {
				return -1;
			} else {
				return 1 + previous.depth();
			}
		}

		public JsonContext(JsonContext previous, String value) {
			this.previous = previous;
			this.value = value;
		}

		public JsonContext previous() {
			return this.previous;
		}

		public String value() {
			return this.value;
		}

		@Override
		public String toString() {
			if (previous == null)
				return "root";
			else
				return previous.toString() + " -> " + value();
		}

		JsonContext previous;
		String value;
	}

}
