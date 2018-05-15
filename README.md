# json-diff
Google JSON (Gson) diff comparison tool

# Usage

Print JSON diff to console

```
JsonParser parser = new JsonParser();
JsonElement expected = parser.parse(expectedJson);
JsonElement actual = parser.parse(actualJson);
JsonDiff.diff(expected, actual);
```

Print JSON diff to String
```
StringWriter result = new StringWriter();
JsonDiff.diff(result, expected, actual);
System.out.print(result.toString());
```
