# json-diff
Google JSON (Gson) diff comparison tool

# Usage

Print JSON diff to console

```java
JsonParser parser = new JsonParser();
JsonElement expected = parser.parse(expectedJson);
JsonElement actual = parser.parse(actualJson);
boolean hasDifferences = JsonDiff.diff(expected, actual);
```

Print JSON diff to String
```java
StringWriter result = new StringWriter(); // Or NullWriter if we don't care about the differences
boolean hasDifferences = JsonDiff.diff(result, expected, actual);
System.out.print(result.toString());
```
