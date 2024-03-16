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

import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class TOMLSubtreeTest {

    @Test
    public void testShouldProperlyRetrieveSubtrees() throws IOException {
        System.out.println("testShouldProperlyRetrieveSubtrees");

        // Given the "arrow-parquet-Cargo.toml" parsed TOML
        TOML toml = Util.parse("arrow-parquet-Cargo.toml", false);

        {
            // When we retrieve the "dependencies" subtree
            TOML dependencies = toml.subtree("dependencies");

            // Then the "arrow-ipc/workspace" should be true
            assertTrue(dependencies.getBoolean("arrow-ipc/workspace").orElse(false));
        }
        {
            // When we retrieve the  "[target.'cfg(target_arch = "wasm32")'.dependencies]" subtree
            TOML wasmDependencies = toml.subtree("target/cfg(target_arch = \"wasm32\")/dependencies");
            // Then "ahash/version" should be "0.8"
            assertEquals("0.8", wasmDependencies.getString("ahash/version").orElse("ERROR"));
        }

    }

}
