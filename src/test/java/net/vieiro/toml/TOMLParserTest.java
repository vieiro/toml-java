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

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class TOMLParserTest {
    
    @Test
    public void testShouldParseTableTestProperly() throws Exception {
        System.out.println("testShouldParseTableTestProperly");
        TOML toml = TestUtil.parse("table-test.toml");

        assertEquals("key1", toml.getString("key1").orElse(null));
        assertEquals("some string", toml.getString("table-1/key1").orElse(null));
        assertEquals("another string", toml.getString("table-2/key1").orElse(null));

        assertEquals(456, toml.getLong("table-2/key2").orElse(null));

        assertEquals("pug", toml.getString("dog/tater.man/type/name").orElse(null));
    }

    @Test
    public void testShouldParseIntegersProperly() throws Exception {
        System.out.println("testShouldParseTableTestProperly");
        TOML toml = TestUtil.parse("integer-test.toml");

        assertEquals(5_349_221L, toml.getLong("int6").orElse(-1L));
        assertEquals(0xDEADBEEFL, toml.getLong("hex1").orElse(-1L));
        assertEquals(001234567L, toml.getLong("oct1").orElse(-1L));
        assertEquals(3L, toml.getLong("bin2").orElse(-1L));
        assertEquals(3.1415926, toml.getDouble("pi").orElse(-1.0));

        assertEquals(Double.POSITIVE_INFINITY, toml.getDouble("inf").orElse(-1.0));
        assertEquals(Double.NEGATIVE_INFINITY, toml.getDouble("minus_inf").orElse(-1.0));
        assertTrue(toml.getDouble("nan").isPresent());
        assertTrue(Double.isNaN(toml.getDouble("nan").get()));

    }
    
}
