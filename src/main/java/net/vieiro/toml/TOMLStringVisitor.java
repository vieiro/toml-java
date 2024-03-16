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

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.vieiro.toml.antlr4.TOMLAntlrParser;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Utilities for visiting TOML string values and quoted keys. TOML specification
 * has evolved during the years, making things more complex and slow (some
 * Unicode ranges are not allowed, control characters are not allowed, etc.)
 */
final class TOMLStringVisitor {

    // From control characters we only allow \b (0x08) \t (0x09), \r (0x0d), \n (0x0a) and \f (0x0c)
    private static final Pattern HAS_CONTROL_CHARS = Pattern.compile(".*([\\u0000-\\u0008\\u000B\\u000e\\u0010-\\u001F\\u007F]).*");

    /**
     * Returns true if a given codepoint is a Unicode scalar value
     *
     * @see
     * <a href="https://www.unicode.org/glossary/#unicode_scalar_value">Unicode
     * Scalar Value</a>
     * @param codepoint An unicode codepoint
     * @return True if this is an Unicode scalar.
     */
    static boolean isUnicodeScalar(int codePoint) {
        return (0 <= codePoint && codePoint <= 0xD7FF16) || (0xE00016 <= codePoint && codePoint <= 0x10FFFF16);
    }

    /**
     * Returns true if a given codepoint is a Unicode surrogate pair.
     * @see
     * <a href="https://github.com/toml-lang/toml/pull/274">Unicode escapes must be scalar values</a>
     * @param codepoint An unicode codepoint
     * @return True if this is an unicode surrogate pair
     */
    static boolean isSurrogatePair(int codepoint) {
        return Character.isSurrogate((char) codepoint);
    }

    /**
     * Adapted from https://gist.github.com/uklimaschewski/6741769 Unescapes a
     * string that contains standard Java escape sequences.
     * <ul>
     * <li><strong>&#92;b &#92;f &#92;n &#92;r &#92;t &#92;" &#92;'</strong> :
     * BS, FF, NL, CR, TAB, double and single quote.</li>
     * <li><strong>&#92;X &#92;XX &#92;XXX</strong> : Octal character
     * specification (0 - 377, 0x00 - 0xFF).</li>
     * <li><strong>&#92;uXXXX or &#92;uXXXXXXXX</strong> : Hexadecimal based
     * Unicode character.</li>
     * </ul>
     *
     * @param st A string optionally containing standard java escape sequences.
     * @return The translated string.
     */
    static String unescapeTOMLString(String st) throws Exception {
        // Fail fast if not escaped
        if (st.indexOf('\\') == -1) {
            return st;
        }

        StringBuilder sb = new StringBuilder(st.length());

        for (int i = 0; i < st.length(); i++) {
            char ch = st.charAt(i);
            if (ch == '\\') {
                char nextChar = (i == st.length() - 1) ? '\\' : st
                        .charAt(i + 1);
                // Octal escape?
                if (nextChar >= '0' && nextChar <= '7') {
                    String code = "" + nextChar;
                    i++;
                    if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                            && st.charAt(i + 1) <= '7') {
                        code += st.charAt(i + 1);
                        i++;
                        if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                                && st.charAt(i + 1) <= '7') {
                            code += st.charAt(i + 1);
                            i++;
                        }
                    }
                    sb.append((char) Integer.parseInt(code, 8));
                    continue;
                }
                // Line ending backslash
                /* "When the last non-whitespace character on a line is an 
                unescaped \, it will be trimmed along with all whitespace 
                (including newlines) up to the next non-whitespace 
                character or closing delimiter"

                This is tricky, because "\\n" complies, but "\   \n" complies too.
                 */
                if (Character.isWhitespace(nextChar)) {
                    boolean newLineFound = nextChar == '\n';
                    // line ending backslash
                    int newi = i + 1;
                    for (int j = i + 2; j < st.length(); j++) {
                        int c = st.charAt(j);
                        if (Character.isWhitespace(c)) {
                            newLineFound = newLineFound || c == '\n';
                            newi = j;
                        } else {
                            break;
                        }
                    }
                    if (newLineFound) {
                        i = newi;
                        continue;
                    }
                }
                switch (nextChar) {
                    case '\\':
                        ch = '\\';
                        break;
                    case '\n':
                    case '\r':
                        continue;
                    case 'b':
                        ch = '\b';
                        break;
                    case 'f':
                        ch = '\f';
                        break;
                    case 'n':
                        ch = '\n';
                        break;
                    case 'r':
                        ch = '\r';
                        break;
                    case 't':
                        ch = '\t';
                        break;
                    case '\"':
                        ch = '\"';
                        break;
                    case '\'':
                        ch = '\'';
                        break;
                    // Hex Unicode: u????
                    case 'u': {
                        if (i >= st.length() - 5) {
                            ch = 'u';
                            break;
                        }
                        int code = Integer.parseInt(
                                "" + st.charAt(i + 2) + st.charAt(i + 3)
                                + st.charAt(i + 4) + st.charAt(i + 5), 16);
                        if (!isUnicodeScalar(code)) {
                            throw new Exception(String.format("Unicode point \\u%X is not a Unicode scalar", code));
                        }
                        if (isSurrogatePair(code)) {
                            throw new Exception(String.format("Unicode point \\u%X is an Unicode surrogate pair", code));
                        }
                        sb.append(Character.toChars(code));
                        i += 5;
                        continue;
                    }
                    // Hex Unicode: U????????
                    case 'U': {
                        if (i >= st.length() - 9) {
                            ch = 'U';
                            break;
                        }
                        int code = Integer.parseInt(
                                "" + st.charAt(i + 2) + st.charAt(i + 3)
                                + st.charAt(i + 4) + st.charAt(i + 5)
                                + st.charAt(i + 6) + st.charAt(i + 7)
                                + st.charAt(i + 8) + st.charAt(i + 9),
                                16);
                        if (!isUnicodeScalar(code)) {
                            throw new Exception(String.format("Unicode point \\u%X is not a Unicode scalar", code));
                        }
                        sb.append(Character.toChars(code));
                        i += 9;
                        continue;
                    }
                    default:
                        throw new Exception("Bad escape sequence '\\" + ((char) nextChar) + "'");
                }
                i++;
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    private static final void detectUnquotedControlChars(ParserRuleContext ctx, String value) {
        Matcher matcher = HAS_CONTROL_CHARS.matcher(value);
        if (matcher.matches()) {
            ArrayList<String> chars = new ArrayList<>();
            while (matcher.find()) {
                chars.add(String.format("0x%02x", matcher.group(1).charAt(0)));
                if (chars.size() > 10) {
                    chars.add("...");
                    break;
                }
            }
            String offending = String.join(",", chars);
            throw TOMLVisitor.newPCException(ctx, String.format("String value contains illegal Unicode control characters (%s)", offending));
        }
    }

    static Object visitString(TOMLAntlrParser.StringContext ctx) {
        try {
            String value = null;
            if (ctx.BASIC_STRING() != null) {
                String BASIC_STRING = ctx.BASIC_STRING().getText();
                // Remove quotes from BASIC_STRING
                BASIC_STRING = BASIC_STRING.substring(1, BASIC_STRING.length() - 1);
                detectUnquotedControlChars(ctx, BASIC_STRING);
                value = unescapeTOMLString(BASIC_STRING);
            } else if (ctx.ML_BASIC_STRING() != null) {
                String ML_BASIC_STRING = ctx.ML_BASIC_STRING().getText();
                ML_BASIC_STRING = ML_BASIC_STRING.substring(3, ML_BASIC_STRING.length() - 3);
                if (ML_BASIC_STRING.endsWith("\"\"\"") && !ML_BASIC_STRING.endsWith("\\\"\"\"")) {
                    // TODO: This is related to the parser not handling ML_LITERAL_STRING in submode
                    throw TOMLVisitor.newPCException(ctx, "Illegal basic multiline string");
                }
                detectUnquotedControlChars(ctx, ML_BASIC_STRING);
                ML_BASIC_STRING = unescapeTOMLString(ML_BASIC_STRING);
                // TODO: Review this.
                ML_BASIC_STRING = ML_BASIC_STRING.replaceAll("^\r?\n", "");
                value = ML_BASIC_STRING;
            } else if (ctx.LITERAL_STRING() != null) {
                String LITERAL_STRING = ctx.LITERAL_STRING().getText();
                // Remove single quotes
                LITERAL_STRING = LITERAL_STRING.substring(1, LITERAL_STRING.length() - 1);
                detectUnquotedControlChars(ctx, LITERAL_STRING);
                LITERAL_STRING = LITERAL_STRING.replaceAll("^\r?\n", "");
                value = LITERAL_STRING;
            } else if (ctx.ML_LITERAL_STRING() != null) {
                String ML_LITERAL_STRING = ctx.ML_LITERAL_STRING().getText();
                // Remove ''' at start & end
                ML_LITERAL_STRING = ML_LITERAL_STRING.substring(3, ML_LITERAL_STRING.length() - 3);
                if (ML_LITERAL_STRING.endsWith("'''") && !ML_LITERAL_STRING.endsWith("\\'''")) {
                    // TODO: This is related to the parser not handling ML_LITERAL_STRING in submode
                    throw TOMLVisitor.newPCException(ctx, "Illegal literal string");
                }
                detectUnquotedControlChars(ctx, ML_LITERAL_STRING);
                ML_LITERAL_STRING = ML_LITERAL_STRING.replaceAll("^\r?\n", "");
                value = ML_LITERAL_STRING;
            } else {
                throw TOMLVisitor.newPCException(ctx, "Unsupported TOML string type.");
            }
            return value;
        } catch (Exception e) {
            throw TOMLVisitor.newPCException(ctx, e);
        }
    }

    static Object visitQuoted_key(TOMLAntlrParser.Quoted_keyContext ctx) {
        try {
            String value = null;
            if (ctx.BASIC_STRING() != null) {
                String BASIC_STRING = ctx.BASIC_STRING().getText();
                detectUnquotedControlChars(ctx, BASIC_STRING);
                // Remove quotes
                BASIC_STRING = BASIC_STRING.substring(1, BASIC_STRING.length() - 1);
                value = unescapeTOMLString(BASIC_STRING);
            } else if (ctx.LITERAL_STRING() != null) {
                String LITERAL_STRING = ctx.LITERAL_STRING().getText();
                detectUnquotedControlChars(ctx, LITERAL_STRING);
                // Remove quotes
                LITERAL_STRING = LITERAL_STRING.substring(1, LITERAL_STRING.length() - 1);
                value = LITERAL_STRING;
            } else {
                throw TOMLVisitor.newPCException(ctx, "Unsupported quoted key value type");
            }
            return value;
        } catch (Exception e) {
            throw TOMLVisitor.newPCException(ctx, e);
        }
    }

    static Object visitUnquoted_key(TOMLAntlrParser.Unquoted_keyContext ctx) {
        String UNQUOTED_KEY = ctx.UNQUOTED_KEY().getText();
        // Grammar makes it impossible for UNQUOTED_KEY to contain dots. Let's check it, anyway
        if (UNQUOTED_KEY.indexOf('.') != -1) {
            throw new IllegalStateException("Grammar shouldn't allow dots in UNQUOTED_KEY.");
        }
        return UNQUOTED_KEY.trim();
    }

}
