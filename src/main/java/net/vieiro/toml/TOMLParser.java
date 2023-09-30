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
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vieiro.toml.antlr4.TomlLexerInternal;
import net.vieiro.toml.antlr4.TomlParserInternal;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointBuffer;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;

/**
 * Parsers TOML files.
 */
public final class TOMLParser {

    private static final Logger LOG = Logger.getLogger(TOMLParser.class.getName());

    private static final int DEFAULT_BUFFER_SIZE = 4 * 4096;

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
     * @return A TOML object with the result of parsing.
     * @throws IOException If an I/O error happens.
     */
    public static TOML parseFromReader(Reader reader) throws IOException {
        return parseFromReader(reader, "Unknown source");
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
        try {
            CodePointBuffer.Builder codePointBufferBuilder = CodePointBuffer.builder(DEFAULT_BUFFER_SIZE);
            CharBuffer charBuffer = CharBuffer.allocate(DEFAULT_BUFFER_SIZE);
            while ((reader.read(charBuffer)) != -1) {
                charBuffer.flip();
                codePointBufferBuilder.append(charBuffer);
                charBuffer.compact();
            }
            CharStream stream = CodePointCharStream.fromBuffer(codePointBufferBuilder.build(), filename);
            return parse(stream);
        } catch (MalformedInputException mie) {
            String error = String.format("Malformed UTF-8 detected on input %s", mie.getMessage());
            return new TOML(Collections.emptyMap(), Arrays.asList(error));
        } finally {
            reader.close();
        }
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
     * Parse a TOML document from an InputStream. Uses UTF-8 as the encoding.
     *
     * @param input The InputStream.
     * @return A TOML object with the result of parsing.
     * @throws IOException If an I/O error happens.
     */
    public static TOML parseFromInputStream(InputStream input) throws IOException {
        // We want "CodingErrorAction.REPORT" to detect invalid UTF-8
        try (ReadableByteChannel channel = Channels.newChannel(input)) {
            CharStream stream = CharStreams.fromChannel(
                    channel,
                    StandardCharsets.UTF_8,
                    DEFAULT_BUFFER_SIZE,
                    CodingErrorAction.REPORT,
                    IntStream.UNKNOWN_SOURCE_NAME,
                    -1);
            return parse(stream);
        } catch (MalformedInputException mie) {
            String error = String.format("Malformed UTF-8 detected on input %s", mie.getMessage());
            return new TOML(Collections.emptyMap(), Arrays.asList(error));
        }
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

        try {
            parser.document().accept(visitor);
        } catch (NoViableAltException nvae) {
            visitor.getErrors().add("toml-java cannot parse this TOML documen. If you're sure this is a valid TOML document please file an issue in toml-java");
            TOML toml = new TOML(Collections.emptyMap(), visitor.getErrors());
            return toml;
        } catch (ParseCancellationException pce) {
            visitor.getErrors().add(pce.getMessage());
            TOML toml = new TOML(Collections.emptyMap(), visitor.getErrors());
            return toml;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage() + "/" + e.getClass().getName(), e);
            visitor.getErrors().add(String.format("Parsing stopped: %s", e.getMessage()));
            TOML toml = new TOML(Collections.emptyMap(), visitor.getErrors());
            return toml;
        }

        return new TOML(visitor.getRoot(), visitor.getErrors());
    }

}
