# toml-java

A parser for [TOML](https://toml.io/en/) files with minimum dependencies.

[![Javadoc](https://img.shields.io/badge/JavaDoc-Online-green)](https://vieiro.github.io/toml-java/javadoc/)

## Goals

1. To conform to the TOML specification as much as possible.
1. To return a Java object tree/subtree with no custom classes.
1. To have as little runtime dependencies as possible (currently only Antlr4 runtime is required).

## Maven & Gradle coordinates

### Dependency on Antlr 4.13.1

```xml
<dependency>
    <groupId>net.vieiro</groupId>
    <artifactId>toml-java</artifactId>
    <version>13.3.1</version>
</dependency>
```

```
implementation 'net.vieiro:toml-java:13.3.1'
```

## Basic usage

This main API of this library has just two public classes:

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
assert(toml.getErrors().isEmpty());

// Obtain the parsed object tree
Map<String, Object> parsed = toml.getRoot();

// Or query the parsed object tree
String red = toml.getString("fruits/0/physical/color").orElse(null);

```

### Subtree queries

In order to parse complex TOML documents, one can retrieve a subtree and query it. For instance, given the following TOML document:

```toml
[package]
name = "parquet"
version = {workspace = true}

[dependencies]
arrow-array = {worskpace = true, optional = true}

```

You could query it using either the TOML "root" object or use a subtree TOML object, like so:

```java
// Parse the document
TOML toml = TOMLParser.parseFromInputStream(...);

// Query the value of "dependencies/arrow-array/optional"
bool optional1 = toml.getBoolean("dependencies/arrow-array/optional").orElse(false);

// Or query a subtree
bool optional2 = toml.getSubtree("dependencies").getBoolean("arrow-array/optional").orElse(false);

```

## Antlr4 Parser

The ANTLR4 parser (automatically generated) is also available under the `net.vieiro.toml.antlr4` package. This API is
considered stable in minor version (`1.2.X` at the time being). If the grammar changes then
the minor version will be also upgraded.


## Type mapping

The Java object tree generated after parsing a TOML document is created with the following Java types:

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
| Table | Map&lt;String, Object&gt; |
| Array of Tables | List&lt;Map&lt;String, Object&gt;&gt; |

### Javadoc API

See [the Javadoc for this project](https://vieiro.github.io/toml-java/javadoc/) for more details.

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

## Release notes

## 1.1.1 Initial version

This version depends on Antlr4 v4.13.1.

## 1.1.11 Dependency downgrade

This version depends on Antlr4 v4.11.1 (changed to adhere to NetBeans Antlr4 version).

## 2.13.1 Antlr4 Lexer & Parser

- Changed versioning scheme. Major: Grammar version, Minor: Antlr4 version, Patch: bug fixes. (e.g. 2.13.1 means Grammar version 2, Antlr 4.13.X, Release 1)
- The Antlr4 lexer and parser is now considered a public API.
- Updated the lexer to work nicely with unclosed literal strings (for NetBeans IDE Editor integration).
- Upgraded Antlr4 runtime to v4.13.1 again.
- Simple query language now ignores contiguous or leading spaces (this is, the following queries are equivalent: "/a/b", "a/b", "a//b").
- Added "subtree" queries, to ease analyzing subtrees.

## 13.3.1 

- Changed versioning scheme to:
    - Major: antlr version (for instance, `13` means `Antlr 4.13.X`
    - Minor: toml-java grammar version.
    - Patch: bug fixes
- The lexer now detects incomplete tokens and properly reports them with token `INVALID_VALUE`.
    - This is useful for the NetBeans Antlr bridge.
- toml-java runs 555 unit tests.

