# toml-java

A simple parser for TOML files, with minimum dependencies (just Antlr4 runtime).

> NOTE: This is still alpha-quality software.

## Goals

- To conform to the TOML specification as much as possible.
- To return a Java object tree with no custom classes.

## Type mapping

The java object tree for the TOML document is created with the following Java types:

| TOML Type | Java Type |
|-----------|-----------|
| Integer   | Long      |
| Boolean   | Boolean   |
| Float     | Double (with PositiveInfinity, NegativeInfinity or NaN) |
| String    | String    |
| Offset date-time | OffsetDateTime |
| Local date-time | LocalDateTime |
| Local date | LocalDate |
| Local time | LocalTime |
| Array | List&lt;Object&gt; |
| Table | Map&lt;Object, Object&gt; |
| Array of Tables | List&lt;Map&lt;Object, Object&gt;&gt; |

## Basic usage

```java

// Parse an InputStream
TOML toml = TOMLParser.parseFromInputStream(input);

// Obtain the parsed object
Map<Object, Object> parsed = toml.getRoot();

// Obtain the syntax errors, if any
List<String> errors = toml.getErrors();

```

## Querying

The TOML result has some simple methods for querying the generated object tree:

```java

// Parse an InputStream
TOML toml = TOMLParser.parseFromInputStream(input, StandardCharsets.UTF_8);

// Query for a property
String name = toml.getString("workspace/project/name").orElse(null);
```

## Advanced querying

You can use more advanced techniques and expression languages for querying the
object tree. The unit tests have some examples for querying the object tree using
OGNL.

## JSON Serialization

The library does not contain JSON serialization per se. The unit tests have an
example of using GSON for serializing/pretty-printing a complex object tree to JSON.

## Defensive programming

The library throws exceptions in border cases. Please fill an issue if you find one of these,
with an example TOML document to reproduce the problem.


