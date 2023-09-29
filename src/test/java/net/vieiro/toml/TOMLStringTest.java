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

/**
 *
 */
public class TOMLStringTest {

    @Test
    public void testShouldParseStringsProperly() throws Exception {
        System.out.println("testShouldParseStringsProperly");
        TOML toml = Util.parse("string-test.toml", false);

        String c = toml.getString("c").orElse(null);
        assertEquals("Roses are red\r\nViolets are blue", c);

        String d = toml.getString("d").orElse(null);
        assertEquals("String does not end here\" but ends here\\", d);

        String str1 = toml.getString("str1").orElse(null);
        String str2 = toml.getString("str2").orElse(null);
        String str3 = toml.getString("str3").orElse(null);

        assertEquals(str1, str2);
        assertEquals(str1, str3);

        String winpath = toml.getString("winpath").orElse(null);

        assertEquals("C:\\Users\\nodejs\\templates", winpath);

        String regex2 = toml.getString("regex2").orElse(null);
        assertEquals("I [dw]on't need \\d{2} apples", regex2);

        String empty = toml.getString("0").orElse(null);
        assertEquals("", empty);

        String escapebs1 = toml.getString("escape-bs-1").orElse(null);
        assertEquals("mle \\\nb", escapebs1, "escape-bs-1 fails");

        String escapebs2 = toml.getString("escape-bs-2").orElse(null);
        assertEquals("mle \\b", escapebs2, "escape-bs-2 fails");

        String escapebs3 = toml.getString("escape-bs-3").orElse(null);
        assertEquals("mle \\\\\n  b", escapebs3, "escape-bs-3 fails");

        String multiline_empty_four = toml.getString("multiline_empty_four").orElse(null);
        assertEquals("XX", multiline_empty_four, "multiline_empty_four failss");

    }

//    @Test
//    public void testShouldLineEndingBackslashMatch() {
//        String multiline = "The quick brown \\\n"
//                + "\n"
//                + "\n"
//                + "  fox jumps over \\\n"
//                + "    the lazy dog.";
//        String result = TOMLVisitor.LINE_ENDING_BACKSLASH.matcher(multiline).replaceAll("");
//        assertEquals("The quick brown fox jumps over the lazy dog.", result);
//    }
}
