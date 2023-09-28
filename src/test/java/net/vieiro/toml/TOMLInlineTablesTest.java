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

import java.util.List;
import java.util.Map;
import ognl.Ognl;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class TOMLInlineTablesTest {


    @Test
    public void testShouldParseInlineTablesProperly() throws Exception {
        System.out.println("testShouldParseInlineTablesProperly");
        TOML toml = Util.parse("inline-table-test.toml", false);

        String mustang = toml.getString("nested/details/model").orElse(null);
        assertEquals("Mustang", mustang);

        Long year = toml.getLong("nested/details/year").orElse(0L);
        assertEquals(1968L, year);

        String pug = toml.getString("animal/type/name").orElse(null);
        assertEquals("pug", pug);

    }

}
