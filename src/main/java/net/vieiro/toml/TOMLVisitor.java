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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.vieiro.toml.antlr4.TomlParserInternal;
import net.vieiro.toml.antlr4.TomlParserInternalVisitor;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 *
 */
public class TOMLVisitor implements ANTLRErrorListener, TomlParserInternalVisitor<Object> {

    private static final Level LEVEL = Level.FINE;

    private static final Logger LOG = Logger.getLogger(TOMLVisitor.class.getName());

    private HashSet<List<String>> standardTableKeys;

    private List<String> errors;
    private Map<String, Object> root;
    private Stack<List<Object>> arrayStack;
    private Map<String, Object> currentTable;

    public TOMLVisitor() {
        errors = new ArrayList<>();
        reset();
    }

    private void reset() {
        root = new HashMap<>();
        arrayStack = new Stack<>();
        currentTable = root;
        standardTableKeys = new HashSet<>();
    }

    public List<String> getErrors() {
        return errors;
    }

    public Map<String, Object> getRoot() {
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
        reset();
        if (ctx.expression() != null) {
            for (ParserRuleContext expression : ctx.expression()) {
                expression.accept(this);
            }
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
        throw newPCException(ctx, "Parser does not recognize this expression type");
    }

    @Override
    public Object visitComment(TomlParserInternal.CommentContext ctx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitKey(TomlParserInternal.KeyContext ctx) {
        if (ctx.simple_key() != null) {
            return ctx.simple_key().accept(this);
        } else if (ctx.dotted_key() != null) {
            return ctx.dotted_key().accept(this);
        }
        throw newPCException(ctx, String.format("Unknown key type '%s'", ctx.getText()));
    }

    @Override
    public Object visitSimple_key(TomlParserInternal.Simple_keyContext ctx) {
        if (ctx.quoted_key() != null) {
            return ctx.quoted_key().accept(this);
        } else if (ctx.unquoted_key() != null) {
            return ctx.unquoted_key().accept(this);
        }
        throw newPCException(ctx, "Unrecognized key type");
    }

    @Override
    public Object visitUnquoted_key(TomlParserInternal.Unquoted_keyContext ctx) {
        return TOMLStringVisitor.visitUnquoted_key(ctx);
    }

    @Override
    public Object visitQuoted_key(TomlParserInternal.Quoted_keyContext ctx) {
        return TOMLStringVisitor.visitQuoted_key(ctx);
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
        throw newPCException(ctx, "Value could not be parsed");
    }

    @Override
    public Object visitString(TomlParserInternal.StringContext ctx) {
        return TOMLStringVisitor.visitString(ctx);
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
        try {
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
        } catch (Exception e) {
            throw newPCException(ctx, e);
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object visitInline_table(TomlParserInternal.Inline_tableContext ctx) {
        Map<String, Object> inlineTable = new HashMap<>();
        if (ctx.key() != null) {
            List<List<String>> listOfKeys = ctx.key().stream()
                    .map((k) -> k.accept(this))
                    .map(TOMLVisitor::toKey)
                    .collect(Collectors.toList());
            // Check for repeated or redefinition of keys
            boolean equalOrRdefined = false;
            for (int i = 0; i < listOfKeys.size() && !equalOrRdefined; i++) {
                List<String> keyI = listOfKeys.get(i);
                for (int j = i + 1; j < listOfKeys.size() && !equalOrRdefined; j++) {
                    List<String> keyJ = listOfKeys.get(j);
                    if (isPrefix(keyI, keyJ)) {
                        throw newPCException(ctx, String.format("Redefinition of keys %s and %s", keyI, keyJ));
                    }
                }
            }

            // Compute the list of values
            List<Object> listOfValues = ctx.inline_value().stream().map((v) -> v.accept(this)).collect(Collectors.toList());
            // Check we have the same size of listOfKeys and listOfValues
            if (listOfKeys.size() != listOfValues.size()) {
                throw newPCException(ctx, String.format("Table does not have same number of keys (%d) and values (%d)",
                        listOfKeys.size(), listOfValues.size()));
            }
            for (int i = 0; i < listOfKeys.size(); i++) {
                List<String> key = listOfKeys.get(i);
                Object value = listOfValues.get(i);
                Map<String, Object> table = getTableForKeyValue(ctx, inlineTable, key, value);
            }
        }
        // Inline tables are immutable and cannot be extended
        return Collections.unmodifiableMap(inlineTable);
    }

    @Override
    public Object visitInner_array(TomlParserInternal.Inner_arrayContext ctx) {
        if (ctx.inline_value() != null) {
            List<Object> innerArray = ctx.inline_value().stream().map((v) -> v.accept(this)).collect(Collectors.toList());
            return Collections.unmodifiableList(innerArray);
        }
        return Collections.emptyList();
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
        throw newPCException(ctx, "Unknown inline value type");
    }

    @Override
    public Object visitArray_(TomlParserInternal.Array_Context ctx) {
        ArrayList<Object> array = new ArrayList<Object>();
        arrayStack.push(array);
        if (ctx.array_values() != null) {
            ctx.array_values().accept(this);
        }
        arrayStack.pop();
        return Collections.unmodifiableList(array);
    }

    @Override
    public Object visitArray_values(TomlParserInternal.Array_valuesContext ctx) {
        List<Object> current = arrayStack.peek();
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
        throw newPCException(ctx, "Unkonwn table type in parser");
    }

    @Override
    public Object visitKey_value(TomlParserInternal.Key_valueContext ctx) {
        List<String> key = toKey(ctx.key().accept(this));
        LOG.log(LEVEL, "Visiting key-value with key {0}", key);

        // If this is a dotted key, do we overwrite an o value?
        if (ctx.exception != null) {
            throw newPCException(ctx, ctx.exception);
        }
        if (ctx.value() == null) {
            throw newPCException(ctx, "Unkonwn table type in parser");
        }
        Object value = ctx.value().accept(this);
        Map<String, Object> table = getTableForKeyValue(ctx, key, value);
        return key;
    }

    /**
     * Visiting a new standard table (such as "[a]" or "[a.b.c]" creates a new
     * key context. Of course, new entries cannot overwrite previously created
     * entries.
     *
     * @param ctx The context
     * @return The newly created table
     */
    @Override
    public Object visitStandard_table(TomlParserInternal.Standard_tableContext ctx) {
        List<String> key = toKey(ctx.key().accept(this));
        LOG.log(LEVEL, "Visiting standard table with key {0}", key);

        Map<String, Object> newTable = createStandardTable(ctx, key);
        currentTable = newTable;

        standardTableKeys.add(key);

        return newTable;
    }

    @Override
    public Object visitArray_table(TomlParserInternal.Array_tableContext ctx) {
        List<String> key = toKey(ctx.key().accept(this));
        LOG.log(LEVEL, "Visiting standard table with key {0}", key);

        Map<String, Object> newTable = createArrayTable(ctx, key);
        currentTable = newTable;

        standardTableKeys.add(key);

        return newTable;
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
    public Object visitErrorNode(ErrorNode en) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //----------------------------------------------------------------------
    // Utility methods
    static ParseCancellationException newPCException(ParserRuleContext ctx, String errorDescription) {
        Token start = ctx.start;
        Token stop = ctx.stop;
        if (start.getLine() >= stop.getLine()) {
            if (start.getCharPositionInLine() > stop.getCharPositionInLine()) {
                Token swap = start;
                start = stop;
                stop = swap;
            }
        }
        String message = String.format("%s from line:col %d:%d to %d:%d",
                errorDescription,
                start.getLine(), start.getCharPositionInLine(),
                stop.getLine(), stop.getCharPositionInLine());
        return new ParseCancellationException(message);
    }

    static ParseCancellationException newPCException(ParserRuleContext ctx, Exception exception) {
        return newPCException(ctx, exception.getMessage());
    }

    private List<String> combineKeys(List<String> a, List<String> b) {
        return Stream.concat(a.stream(), b.stream()).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private static List<String> toKey(Object o) {
        return (o instanceof List) ? (List<String>) o : Arrays.asList(o.toString());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getTableForKeyValue(ParserRuleContext ctx, List<String> key, Object value) {
        return getTableForKeyValue(ctx, currentTable, key, value);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getTableForKeyValue(ParserRuleContext ctx, Map<String, Object> container, List<String> key, Object value) {
        // Check if this is a dotted key trying to explicitly adding to a standard table
        if (key.size() > 1) {
            List<String> prefix = key.subList(0, key.size() - 1);
            if (standardTableKeys.contains(prefix)) {
                String message = String.format("Using dotted keys to add to %s after defining it is not allowed", prefix);
                throw newPCException(ctx, message);
            }
        }
        for (int i = 0; i < key.size() - 1; i++) {
            String part = key.get(i);
            Object o = container.get(part);
            if (o == null) {
                // Add a new table
                HashMap<String, Object> newTable = new HashMap<>();
                container.put(part, newTable);
                container = newTable;
            } else if (o instanceof Map) {
                // Use o table
                container = (Map<String, Object>) o;
            } else if (o instanceof List) {
                String message = String.format("Can't redefinee existing key %s (currently a list)", key);
                throw newPCException(ctx, message);
            } else {
                String message = String.format("Key part '%s' in '%s' refers to an existing scalar", part, key);
                throw newPCException(ctx, message);
            }
        }
        String lastKeyPart = key.get(key.size() - 1);
        if (container.containsKey(lastKeyPart)) {
            String message = String.format("Can't ovewrite existing table with key %s", key);
            throw newPCException(ctx, message);
        }
        try {
            container.put(lastKeyPart, value);
        } catch (UnsupportedOperationException uoe) {
            String message = String.format("Can't ovewrite existing table with key %s", key);
            throw newPCException(ctx, message);
        }
        return container;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> createStandardTable(TomlParserInternal.Standard_tableContext ctx, List<String> key) {
        Map<String, Object> container = root;
        for (int i = 0; i < key.size() - 1; i++) {
            String part = key.get(i);
            Object o = container.get(part);
            if (o == null) {
                // Add a new table
                HashMap<String, Object> newTable = new HashMap<>();
                container.put(part, newTable);
                container = newTable;
            } else if (o instanceof Map) {
                // Use o table
                container = (Map<String, Object>) o;
            } else if (o instanceof List) {
                List<Map<String, Object>> arrayTable = (List<Map<String, Object>>) o;
                container = arrayTable.get(arrayTable.size() - 1);
            } else {
                String message = String.format("Key '%s' for a scalar value cannot be overwritten with key %s",
                        part, key);
                throw newPCException(ctx, message);
            }
        }
        String lastKeyPart = key.get(key.size() - 1);
        if (container.containsKey(lastKeyPart)) {
            /*

            This is an incongruence in the TOML specification?

            This document is VALID:

            ```toml
            [fruit.apple]
            x=43

            [fruit] <- fruit already exists and is a valid key
            apple.color = "red"
            apple.taste.sweet = true
            ```

            But this document is not valid:

            ```toml
            [fruit]
            apple.color = "red"
            apple.taste.sweet = true

            [fruit.apple] <- fruit.apple already exists and is NOT a valid key
            x=43
            ```

            The only difference is that in the first case (valid) "fruit" is already
            a part of an existing standard table with name "fruit.apple", and
            in the second case "fruit.apple" is not an standard table key.

             */

            if (!keyIsSuffixOfStandardTable(key)) {
                String message = String.format("Can't ovewrite existing standard table with key %s", key);
                throw newPCException(ctx, message);
            }

            Object o = container.get(lastKeyPart);
            if (!(o instanceof Map)) {
                String message = String.format("Can't ovewrite existing table with key %s", key);
                throw newPCException(ctx, message);
            }
            container = (Map<String, Object>) o;
            return container;
        }
        HashMap<String, Object> newTable = new HashMap<>();
        try {
            container.put(lastKeyPart, newTable);
        } catch (UnsupportedOperationException uoe) {
            String message = String.format("Can't ovewrite existing table with key %s", key);
            throw newPCException(ctx, message);
        }
        return newTable;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> createArrayTable(TomlParserInternal.Array_tableContext ctx, List<String> key) {
        Map<String, Object> container = root;
        for (int i = 0; i < key.size() - 1; i++) {
            String part = key.get(i);
            Object o = container.get(part);
            if (o == null) {
                // Add a new table
                HashMap<String, Object> newTable = new HashMap<>();
                container.put(part, newTable);
                container = newTable;
            } else if (o instanceof Map) {
                // Use o table
                container = (Map<String, Object>) o;
            } else if (o instanceof List) {
                List<Map<String, Object>> arrayTable = (List<Map<String, Object>>) o;
                container = arrayTable.get(arrayTable.size() - 1);
            } else {
                String message = String.format("Key '%s' for a scalar value cannot be overwritten with key %s",
                        part, key);
                throw newPCException(ctx, message);
            }
        }
        String lastKeyPart = key.get(key.size() - 1);
        List<Map<String, Object>> arrayTable = null;
        if (container.containsKey(lastKeyPart)) {
            // The array exists
            Object o = container.get(lastKeyPart);
            if (!(o instanceof List)) {
                String message = String.format("Can't ovewrite existing table with key %s", key);
                throw newPCException(ctx, message);
            }
            arrayTable = (List<Map<String, Object>>) o;
        } else {
            arrayTable = new ArrayList<Map<String, Object>>();
            try {
                container.put(lastKeyPart, arrayTable);
            } catch (UnsupportedOperationException uoe) {
                String message = String.format("Can't ovewrite existing table with key %s", key);
                throw newPCException(ctx, message);
            }
        }
        HashMap<String, Object> newTable = new HashMap<>();
        try {
            arrayTable.add(newTable);
        } catch (UnsupportedOperationException uoe) {
            String message = String.format("Can't ovewrite existing table with key %s", key);
            throw newPCException(ctx, message);
        }
        return newTable;
    }

    static boolean isPrefix(List<String> min, List<String> max) {
        if (min.size() > max.size()) {
            return isPrefix(max, min);
        }
        boolean equalElements = true;
        for (int i = 0; i < min.size() && equalElements; i++) {
            equalElements = equalElements && min.get(i).equals(max.get(i));
        }
        return equalElements;
    }

    /**
     * Detect if a key such as "fruits.apple" is a suffix of any other standard
     * key name (such as "fruits.apple.orange".
     *
     * @param key The key
     * @return true if the given key is a suffix of any other standard key,
     * false otherwise
     */
    private boolean keyIsSuffixOfStandardTable(List<String> key) {
        for (List<String> standardKey : standardTableKeys) {
            if (key.size() < standardKey.size()) {
                // Here is a candidate
                boolean isSuffix = true;
                for (int i = 0; i < key.size() && isSuffix; i++) {
                    isSuffix &= key.get(i).equals(standardKey.get(i));
                }
                if (isSuffix) {
                    return true;
                }
            }
        }
        return false;
    }

}
