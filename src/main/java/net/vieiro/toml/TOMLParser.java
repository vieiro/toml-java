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
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

    private TOMLParser() {
    }

    /**
     * Parse a TOML document in a String.
     *
     * @param content The TOML document.
     * @return A TOML object with the result of parsing.
     * @throws IOException If an I/O error happens.
     */
    public static TOML parseFromString(String content) throws IOException {
        return parse(CharStreams.fromString(content));
    }

    /**
     * Parse a TOML document from a Reader.
     *
     * @param reader The Reader.
     * @param filename The name of the file, used in error messages.
     * @return A TOML object with the result of parsing.
     * @throws IOException If an I/O error happens.
     */
    public static TOML parseFromReader(Reader reader, String filename) throws IOException {
        return parse(CharStreams.fromReader(reader, filename));
    }

    /**
     * Parse a TOML document from a Reader.
     *
     * @param reader The Reader.
     * @return A TOML object with the result of parsing.
     * @throws IOException If an I/O error happens.
     */
    public static TOML parseFromReader(Reader reader) throws IOException {
        return parse(CharStreams.fromReader(reader));
    }

    /**
     * Parse a TOML document from a file.
     *
     * @param filename The name of the file.
     * @return A TOML object with the result of parsing.
     * @throws IOException If an I/O error happens.
     */
    public static TOML parseFromFilename(String filename) throws IOException {
        return parse(CharStreams.fromFileName(filename, StandardCharsets.UTF_8));
    }

    /**
     * Parse a TOML document from an InputStream.
     * Uses UTF-8 as the encoding.
     * @param input The InputStream.
     * @return A TOML object with the result of parsing.
     * @throws IOException If an I/O error happens.
     */
    public static TOML parseFromInputStream(InputStream input) throws IOException {
        return parse(CharStreams.fromStream(input, StandardCharsets.UTF_8));
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

        parser.document().accept(visitor);

        return new TOML(visitor.getRoot(), visitor.getErrors());
    }

}
