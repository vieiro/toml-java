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

import java.io.IOException;
import java.nio.charset.Charset;
import net.vieiro.toml.antlr4.TomlLexerInternal;
import net.vieiro.toml.antlr4.TomlParserInternal;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

/**
 * Parsers TOML files.
 */
public final class TOMLParser {

    public static TOML parse(String filename, Charset charset) throws IOException {
        return parse(CharStreams.fromFileName(filename, charset));
    }

    private static TOML parse(CharStream input) {
        TOMLVisitor visitor = new TOMLVisitor();

        TomlLexerInternal lexer = new TomlLexerInternal(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(visitor);

        TokenStream tokens = new CommonTokenStream(lexer);

        TomlParserInternal parser = new TomlParserInternal(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(visitor);

        Object result = parser.document().accept(visitor);

        return new TOML(result, visitor.getErrors());
    }

}
