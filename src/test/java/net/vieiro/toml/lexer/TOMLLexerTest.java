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
package net.vieiro.toml.lexer;

import net.vieiro.toml.*;
import java.io.IOException;
import java.util.BitSet;
import net.vieiro.toml.antlr4.TOMLAntlrLexer;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class TOMLLexerTest {

    private static final String string_repeat(char c, int n) {
        if (n <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * The NetBeans Antlr lexing bridge requires that Antlr Lexers behave
     * properly even on errors (unclosed strings, etc.).
     *
     * We do this adding extra Lexer rules that detect incomplete tokens and
     * mark these as "INVALID_VALUE". The parser is then responsible for
     * generating errors for these unexpected tokens.
     *
     * This test opens a (possibly invalid) TOML file, and then starts removing
     * one character at a time (simulating a person editing a file). The lexer
     * should be have properly (generating INVALID_VALUE tokens) at all times.
     *
     * @throws IOException
     */
    @Test
    public void testShouldLexerBehaveProperlyOnErrors() throws IOException {
        System.out.println("testShouldLexerBehaveProperlyOnErrors");

        // Given the "arrow-parquet-Cargo.toml" input string
        String input = Util.read("toml-lexer-test.toml");

        for (StringBuilder sb = new StringBuilder(input); sb.length() > 0; sb.deleteCharAt(sb.length() - 1)) {
            String toParse = sb.toString();
            String[] lines = toParse.split("\n");
            TOMLAntlrLexer lexer = new TOMLAntlrLexer(CharStreams.fromString(toParse));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new ANTLRErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> rcgnzr, Object o, int lineNumber,
                        int columnNumber, String message, RecognitionException re) {
                    System.err.format("Error at %d:%d:%s%n", lineNumber, columnNumber, message);
                    System.err.format("%d: |%s|%n", lineNumber, lines[lineNumber - 1]);
                    System.err.format("%d:  %s^%n", lineNumber, string_repeat(' ', columnNumber));
                    fail("Lexer should detect an invalid value in this situation.");
                }

                @Override
                public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean bln, BitSet bitset, ATNConfigSet atncs) {
                }

                @Override
                public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitset, ATNConfigSet atncs) {
                }

                @Override
                public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atncs) {
                }
            });
            while (true) {
                Token token = lexer.nextToken();
                if (token.getType() == TOMLAntlrLexer.EOF) {
                    int tokenIndex = token.getStopIndex();
                    if (tokenIndex != toParse.length() - 1) {
                        System.out.format("Error parsing. token stop %d input max %d%n", tokenIndex, toParse.length() - 1);
                    }
                    break;
                }
                // Set to true to dump lexer tokens
                boolean debug = false;
                if (debug) {
                    String tokenName = TOMLAntlrLexer.VOCABULARY.getSymbolicName(token.getType());
                    int line = token.getLine();
                    int column = token.getCharPositionInLine();
                    String text = token.getText().replace('\r', '?').replace('\n', '?');
                    System.out.format("%3d:%3d %5d-%5d-%s (%s)%n", line, column, token.getStartIndex(), token.getStopIndex(), tokenName, text);
                }
            }
        }

    }

}
