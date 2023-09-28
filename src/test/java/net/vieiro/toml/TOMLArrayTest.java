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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class TOMLArrayTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testShouldParseArraysProperly() throws Exception {
        System.out.println("testShouldParseArraysProperly");
        TOML toml = Util.parse("array-test.toml", false);

        {
            assertTrue(toml.getArray("integers").isPresent());
            List<Object> integers = toml.getArray("integers").get();
            assertEquals(3, integers.size());
            assertEquals(1L, integers.get(0));
            assertEquals(2L, integers.get(1));
            assertEquals(3L, integers.get(2));
        }

        {
            assertTrue(toml.getArray("nested_arrays_of_ints").isPresent());
            List<Object> nested_arrays_of_ints = toml.getArray("nested_arrays_of_ints").get();
            assertEquals(2, nested_arrays_of_ints.size());
            assertTrue(nested_arrays_of_ints.get(0) instanceof List);
            assertEquals(2, ((List) nested_arrays_of_ints.get(0)).size());
            assertEquals(3, ((List) nested_arrays_of_ints.get(1)).size());
        }

    }

}
