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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.vieiro.toml.antlr4.TomlParserInternal;
import net.vieiro.toml.antlr4.TomlParserInternalVisitor;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Visits the TOML AST.
 */
final class TOMLVisitor implements ANTLRErrorListener, TomlParserInternalVisitor<Object> {

    private static final Logger LOG = Logger.getLogger(TOMLVisitor.class.getName());

    private List<String> errors;
    private HashMap<Object, Object> root;
    private HashMap<Object, Object> currentTable;
    private HashMap<Object, Object> currentInlineTable;
    private Stack<ArrayList<Object>> arrayStack;

    public TOMLVisitor() {
        this.errors = new ArrayList<String>();
        this.arrayStack = new Stack<>();
    }

    public List<String> getErrors() {
        return errors;
    }

    public Map<Object, Object> getRoot() {
        return root;
    }

    //----------------------------------------------------------------------
    // AntlrErrorListener
    @Override
    public void syntaxError(Recognizer<?, ?> rcgnzr, Object o, int line, int col, String message, RecognitionException re) {
        String error = String.format("Syntax error at %d:%d %s", line, col, message);
        errors.add(error);
    }

    @Override
    public void reportAmbiguity​(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
        // Empty
    }

    @Override
    public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitset, ATNConfigSet atncs) {
        // Empty
    }

    @Override
    public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atncs) {
        // Empty
    }

    //----------------------------------------------------------------------
    // TomlParserInternalVisitor
    @Override
    public Object visitDocument(TomlParserInternal.DocumentContext ctx) {
        root = new HashMap<>();
        currentTable = root;

        for (TomlParserInternal.ExpressionContext expression : ctx.expression()) {
            expression.accept(this);
        }

        return root;
    }

    @Override
    public Object visitExpression(TomlParserInternal.ExpressionContext ctx) {
        if (ctx.key_value() != null) {
            return ctx.key_value().accept(this);
        } else if (ctx.table() != null) {
            return ctx.table().accept(this);
        } else if (ctx.comment() != null) {
            return null;
        }
        if (ctx.exception != null) {
            throw ctx.exception;
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitComment(TomlParserInternal.CommentContext ctx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object visitKey_value(TomlParserInternal.Key_valueContext ctx) {
        Object key = ctx.key().accept(this);
        Object value = ctx.value().accept(this);
        if (key instanceof List) {
            List<Object> keys = (List<Object>) key;
            HashMap<Object, Object> newTable = createNestedTables(currentTable, keys, true);
            newTable.put(keys.get(keys.size() - 1), value);
        } else {
            currentTable.put(key, value);
        }
        return key;
    }

    @Override
    public Object visitKey(TomlParserInternal.KeyContext ctx) {
        if (ctx.simple_key() != null) {
            return ctx.simple_key().accept(this);
        } else if (ctx.dotted_key() != null) {
            return ctx.dotted_key().accept(this);
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitSimple_key(TomlParserInternal.Simple_keyContext ctx) {
        if (ctx.quoted_key() != null) {
            return ctx.quoted_key().accept(this);
        } else if (ctx.unquoted_key() != null) {
            return ctx.unquoted_key().accept(this);
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitUnquoted_key(TomlParserInternal.Unquoted_keyContext ctx) {
        String UNQUOTED_KEY = ctx.UNQUOTED_KEY().getText();
        // Grammar makes it impossible for UNQUOTED_KEY to contain dots. Let's check it, anyway
        if (UNQUOTED_KEY.indexOf('.') != -1) {
            throw new IllegalStateException("Grammar shouldn't allow dots in UNQUOTED_KEY.");
        }
        return UNQUOTED_KEY.trim();
    }

    @Override
    public Object visitQuoted_key(TomlParserInternal.Quoted_keyContext ctx) {
        if (ctx.BASIC_STRING() != null) {
            String BASIC_STRING = ctx.BASIC_STRING().getText();
            // Remove quotes
            BASIC_STRING = BASIC_STRING.substring(1, BASIC_STRING.length() - 1);
            return unescapeTOMLString(BASIC_STRING);
        } else if (ctx.LITERAL_STRING() != null) {
            String LITERAL_STRING = ctx.LITERAL_STRING().getText();
            // Remove quotes
            LITERAL_STRING = LITERAL_STRING.substring(1, LITERAL_STRING.length() - 1);
            return LITERAL_STRING;
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitDotted_key(TomlParserInternal.Dotted_keyContext ctx) {
        return ctx.simple_key().stream().map(simple_key -> simple_key.accept(this)).collect(Collectors.toList());
    }

    @Override
    public Object visitValue(TomlParserInternal.ValueContext ctx) {
        if (ctx.string() != null) {
            return ctx.string().accept(this);
        } else if (ctx.integer() != null) {
            return ctx.integer().accept(this);
        } else if (ctx.floating_point() != null) {
            return ctx.floating_point().accept(this);
        } else if (ctx.bool_() != null) {
            return ctx.bool_().accept(this);
        } else if (ctx.date_time() != null) {
            return ctx.date_time().accept(this);
        } else if (ctx.array_() != null) {
            return ctx.array_().accept(this);
        } else if (ctx.inline_table() != null) {
            return ctx.inline_table().accept(this);
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    static final Pattern LINE_ENDING_BACKSLASH = Pattern.compile("\\\\\r?\\n[\\s]*");

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
    public String unescapeTOMLString(String st) {
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
                        sb.append(Character.toChars(code));
                        i += 9;
                        continue;
                    }
                }
                i++;
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    @Override
    public Object visitString(TomlParserInternal.StringContext ctx) {
        if (ctx.BASIC_STRING() != null) {
            String basicString = ctx.BASIC_STRING().getText();
            // Remove quotes from basicString
            basicString = basicString.substring(1, basicString.length() - 1);
            return unescapeTOMLString(basicString);
        } else if (ctx.ML_BASIC_STRING() != null) {
            String ML_BASIC_STRING = ctx.ML_BASIC_STRING().getText();
            ML_BASIC_STRING = ML_BASIC_STRING.substring(3, ML_BASIC_STRING.length() - 3);
            ML_BASIC_STRING = unescapeTOMLString(ML_BASIC_STRING);
            // ML_BASIC_STRING = LINE_ENDING_BACKSLASH.matcher(ML_BASIC_STRING).replaceAll("");
            // TODO: Review this.
            ML_BASIC_STRING = ML_BASIC_STRING.replaceAll("^\r?\n", "");
            return ML_BASIC_STRING;
        } else if (ctx.LITERAL_STRING() != null) {
            String LITERAL_STRING = ctx.LITERAL_STRING().getText();
            // Remove single quotes
            LITERAL_STRING = LITERAL_STRING.substring(1, LITERAL_STRING.length() - 1);
            LITERAL_STRING = LITERAL_STRING.replaceAll("^\r?\n", "");
            return LITERAL_STRING;
        } else if (ctx.ML_LITERAL_STRING() != null) {
            String ML_LITERAL_STRING = ctx.ML_LITERAL_STRING().getText();
            // Remove ''' at start & end
            ML_LITERAL_STRING = ML_LITERAL_STRING.substring(3, ML_LITERAL_STRING.length() - 3);
            ML_LITERAL_STRING = ML_LITERAL_STRING.replaceAll("^\r?\n", "");
            return ML_LITERAL_STRING;
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitInteger(TomlParserInternal.IntegerContext ctx) {
        int radix = 10;
        String longText = null;
        if (ctx.DEC_INT() != null) {
            longText = ctx.DEC_INT().getText();
            radix = 10;
        } else if (ctx.HEX_INT() != null) {
            longText = ctx.HEX_INT().getText().substring(2); // Remove leading '0x'
            radix = 16;
        } else if (ctx.OCT_INT() != null) {
            longText = ctx.OCT_INT().getText().substring(2); // Remove leading '0o'
            radix = 8;
        } else if (ctx.BIN_INT() != null) {
            longText = ctx.BIN_INT().getText().substring(2); // Remove leading '0b'
            radix = 2;
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        // Remove any '_'
        longText = longText.replace("_", "");
        return Long.valueOf(longText, radix);
    }

    @Override
    public Object visitFloating_point(TomlParserInternal.Floating_pointContext ctx) {
        if (ctx.NAN() != null) {
            return Double.NaN;
        } else if (ctx.INF() != null) {
            return (ctx.INF().getText().indexOf('-') == -1) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        }
        String doubleText = ctx.FLOAT().getText();
        doubleText = doubleText.replace("_", "");
        return Double.valueOf(doubleText);
    }

    @Override
    public Object visitBool_(TomlParserInternal.Bool_Context ctx) {
        String BOOLEAN = ctx.BOOLEAN().getText();
        return Boolean.valueOf(BOOLEAN);
    }

    private static final DateTimeFormatter DATETIME_WITH_SPACES = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Object visitDate_time(TomlParserInternal.Date_timeContext ctx) {
        if (ctx.OFFSET_DATE_TIME() != null) {
            String OFFSET_DATE_TIME = ctx.OFFSET_DATE_TIME().getText();
            OFFSET_DATE_TIME = OFFSET_DATE_TIME.replace(" ", "T");
            try {
                return OffsetDateTime.parse(OFFSET_DATE_TIME);
            } catch (DateTimeParseException dtpe) {
                return OffsetDateTime.from(DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(OFFSET_DATE_TIME));
            }
        } else if (ctx.LOCAL_DATE_TIME() != null) {
            String LOCAL_DATE_TIME = ctx.LOCAL_DATE_TIME().getText();
            try {
                return LocalDateTime.parse(LOCAL_DATE_TIME);
            } catch (DateTimeParseException dtpe) {
                return LocalDateTime.from(DATETIME_WITH_SPACES.parse(LOCAL_DATE_TIME));
            }
        } else if (ctx.LOCAL_DATE() != null) {
            String LOCAL_DATE = ctx.LOCAL_DATE().getText();
            return LocalDate.parse(LOCAL_DATE);
        } else if (ctx.LOCAL_TIME() != null) {
            String LOCAL_TIME = ctx.LOCAL_TIME().getText();
            return LocalTime.parse(LOCAL_TIME);
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitArray_(TomlParserInternal.Array_Context ctx) {
        ArrayList<Object> array = new ArrayList<Object>();
        arrayStack.push(array);
        if (ctx.array_values() != null) {
            ctx.array_values().accept(this);
        }
        arrayStack.pop();
        return array;
    }

    @Override
    public Object visitArray_values(TomlParserInternal.Array_valuesContext ctx) {
        ArrayList<Object> current = arrayStack.peek();
        if (ctx.value() != null) {
            current.add(ctx.value().accept(this));
        }
        if (ctx.array_values() != null) {
            ctx.array_values().accept(this);
        }
        return current;
    }

    @Override
    public Object visitComment_or_nl(TomlParserInternal.Comment_or_nlContext ctx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitTable(TomlParserInternal.TableContext ctx) {
        if (ctx.standard_table() != null) {
            return ctx.standard_table().accept(this);
        } else if (ctx.array_table() != null) {
            return ctx.array_table().accept(this);
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Creates a collection of nested tables given a set of keys.
     *
     * @param baseTable The base table
     * @param keys The keys
     * @param butLastKey true to avoid using the last key (possibly because it's
     * reserved for a literal)
     * @return The last table created
     * @throws ParseCancellationException if any of the keys is already reserved
     * for a non-table object
     */
    @SuppressWarnings("unchecked")
    private HashMap<Object, Object> createNestedTables(HashMap<Object, Object> baseTable, List<Object> keys, boolean butLastKey) {
        HashMap<Object, Object> previousTable = baseTable;
        int len = butLastKey ? keys.size() - 1 : keys.size();
        for (int i = 0; i < len; i++) {
            Object key = keys.get(i);
            Object o = previousTable.get(key);
            if (o == null) {
                HashMap<Object, Object> newTable = new HashMap<>();
                previousTable.put(key, newTable);
                previousTable = newTable;
            } else if (o instanceof Map) {
                previousTable = (HashMap<Object, Object>) o;
            } else if (o instanceof List) {
                // Is this an array table?
                List list = (List) o;
                Object last = list.get(list.size() - 1);
                if (!(last instanceof HashMap)) {
                    String message = String.format("Key '%s' in '%s' is already used for a non-table object",
                            key, keys.stream().map(Object::toString).collect(Collectors.joining(".")));
                    throw new ParseCancellationException(message);
                }
                previousTable = (HashMap<Object, Object>) last;
            } else {
                String message = String.format("Key '%s' in '%s' is already used for a non-table object",
                        key, keys.stream().map(Object::toString).collect(Collectors.joining(".")));
                throw new ParseCancellationException(message);
            }
        }
        return previousTable;

    }

    @Override
    @SuppressWarnings("unchecked")
    public Object visitStandard_table(TomlParserInternal.Standard_tableContext ctx) {
        Object newTableKey = ctx.key().accept(this);
        if (newTableKey instanceof List) {
            currentTable = createNestedTables(root, (List<Object>) newTableKey, false);
        } else {
            currentTable = createNestedTables(root, Arrays.asList(newTableKey), false);
        }
        return currentTable;
    }

    @SuppressWarnings("unchecked")
    private List<HashMap<Object, Object>> createArrayTable(HashMap<Object, Object> baseTable, List<Object> keys) {
        HashMap<Object, Object> previousTable = baseTable;
        if (keys.size() > 1) {
            for (int i = 0; i < keys.size() - 1; i++) {
                Object key = keys.get(i);
                Object o = previousTable.get(key);
                if (o == null) {
                    HashMap<Object, Object> newTable = new HashMap<>();
                    previousTable.put(key, newTable);
                    previousTable = newTable;
                } else if (o instanceof Map) {
                    previousTable = (HashMap<Object, Object>) o;
                } else if (o instanceof List) {
                    List list = (List) o;
                    Object last = list.get(list.size() - 1);
                    if (!(last instanceof HashMap)) {
                        String message = String.format("Key '%s' in '%s' is already used for a non-table object",
                                key, keys.stream().map(Object::toString).collect(Collectors.joining(".")));
                        throw new ParseCancellationException(message);
                    }
                    previousTable = (HashMap<Object, Object>) last;
                } else {
                    String message = String.format("Key '%s' in '%s' is already used for a non-table object",
                            key, keys.stream().map(Object::toString).collect(Collectors.joining(".")));
                    throw new ParseCancellationException(message);
                }
            }
        }
        Object lastKeyPart = keys.get(keys.size() - 1);
        List<HashMap<Object, Object>> listOfTables = (List<HashMap<Object, Object>>) previousTable.get(lastKeyPart);
        if (listOfTables == null) {
            listOfTables = new ArrayList<>();
            previousTable.put(lastKeyPart, listOfTables);
        } else if (!(listOfTables instanceof List)) {
            String message = String.format("Key '%s' in '%s' is already used for a non-table object",
                    lastKeyPart, keys.stream().map(Object::toString).collect(Collectors.joining(".")));
            throw new ParseCancellationException(message);
        }
        return listOfTables;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object visitArray_table(TomlParserInternal.Array_tableContext ctx) {
        Object newTableKey = ctx.key().accept(this);
        List<HashMap<Object, Object>> tableArray = null;
        if (newTableKey instanceof List) {
            tableArray = createArrayTable(root, (List) newTableKey);
        } else {
            tableArray = createArrayTable(root, Arrays.asList(newTableKey));
        }
        currentTable = new HashMap<>();
        tableArray.add(currentTable);
        return tableArray;
    }

    @Override
    public Object visit(ParseTree pt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitChildren(RuleNode rn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitTerminal(TerminalNode tn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object visitInline_table(TomlParserInternal.Inline_tableContext ctx) {
        HashMap<Object, Object> inlineTable = new HashMap<>();
        if (ctx.key() != null) {
            List<Object> keys = ctx.key().stream().map((k) -> k.accept(this)).collect(Collectors.toList());
            List<Object> values = ctx.inline_value().stream().map((v) -> v.accept(this)).collect(Collectors.toList());

            for (int i = 0; i < keys.size(); i++) {
                Object key = keys.get(i);
                Object value = values.get(i);

                if (key instanceof List) {
                    List<Object> dottedKey = (List<Object>) key;
                    HashMap<Object, Object> lastTable = createNestedTables(inlineTable, dottedKey, true);
                    lastTable.put(dottedKey.get(dottedKey.size() - 1), value);
                } else {
                    inlineTable.put(key, value);
                }
            }
        }
        return inlineTable;
    }

    @Override
    public Object visitInner_array(TomlParserInternal.Inner_arrayContext ctx) {
        if (ctx.inline_value() != null) {
            return ctx.inline_value().stream().map((v) -> v.accept(this)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public Object visitErrorNode(ErrorNode en) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitInline_value(TomlParserInternal.Inline_valueContext ctx) {
        if (ctx.string() != null) {
            return ctx.string().accept(this);
        } else if (ctx.integer() != null) {
            return ctx.integer().accept(this);
        } else if (ctx.floating_point() != null) {
            return ctx.floating_point().accept(this);
        } else if (ctx.bool_() != null) {
            return ctx.bool_().accept(this);
        } else if (ctx.date_time() != null) {
            return ctx.date_time().accept(this);
        } else if (ctx.inner_array() != null) {
            return ctx.inner_array().accept(this);
        } else if (ctx.inline_table() != null) {
            return ctx.inline_table().accept(this);
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
