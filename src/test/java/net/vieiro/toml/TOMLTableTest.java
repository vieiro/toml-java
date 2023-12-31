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

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class TOMLTableTest {

    @Test
    public void testShouldParseTableTestProperly() throws Exception {
        System.out.println("testShouldParseTableTestProperly");
        TOML toml = Util.parse("table-test.toml", false);

        assertEquals("key1", toml.getString("key1").orElse(null));
        assertEquals("some string", toml.getString("table-1/key1").orElse(null));
        assertEquals("another string", toml.getString("table-2/key1").orElse(null));

        assertEquals(456, toml.getLong("table-2/key2").orElse(null));

        assertEquals("pug", toml.getString("dog/tater.man/type/name").orElse(null));
    }

}
