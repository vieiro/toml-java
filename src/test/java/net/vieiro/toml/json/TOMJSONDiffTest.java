/*
 * Copyright 2023 Antonio Vieiro <antonio@vieiro.net>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.vieiro.toml.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flipkart.zjsonpatch.JsonDiff;
import java.io.IOException;
import java.io.InputStream;
import net.vieiro.toml.TOML;
import net.vieiro.toml.Util;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Reproduces some of the tests at https://github.com/toml-lang/toml-test by
 * parsing and transforming results to JSON. These tests are not currently
 * thread safe.
 */
public class TOMJSONDiffTest {

    private static ObjectMapper mapperInstance;

    public static synchronized ObjectMapper getMapper() {
        if (mapperInstance == null) {
            System.out.println("Initializing object mapper for JSON tests...");
            mapperInstance = new ObjectMapper();
            mapperInstance.configure(DeserializationFeature.USE_LONG_FOR_INTS, true);
            mapperInstance.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            mapperInstance.registerModule(javaTimeModule);
            // Serialize toml-java Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY an Double.NaN to JSON null
            SimpleModule doubleModule = new SimpleModule();
            doubleModule.addSerializer(Double.class, new NumberSerializers.DoubleSerializer(Double.class) {
                @Override
                public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                    Double d = (Double) value;
                    if (Double.isNaN(d)) {
                        gen.writeNull();;
                    } else if (d == Double.POSITIVE_INFINITY) {
                        gen.writeNull();
                    } else if (d == Double.NEGATIVE_INFINITY) {
                        gen.writeNull();
                    } else {
                        super.serialize(value, gen, provider);
                    }
                }
            });
            mapperInstance.registerModule(doubleModule);
        }
        return mapperInstance;
    }

    private void performJSONTest(String testName) throws Exception {
        ObjectMapper objectMapper = getMapper();

        assertNotNull(objectMapper, "objectMapper not initialized");
        // Given an expected JsonNode
        String jsonResource = testName.replace("toml", "json");
        JsonNode expected = null;

        try (InputStream jsonInput = TOMJSONDiffTest.class
                .getResourceAsStream(jsonResource)) {
            assertNotNull(jsonInput, String.format("Couldn't find test resource '%s'", jsonResource));
            // System.out.format( "  - Reading json file 'json/%s'%n", jsonResource);
            expected = objectMapper.readTree(jsonInput);
        }
        assertNotNull(expected);
        // ... and a TOMLParser generated JsonNode ...
        TOML toml = Util.parse("json/" + testName, false);
        JsonNode result = objectMapper.valueToTree(toml.getRoot());
        // ... when we compare these two JSON nodes ...
        JsonNode diff = JsonDiff.asJson(expected, result);

        // .. then the comparison should be an empty array
        assertTrue(diff.isArray());
        boolean diffIsEmpty = ((ArrayNode) diff).isEmpty();
        if (!diffIsEmpty) {
            String message = String.format("Expected an empty difference for file: %s but got:"
                    + "%nEXPECTED:%n"
                    + "%s"
                    + "%nRESULT:%n"
                    + "%s"
                    + "%nDIFF:%n"
                    + ""
                    + "%s",
                    testName,
                    expected.toPrettyString(),
                    result.toPrettyString(),
                    diff.toPrettyString());

            assertTrue(diffIsEmpty, message);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "alphanum.toml",
        "array-0.toml",
        "array-1.toml",
        "array-implicit-and-explicit-after.toml",
        "array-implicit.toml",
        "array-many.toml",
        "array-nest.toml",
        "array-of-tables-0.toml",
        "array-of-tables-1.toml",
        "array-of-tables-2.toml",
        "array-one.toml",
        "array-table-array.toml",
        "array.toml",
        "array-within-dotted.toml",
        "at-eof2.toml",
        "at-eof.toml",
        "boolean-0.toml",
        "bool.toml",
        "case-sensitive.toml",
        "comment-0.toml",
        "datetime.toml",
        "dotted-empty.toml",
        "dotted.toml",
        "double-quote-escape.toml",
        "empty-file.toml",
        "empty-name.toml",
        "empty.toml",
        "end-in-bool.toml",
        "equals-nospace.toml",
        "escaped-escape.toml",
        "escapes.toml",
        "escape-tricky.toml",
        "everywhere.toml",
        "example.toml",
        "exponent.toml",
        "float-0.toml",
        "float-1.toml",
        "float-2.toml",
        "float.toml",
        "hetergeneous.toml",
        /* 
        "hex-escape.toml" is disabled.
        toml-java correctly refuses to parse "\x0A" as a hex character.
        The TOML 1.0.0 specification (https://toml.io/en/v1.0.0#string) reads:

        "All other escape sequences not listed above are reserved; if they are used, TOML should produce an error."

        */
        // DISABLED "hex-escape.toml", 
        "implicit-and-explicit-after.toml",
        "implicit-and-explicit-before.toml",
        "implicit-groups.toml",
        "inf-and-nan.toml",
        "inline-table-0.toml",
        "inline-table-1.toml",
        "inline-table-2.toml",
        "inline-table-3.toml",
        "inline-table.toml",
        "integer-0.toml",
        "integer-1.toml",
        "integer-2.toml",
        "integer.toml",
        "key-dotted.toml",
        "keys-0.toml",
        "keys-1.toml",
        "keys-3.toml",
        "keys-4.toml",
        "keys-5.toml",
        "keys-6.toml",
        "keys-7.toml",
        "key-value-pair-0.toml",
        "keyword.toml",
        "literals.toml",
        "local-date-0.toml",
        "local-date-time-0.toml",
        "local-date.toml",
        "local-time-0.toml",
        "local-time.toml",
        "local.toml",
        "long.toml",
        "milliseconds.toml",
        "mixed-int-array.toml",
        "mixed-int-float.toml",
        "mixed-int-string.toml",
        "mixed-string-table.toml",
        "multiline-escaped-crlf.toml",
        "multiline-quotes.toml",
        "multiline.toml",
        "names.toml",
        "nested-double.toml",
        "nested-inline-table.toml",
        "nested.toml",
        "nest.toml",
        "newline-crlf.toml",
        "newline-lf.toml",
        "nl.toml",
        "no-eol.toml",
        "noeol.toml",
        "nonascii.toml",
        "nospaces.toml",
        "numeric-dotted.toml",
        "numeric.toml",
        "offset-date-time-0.toml",
        "offset-date-time-1.toml",
        "quoted-dots.toml",
        "quoted-unicode.toml",
        "raw-multiline.toml",
        "raw.toml",
        "simple.toml",
        "space.toml",
        "spec-example-1-compact.toml",
        "spec-example-1.toml",
        "special-chars.toml",
        "special-word.toml",
        "string-0.toml",
        "string-1.toml",
        "string-2.toml",
        "string-3.toml",
        "string-4.toml",
        "string-5.toml",
        "string-6.toml",
        "string-7.toml",
        "string-quote-comma-2.toml",
        "string-quote-comma.toml",
        "strings.toml",
        "string-with-comma-2.toml",
        "string-with-comma.toml",
        "sub-empty.toml",
        "sub.toml",
        "table-0.toml",
        "table-1.toml",
        "table-2.toml",
        "table-3.toml",
        "table-4.toml",
        "table-5.toml",
        "table-6.toml",
        "table-7.toml",
        "table-8.toml",
        "table-9.toml",
        "table-array-string-backslash.toml",
        "timezone.toml",
        "tricky.toml",
        "underscore.toml",
        "unicode-escape.toml",
        "unicode-literal.toml",
        "whitespace.toml",
        "with-literal-string.toml",
        "without-super.toml",
        "with-pound.toml",
        "with-single-quotes.toml",
        "zero.toml",}
    )
    public void testShouldPassJSONDifferenceTests(String test) throws Exception {
        System.out.format("testShouldPassJSONDifferenceTests - %s%n", test);
        performJSONTest(test);
    }

}
