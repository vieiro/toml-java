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
import ognl.Ognl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class TOMLArrayTableTest {

    @Test
    public void testShouldParseProperlyArrayOfTables() throws Exception {
        System.out.println("testShouldParseProperlyArrayOfTables");
        boolean verbose = false;
        TOML toml = Util.parse("array-of-tables-test.toml", verbose);

        // Querying the TOML document using OGNL
        Object red = Ognl.getValue("fruits[0].physical.color", toml.root);
        assertEquals("red", red);

        Object plantain = Ognl.getValue("fruits[1].varieties[0].name", toml.root);
        assertEquals("plantain", plantain);

        if (verbose) {
            // Dumping the TOML document to JSON using GSON
            System.out.println("array-of-tables-test.toml in JSON format:");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            PrintWriter out = new PrintWriter(System.out);
            gson.toJson(toml.root, out);
            out.flush();
            System.out.println();
        }
    }

}
