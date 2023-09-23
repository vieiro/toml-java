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
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import net.vieiro.toml.antlr4.TomlParserInternal;
import net.vieiro.toml.antlr4.TomlParserInternalVisitor;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
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

    public TOMLVisitor() {
        this.errors = new ArrayList<String>();
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
    public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean bln, BitSet bitset, ATNConfigSet atncs) {
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitComment(TomlParserInternal.CommentContext ctx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitKey_value(TomlParserInternal.Key_valueContext ctx) {
        Object key = ctx.key().accept(this);
        Object value = ctx.value().accept(this);
        currentTable.put(key, value);
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
        return UNQUOTED_KEY;
    }

    @Override
    public Object visitQuoted_key(TomlParserInternal.Quoted_keyContext ctx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitDotted_key(TomlParserInternal.Dotted_keyContext ctx) {
        throw new UnsupportedOperationException("Not supported yet.");
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

    @Override
    public Object visitString(TomlParserInternal.StringContext ctx) {
        if (ctx.BASIC_STRING() != null) {
            String basicString = ctx.BASIC_STRING().getText();
            // Remove quotes from basicString
            return basicString.substring(1, basicString.length() - 1);
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitBool_(TomlParserInternal.Bool_Context ctx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitDate_time(TomlParserInternal.Date_timeContext ctx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitArray_(TomlParserInternal.Array_Context ctx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitArray_values(TomlParserInternal.Array_valuesContext ctx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitComment_or_nl(TomlParserInternal.Comment_or_nlContext ctx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitNl_or_comment(TomlParserInternal.Nl_or_commentContext ctx) {
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

    @Override
    public Object visitStandard_table(TomlParserInternal.Standard_tableContext ctx) {
        Object newTableKey = ctx.key().accept(this);
        HashMap<Object, Object> newTable = new HashMap<>();
        root.put(newTableKey, newTable);
        currentTable = newTable;
        return newTable;
    }

    @Override
    public Object visitInline_table(TomlParserInternal.Inline_tableContext ctx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitInline_table_keyvals(TomlParserInternal.Inline_table_keyvalsContext ctx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitInline_table_keyvals_non_empty(TomlParserInternal.Inline_table_keyvals_non_emptyContext ctx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitArray_table(TomlParserInternal.Array_tableContext ctx) {
        throw new UnsupportedOperationException("Not supported yet.");
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

}
