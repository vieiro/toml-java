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
package net.vieiro.toml;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test utilities
 */
public final class Util {

    public static TOML parse(String resource, boolean verbose) throws IOException {
        return parse(resource, verbose, false);
    }

    public static TOML parse(String resource, boolean verbose, boolean allowingErrors) throws IOException {
        try (InputStream input = Util.class.getResourceAsStream(resource)) {
            if (verbose) {
                System.out.format("  - Reading test file '%s'%n", resource);
            }
            assertNotNull(input, "Missing test resource '" + resource + "'");
            TOML toml = TOMLParser.parseFromInputStream(input);
            List<String> errors = toml.getErrors();

            if (verbose) {
                System.out.format("Parsed %s%n", resource);
                System.out.format("%s%n", toml.root);

                for (String error : errors) {
                    System.out.format("SYNTAX ERROR: %s %s%n", resource, error);
                }
                System.out.flush();
            }

            if (!allowingErrors) {
                assertEquals(0, errors.size(), String.format("Test %s has %d syntax errors (%s).", resource, errors.size(), errors.stream().collect(Collectors.joining(",\n"))));
            }

            return toml;
        }
    }

}
