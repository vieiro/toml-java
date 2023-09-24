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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import ognl.Ognl;
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
    public void testShouldParseIntegersBooleansProperly() throws Exception {
        System.out.println("testShouldParseIntegersBooleansProperly");
        TOML toml = TestUtil.parse("integer-boolean-test.toml");

        assertEquals(5_349_221L, toml.getLong("int6").orElse(-1L));
        assertEquals(0xDEADBEEFL, toml.getLong("hex1").orElse(-1L));
        assertEquals(001234567L, toml.getLong("oct1").orElse(-1L));
        assertEquals(3L, toml.getLong("bin2").orElse(-1L));
        assertEquals(3.1415926, toml.getDouble("pi").orElse(-1.0));

        assertEquals(Double.POSITIVE_INFINITY, toml.getDouble("inf").orElse(-1.0));
        assertEquals(Double.NEGATIVE_INFINITY, toml.getDouble("minus_inf").orElse(-1.0));
        assertTrue(toml.getDouble("nan").isPresent());
        assertTrue(Double.isNaN(toml.getDouble("nan").get()));

        assertEquals(Boolean.TRUE, toml.getBoolean("true").orElse(false));

    }

    @Test
    public void testShouldParseArraysProperly() throws Exception {
        System.out.println("testShouldParseArraysProperly");
        TOML toml = TestUtil.parse("array-test.toml");

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

    @Test
    public void testShouldLineEndingBackslashMatch() {
        String multiline = "The quick brown \\\n"
                + "\n"
                + "\n"
                + "  fox jumps over \\\n"
                + "    the lazy dog.";
        String result = TOMLVisitor.LINE_ENDING_BACKSLASH.matcher(multiline).replaceAll("");
        assertEquals("The quick brown fox jumps over the lazy dog.", result);
    }

    @Test
    public void testShouldParseStringsProperly() throws Exception {
        System.out.println("testShouldParseStringsProperly");
        TOML toml = TestUtil.parse("string-test.toml");

        String str1 = toml.getString("str1").orElse(null);
        String str2 = toml.getString("str2").orElse(null);
        String str3 = toml.getString("str3").orElse(null);

        assertEquals(str1, str2);
        assertEquals(str1, str3);

        String winpath = toml.getString("winpath").orElse(null);

        assertEquals("C:\\Users\\nodejs\\templates", winpath);

        String regex2 = toml.getString("regex2").orElse(null);
        assertEquals("I [dw]on't need \\d{2} apples", regex2);

    }

    @Test
    public void testShouldParseOffsetDateTimProperly() throws Exception {
        System.out.println("testShouldParseOffsetDateTimProperly");
        TOML toml = TestUtil.parse("date-test.toml");

        Instant odt1 = toml.getInstant("odt1").orElse(null);

        assertEquals(Instant.parse("1979-05-27T07:32:00Z"), odt1);

        LocalDateTime ldt1 = toml.getLocalDateTime("ldt1").orElse(null);
        assertEquals(LocalDateTime.parse("1979-05-27T07:32:00"), ldt1);

        LocalDate ld1 = toml.getLocalDate("ld1").orElse(null);
        assertEquals(LocalDate.parse("1979-05-27"), ld1);

        LocalTime lt1 = toml.getLocalTime("lt1").orElse(null);
        assertEquals(LocalTime.parse("07:32:00"), lt1);

    }

    @Test
    public void testShouldParseProperlyArrayOfTables() throws Exception {
        System.out.println("testShouldParseProperlyArrayOfTables");
        TOML toml = TestUtil.parse("array-of-tables-test.toml");

        // Querying the TOML document using OGNL
        Object red = Ognl.getValue("fruits[0].physical.color", toml.root);
        assertEquals("red", red);

        Object plantain = Ognl.getValue("fruits[1].varieties[0].name", toml.root);
        assertEquals("plantain", plantain);

        // Dumping the TOML document to JSON using GSON
        System.out.println("array-of-tables-test.toml in JSON format:");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        PrintWriter out = new PrintWriter(System.out);
        gson.toJson(toml.root, out);
        out.flush();
        System.out.println();
    }

    @Test
    public void testShouldParseInlineTablesProperly() throws Exception {
        System.out.println("testShouldParseInlineTablesProperly");
        TOML toml = TestUtil.parse("inline-table-test.toml");

        String mustang = toml.getString("nested/details/model").orElse(null);
        assertEquals("Mustang", mustang);

        Long year = toml.getLong("nested/details/year").orElse(0L);
        assertEquals(1968L, year);

        String pug = toml.getString("animal/type/name").orElse(null);
        assertEquals("pug", pug);

    }

}
