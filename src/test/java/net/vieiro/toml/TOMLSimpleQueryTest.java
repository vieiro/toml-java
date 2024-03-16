/*
 * Copyright 2024 Antonio <antonio@vieiro.net>.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class TOMLSimpleQueryTest {

    @Test
    public void testShouldSimpleQueryIgnoreDifferentSlashes() throws Exception {

        boolean verbose = false;
        TOML toml = Util.parse("array-of-tables-test.toml", verbose);

        // When we query something starting with a single "/"
        String red = toml.getString("/fruit/apple/color").orElseThrow();
        // Then we get the proper value
        Assertions.assertEquals("red", red);

        // When we remove the leading "/"
        red = toml.getString("fruit/apple/color").orElseThrow();
        // Then the value is also correct
        Assertions.assertEquals("red", red);

        // When we add different contigous slashes
        red = toml.getString("fruit///apple/color").orElseThrow();
        // Then the value is also correct
        Assertions.assertEquals("red", red);


    }

}
