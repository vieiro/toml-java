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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class TOMLRealCargoTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testShouldParseArrowParquetCargoTOML() throws Exception {
        System.out.println("testShouldParseArrowParquetCargoTOML");
        TOML toml = Util.parse("arrow-parquet-Cargo.toml", false);

        List<Object> bin = toml.getArray("bin").orElse(null);
        assertNotNull(bin);
        Map bin1 = (Map) bin.get(1);
        assertNotNull(bin1);
        List<Object> required_features = (List<Object>) bin1.get("required-features");
        assertEquals("cli", required_features.get(1));

        Object cli = Ognl.getValue("#this.get('bin')[1].get('required-features')[1]", toml.root);
        assertEquals("cli", cli);

    }

}
