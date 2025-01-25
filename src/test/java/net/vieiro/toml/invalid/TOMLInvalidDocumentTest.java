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
package net.vieiro.toml.invalid;

import net.vieiro.toml.TOML;
import net.vieiro.toml.Util;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 *
 */
public class TOMLInvalidDocumentTest {

    @ParameterizedTest()
    @ValueSource(strings = {
        "invalid/array/double-comma-1.toml",
        "invalid/array/double-comma-2.toml",
        "invalid/array/extending-table.toml",
        "invalid/array/missing-separator.toml",
        "invalid/array/no-close-2.toml",
        "invalid/array/no-close-table-2.toml",
        "invalid/array/no-close-table.toml",
        "invalid/array/no-close.toml",
        "invalid/array/tables-1.toml",
        "invalid/array/tables-2.toml",
        "invalid/array/text-after-array-entries.toml",
        "invalid/array/text-before-array-separator.toml",
        "invalid/array/text-in-array.toml",
        "invalid/bool/almost-false.toml",
        "invalid/bool/almost-false-with-extra.toml",
        "invalid/bool/almost-true.toml",
        "invalid/bool/almost-true-with-extra.toml",
        "invalid/bool/just-f.toml",
        "invalid/bool/just-t.toml",
        "invalid/bool/mixed-case.toml",
        "invalid/bool/starting-same-false.toml",
        "invalid/bool/starting-same-true.toml",
        "invalid/bool/wrong-case-false.toml",
        "invalid/bool/wrong-case-true.toml",
        "invalid/control/bare-cr.toml",
        "invalid/control/bare-formfeed.toml",
        "invalid/control/bare-null.toml",
        "invalid/control/bare-vertical-tab.toml",
        "invalid/control/comment-cr.toml",
        "invalid/control/comment-del.toml",
        "invalid/control/comment-lf.toml",
        "invalid/control/comment-null.toml",
        "invalid/control/comment-us.toml",
        "invalid/control/multi-del.toml",
        "invalid/control/multi-lf.toml",
        "invalid/control/multi-null.toml",
        "invalid/control/multi-us.toml",
        "invalid/control/rawmulti-del.toml",
        "invalid/control/rawmulti-lf.toml",
        "invalid/control/rawmulti-null.toml",
        "invalid/control/rawmulti-us.toml",
        "invalid/control/rawstring-del.toml",
        "invalid/control/rawstring-lf.toml",
        "invalid/control/rawstring-null.toml",
        "invalid/control/rawstring-us.toml",
        "invalid/control/string-bs.toml",
        "invalid/control/string-del.toml",
        "invalid/control/string-lf.toml",
        "invalid/control/string-null.toml",
        "invalid/control/string-us.toml",
        "invalid/datetime/hour-over.toml",
        "invalid/datetime/mday-over.toml",
        "invalid/datetime/mday-under.toml",
        "invalid/datetime/minute-over.toml",
        "invalid/datetime/month-over.toml",
        "invalid/datetime/month-under.toml",
        "invalid/datetime/no-leads.toml",
        "invalid/datetime/no-leads-with-milli.toml",
        "invalid/datetime/no-secs.toml",
        "invalid/datetime/no-t.toml",
        "invalid/datetime/second-over.toml",
        "invalid/datetime/time-no-leads-2.toml",
        "invalid/datetime/time-no-leads.toml",
        "invalid/datetime/trailing-t.toml",
        "invalid/encoding/bad-codepoint.toml",
        "invalid/encoding/bad-utf8-at-end.toml",
        "invalid/encoding/bad-utf8-in-comment.toml",
        "invalid/encoding/bad-utf8-in-multiline-literal.toml",
        "invalid/encoding/bad-utf8-in-multiline.toml",
        "invalid/encoding/bad-utf8-in-string-literal.toml",
        "invalid/encoding/bad-utf8-in-string.toml",
        "invalid/encoding/bom-not-at-start-1.toml",
        "invalid/encoding/bom-not-at-start-2.toml",
        "invalid/encoding/utf16-bom.toml",
        "invalid/encoding/utf16.toml",
        "invalid/float/double-point-1.toml",
        "invalid/float/double-point-2.toml",
        "invalid/float/exp-double-e-1.toml",
        "invalid/float/exp-double-e-2.toml",
        "invalid/float/exp-double-us.toml",
        "invalid/float/exp-leading-us.toml",
        "invalid/float/exp-point-1.toml",
        "invalid/float/exp-point-2.toml",
        "invalid/float/exp-trailing-us.toml",
        "invalid/float/inf-capital.toml",
        "invalid/float/inf-incomplete-1.toml",
        "invalid/float/inf-incomplete-2.toml",
        "invalid/float/inf-incomplete-3.toml",
        "invalid/float/inf_underscore.toml",
        "invalid/float/leading-point-neg.toml",
        "invalid/float/leading-point-plus.toml",
        "invalid/float/leading-point.toml",
        "invalid/float/leading-us.toml",
        "invalid/float/leading-zero-neg.toml",
        "invalid/float/leading-zero-plus.toml",
        "invalid/float/leading-zero.toml",
        "invalid/float/nan-capital.toml",
        "invalid/float/nan-incomplete-1.toml",
        "invalid/float/nan-incomplete-2.toml",
        "invalid/float/nan-incomplete-3.toml",
        "invalid/float/nan_underscore.toml",
        "invalid/float/trailing-point-min.toml",
        "invalid/float/trailing-point-plus.toml",
        "invalid/float/trailing-point.toml",
        "invalid/float/trailing-us-exp-1.toml",
        "invalid/float/trailing-us-exp-2.toml",
        "invalid/float/trailing-us.toml",
        "invalid/float/us-after-point.toml",
        "invalid/float/us-before-point.toml",
        "invalid/inline-table/add.toml",
        "invalid/inline-table/bad-key-syntax.toml",
        "invalid/inline-table/dotted-key-conflict.toml",
        "invalid/inline-table/double-comma.toml",
        "invalid/inline-table/duplicate-key.toml",
        "invalid/inline-table/empty.toml",
        "invalid/inline-table/linebreak-1.toml",
        "invalid/inline-table/linebreak-2.toml",
        "invalid/inline-table/linebreak-3.toml",
        "invalid/inline-table/linebreak-4.toml",
        "invalid/inline-table/nested_key_conflict.toml",
        "invalid/inline-table/no-comma.toml",
        "invalid/inline-table/overwrite.toml",
        "invalid/inline-table/trailing-comma.toml",
        "invalid/inline-table/unclosed-table.toml",
        "invalid/integer/capital-bin.toml",
        "invalid/integer/capital-hex.toml",
        "invalid/integer/capital-oct.toml",
        "invalid/integer/double-sign-nex.toml",
        "invalid/integer/double-sign-plus.toml",
        "invalid/integer/double-us.toml",
        "invalid/integer/incomplete-bin.toml",
        "invalid/integer/incomplete-hex.toml",
        "invalid/integer/incomplete-oct.toml",
        "invalid/integer/invalid-bin.toml",
        "invalid/integer/invalid-hex.toml",
        "invalid/integer/invalid-oct.toml",
        "invalid/integer/leading-us-bin.toml",
        "invalid/integer/leading-us-hex.toml",
        "invalid/integer/leading-us-oct.toml",
        "invalid/integer/leading-us.toml",
        "invalid/integer/leading-zero-1.toml",
        "invalid/integer/leading-zero-2.toml",
        "invalid/integer/leading-zero-3.toml",
        "invalid/integer/leading-zero-sign-1.toml",
        "invalid/integer/leading-zero-sign-2.toml",
        "invalid/integer/leading-zero-sign-3.toml",
        "invalid/integer/negative-bin.toml",
        "invalid/integer/negative-hex.toml",
        "invalid/integer/negative-oct.toml",
        "invalid/integer/positive-bin.toml",
        "invalid/integer/positive-hex.toml",
        "invalid/integer/positive-oct.toml",
        "invalid/integer/text-after-integer.toml",
        "invalid/integer/trailing-us-bin.toml",
        "invalid/integer/trailing-us-hex.toml",
        "invalid/integer/trailing-us-oct.toml",
        "invalid/integer/trailing-us.toml",
        "invalid/integer/us-after-bin.toml",
        "invalid/integer/us-after-hex.toml",
        "invalid/integer/us-after-oct.toml",
        "invalid/key/after-array.toml",
        "invalid/key/after-table.toml",
        "invalid/key/after-value.toml",
        "invalid/key/bare-invalid-character.toml",
        "invalid/key/dotted-redefine-table.toml",
        "invalid/key/duplicate-keys.toml",
        "invalid/key/duplicate.toml",
        "invalid/key/empty.toml",
        "invalid/key/escape.toml",
        "invalid/key/hash.toml",
        "invalid/key/multiline.toml",
        "invalid/key/newline.toml",
        "invalid/key/no-eol.toml",
        "invalid/key/open-bracket.toml",
        "invalid/key/partial-quoted.toml",
        "invalid/key/quoted-unclosed-1.toml",
        "invalid/key/quoted-unclosed-2.toml",
        "invalid/key/single-open-bracket.toml",
        "invalid/key/space.toml",
        "invalid/key/special-character.toml",
        "invalid/key/start-bracket.toml",
        "invalid/key/start-dot.toml",
        "invalid/key/two-equals2.toml",
        "invalid/key/two-equals3.toml",
        "invalid/key/two-equals.toml",
        "invalid/key/without-value-1.toml",
        "invalid/key/without-value-2.toml",
        "invalid/key/without-value-3.toml",
        "invalid/key/without-value-4.toml",
        "invalid/spec/inline-table-2-0.toml",
        "invalid/spec/inline-table-3-0.toml",
        "invalid/spec/keys-2.toml",
        "invalid/spec/key-value-pair-1.toml",
        "invalid/spec/string-4-0.toml",
        "invalid/spec/string-7-0.toml",
        "invalid/spec/table-9-0.toml",
        "invalid/spec/table-9-1.toml",
        "invalid/string/bad-byte-escape.toml",
        "invalid/string/bad-codepoint.toml",
        "invalid/string/bad-concat.toml",
        "invalid/string/bad-escape-1.toml",
        "invalid/string/bad-escape-2.toml",
        "invalid/string/bad-hex-esc-1.toml",
        "invalid/string/bad-hex-esc-2.toml",
        "invalid/string/bad-hex-esc-3.toml",
        "invalid/string/bad-hex-esc-4.toml",
        "invalid/string/bad-hex-esc-5.toml",
        "invalid/string/bad-multiline.toml",
        "invalid/string/bad-slash-escape.toml",
        "invalid/string/bad-uni-esc-1.toml",
        "invalid/string/bad-uni-esc-2.toml",
        "invalid/string/bad-uni-esc-3.toml",
        "invalid/string/bad-uni-esc-4.toml",
        "invalid/string/bad-uni-esc-5.toml",
        "invalid/string/basic-byte-escapes.toml",
        "invalid/string/basic-multiline-out-of-range-unicode-escape-1.toml",
        "invalid/string/basic-multiline-out-of-range-unicode-escape-2.toml",
        "invalid/string/basic-multiline-quotes.toml",
        "invalid/string/basic-multiline-unknown-escape.toml",
        "invalid/string/basic-out-of-range-unicode-escape-1.toml",
        "invalid/string/basic-out-of-range-unicode-escape-2.toml",
        "invalid/string/basic-unknown-escape.toml",
        "invalid/string/literal-multiline-quotes-1.toml",
        "invalid/string/literal-multiline-quotes-2.toml",
        "invalid/string/missing-quotes.toml",
        "invalid/string/multiline-bad-escape-1.toml",
        "invalid/string/multiline-bad-escape-2.toml",
        "invalid/string/multiline-bad-escape-3.toml",
        "invalid/string/multiline-escape-space.toml",
        "invalid/string/multiline-no-close-2.toml",
        "invalid/string/multiline-no-close.toml",
        "invalid/string/multiline-quotes-1.toml",
        "invalid/string/no-close.toml",
        "invalid/string/text-after-string.toml",
        "invalid/string/wrong-close.toml",
        "invalid/table/append-to-array-with-dotted-keys.toml",
        // "invalid/table/append-with-dotted-keys-1.toml", Discarded. See comment in TOML file.
        // "invalid/table/append-with-dotted-keys-2.toml", Discarded. See comment in TOML file.
        "invalid/table/array-empty.toml",
        "invalid/table/array-implicit.toml",
        "invalid/table/array-missing-bracket.toml",
        "invalid/table/duplicate-key-dotted-array.toml",
        "invalid/table/duplicate-key-dotted-table2.toml",
        "invalid/table/duplicate-key-dotted-table.toml",
        "invalid/table/duplicate-key-table.toml",
        "invalid/table/duplicate-table-array2.toml",
        "invalid/table/duplicate-table-array.toml",
        "invalid/table/duplicate.toml",
        "invalid/table/empty-implicit-table.toml",
        "invalid/table/empty.toml",
        "invalid/table/equals-sign.toml",
        "invalid/table/llbrace.toml",
        "invalid/table/nested-brackets-close.toml",
        "invalid/table/nested-brackets-open.toml",
        "invalid/table/quoted-no-close.toml",
        "invalid/table/redefine.toml",
        "invalid/table/rrbrace.toml",
        "invalid/table/text-after-table.toml",
        "invalid/table/whitespace.toml",
        "invalid/table/with-pound.toml",})
    public void testShouldDetectErrorsInInvalidTOMLDocuments(String tomlInvalidDocumentName) {
        System.out.format("testShouldDetectErrorsInInvalidTOMLDocuments - %s%n", tomlInvalidDocumentName);

        boolean verbose = false;

        // Given a TOML document with errors
        TOML toml = null;

        try {
            toml = Util.parse(tomlInvalidDocumentName, verbose, true);
        } catch (Exception e) {
            String message = String.format("Test '%s failed with exception %s:%s", tomlInvalidDocumentName, e.getMessage(), e.getClass().getName());
            fail(message, e);
        }

        // Then the number of detected errors should be equal to the number of expected errors.
        int numberOfDetectedErrors = toml.getErrors().size();

        if (numberOfDetectedErrors > 0) {
            if (verbose) {
                for (String error : toml.getErrors()) {
                    System.out.format("  - ERROR: %s: %s%n", tomlInvalidDocumentName, error);
                }
            }
        } else {
            fail(String.format("  Test %s must fail, but doesn't", tomlInvalidDocumentName));
        }
    }

    @ParameterizedTest()
    @ValueSource(strings = {
        "invalid/inline-table/unclosed-table.toml",
    })
    public void testShouldBehaveOnUnclosedTable(String tomlInvalidDocumentName) {
        System.out.format("testShouldDetectErrorsInInvalidTOMLDocuments - %s%n", tomlInvalidDocumentName);

        boolean verbose = true;

        // Given a TOML document with errors
        TOML toml = null;

        try {
            toml = Util.parse(tomlInvalidDocumentName, verbose, true);
        } catch (Exception e) {
            String message = String.format("Test '%s failed with exception %s:%s", tomlInvalidDocumentName, e.getMessage(), e.getClass().getName());
            fail(message, e);
        }

        // Then the number of detected errors should be equal to the number of expected errors.
        int numberOfDetectedErrors = toml.getErrors().size();

        if (numberOfDetectedErrors > 0) {
            if (verbose) {
                for (String error : toml.getErrors()) {
                    System.out.format("  - ERROR: %s: %s%n", tomlInvalidDocumentName, error);
                }
            }
        } else {
            fail(String.format("  Test %s must fail, but doesn't", tomlInvalidDocumentName));
        }
    }

}
