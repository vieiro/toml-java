# JSON tests

The "TOML" files in this directory come from https://github.com/toml-lang/toml-test (MIT license).

The equivalent JSON files are generated from the TOML files using https://github.com/woodruffw/toml2json

The tests:

a) Parse the TOML file and transform it to a JsonNode using jackson.
b) Read the corresponding JSON files using jackson into a JsonNode.
c) Compare the two JsonNode objects for differences using https://github.com/flipkart-incubator/zjsonpatch

> **NOTE:** The "toml2json" tool creates weird JSON objects for dates, these have been transformed to plain JSON strings for comparison.



