# toml-java

A parser for [TOML](https://toml.io/en/) files with minimum dependencies.

## Goals

1. To conform to the TOML specification as much as possible.
1. To return a Java object tree with no custom classes.
1. To have as little runtime dependencies as possible (currently only Antlr4 runtime is required).

## Type mapping

The java object tree generated after parsing a TOML document is created with the following Java types:

| TOML Type | Java Type |
|-----------|-----------|
| Integer   | Long      |
| Boolean   | Boolean   |
| Float     | Double (including PositiveInfinity, NegativeInfinity or NaN) |
| String    | String    |
| Offset date-time | OffsetDateTime |
| Local date-time | LocalDateTime |
| Local date | LocalDate |
| Local time | LocalTime |
| Array | List&lt;Object&gt; |
| Table | Map&lt;Object, Object&gt; |
| Array of Tables | List&lt;Map&lt;Object, Object&gt;&gt; |

## Basic usage

This library has just two public classes:

- `TOMLParser`, used to parse a TOML document.
- `TOML`, the result of the parsing, that contains:
    - The parsed java object tree `getRoot()`.
    - The errors generated during parsing, if any `getErrors()`.
    - Different query methods for analyging the generated object graph.
    - Methods for dumping the java object graph to JSON.

A basic parsing of a TOML document would be:

```java
// Parse an InputStream
TOML toml = TOMLParser.parseFromInputStream(input);

// No errors should be produced
assert(toml.getErrors().isEmpty())

// Obtain the parsed object tree
Map<Object, Object> parsed = toml.getRoot();

// Obtain the syntax errors, if any
List<String> errors = toml.getErrors();

```

## Querying

The TOML generated object tree can be queried using a very simple language: you
specify a path separated with forward slashes, specifying either the name of a
table or an integer when accessing an array.

For instance, given the following TOML document:

```toml
[[fruits]]
name = "apple"

[fruits.physical]  # subtable
color = "red"
shape = "round"

[[fruits.varieties]]  # nested array of tables
name = "red delicious"

[[fruits.varieties]]
name = "granny smith"

[[fruits]]
name = "banana"

[[fruits.varieties]]
name = "plantain"

```

You can query it, once parsed, as follows:

```java
// Parse an InputStream
TOML toml = TOMLParser.parseFromInputStream(input);

// Returns "red"
String red = toml.getString("fruits/0/physical/color").orElse(null);

// Returns "plantain"
String plantain = toml.getString("fruits/1/varieties/0/name").orElse(null);

// Also returns "plantain", note the negative index
String plantain_too = toml.getString("fruits/1/varieties/-1/name").orElse(null);
```

You can use negative indexes (-1, -2) for accessing arrays from the end.

See `TOML.java` for different query methods.

## Advanced querying

You can use more advanced techniques and expression languages for querying the
object tree. The unit tests have some examples for querying the object tree using
OGNL (but you could use JXPath, Spring SpEL, etc.).

## JSON Serialization

You can dump a TOML parsing result into JSON without requiring extra dependencies:

```java
// Parse an InputStream
TOML toml = TOMLParser.parseFromString(
    "[dog.\"tater.man\"]\n"
    + "type.name = \"pug\"");
toml.writeJSON(System.out);
```

Produces:

```
{"dog":{"tater.man":{"type":{"name":"pug"}}}}
```

## Bugs

Please file an issue if you find a TOML document that can't be parsed with this library.


