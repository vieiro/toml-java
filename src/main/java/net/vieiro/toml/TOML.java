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
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * TOML is the result from parsing a TOML file. This class includes some methods
 * for querying the generated object-tree, for more advanced querying consider
 * using a specific object-tree querying library such as OGNL, Apache Commons
 * EL, etc.
 */
public final class TOML {

    final Map<String, Object> root;
    final List<String> errors;

    TOML(Map<String, Object> root, List<String> errors) {
        this.root = root;
        this.errors = Collections.unmodifiableList(errors);
    }

    /**
     * Returns a list of the syntax errors generated during parsing.
     *
     * @return A list of errors generated during parsing.
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Returns the root table representing the document.
     *
     * @return The root table representing the parsed TOML document.
     */
    public Map<String, Object> getRoot() {
        return root;
    }

    /**
     * Prints out the object tree. For debugging purposes.
     *
     * @return A String representation of the object tree.
     */
    @Override
    public String toString() {
        return root == null ? "null" : root.toString();
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> get(String path, Class<T> clazz) {
        return TOMLSimpleQuery.get(root, path, clazz);
    }

    /**
     * Retrieves a String value from this TOML object.
     *
     * @param path The path, separated by forward slashes, as in "a/b/c"
     * @return The String value, if any, or Optional.empty() otherwise.
     */
    public Optional<String> getString(String path) {
        return get(path, String.class);
    }

    /**
     * Retrieves a Long value from this TOML object.
     *
     * @param path The path, separated by forward slashes, as in "a/b/c"
     * @return The Long value, if any, or Optional.empty() otherwise.
     */
    public Optional<Long> getLong(String path) {
        return get(path, Long.class);
    }

    /**
     * Retrieves a Double value from this TOML object.
     *
     * @param path The path, separated by forward slashes, as in "a/b/c"
     * @return The Double value, if any, or Optional.empty() otherwise.
     */
    public Optional<Double> getDouble(String path) {
        return get(path, Double.class);
    }

    /**
     * Retrieves a List value from this TOML object.
     *
     * @param path The path, separated by forward slashes, as in "a/b/c"
     * @return The List value, if any, or Optional.empty() otherwise.
     */
    public Optional<List> getArray(String path) {
        return get(path, List.class);
    }

    /**
     * Retrieves a Boolean value from this TOML object.
     *
     * @param path The path, separated by forward slashes, as in "a/b/c"
     * @return The Boolean value, if any, or Optional.empty() otherwise.
     */
    public Optional<Boolean> getBoolean(String path) {
        return get(path, Boolean.class);
    }

    /**
     * Retrieves a Instant value from this TOML object.
     *
     * @param path The path, separated by forward slashes, as in "a/b/c"
     * @return The Instant value, if any, or Optional.empty() otherwise.
     */
    public Optional<Instant> getInstant(String path) {
        return get(path, Instant.class);
    }

    /**
     * Retrieves a OffsetDateTime value from this TOML object.
     *
     * @param path The path, separated by forward slashes, as in "a/b/c"
     * @return The OffsetDateTime value, if any, or Optional.empty() otherwise.
     */
    public Optional<OffsetDateTime> getOffsetDateTime(String path) {
        return get(path, OffsetDateTime.class);
    }

    /**
     * Retrieves a LocalDateTime value from this TOML object.
     *
     * @param path The path, separated by forward slashes, as in "a/b/c"
     * @return The LocalDateTime value, if any, or Optional.empty() otherwise.
     */
    public Optional<LocalDateTime> getLocalDateTime(String path) {
        return get(path, LocalDateTime.class);
    }

    /**
     * Retrieves a LocalDate value from this TOML object.
     *
     * @param path The path, separated by forward slashes, as in "a/b/c"
     * @return The LocalDate value, if any, or Optional.empty() otherwise.
     */
    public Optional<LocalDate> getLocalDate(String path) {
        return get(path, LocalDate.class);
    }

    /**
     * Retrieves a LocalTime value from this TOML object.
     *
     * @param path The path, separated by forward slashes, as in "a/b/c"
     * @return The LocalTime value, if any, or Optional.empty() otherwise.
     */
    public Optional<LocalTime> getLocalTime(String path) {
        return get(path, LocalTime.class);
    }

    /**
     * Retrieves a table from this TOML object.
     *
     * @param path The path, separated by forward slashes, as in "a/b/c"
     * @return The Map stored in the given path, or Optional.empty() otherwise.
     */
    @SuppressWarnings("unchecked")
    public Optional<Map<Object, Object>> getTable(String path) {
        Optional<Map> map = get(path, Map.class);
        if (map.isPresent()) {
            Map<Object, Object> mapOO = (Map<Object, Object>) map.get();
            return Optional.of(mapOO);
        }
        return Optional.empty();
    }

    /**
     * Retrieves a list of tables from this TOML object.
     *
     * @param path The path, separated by forward slashes, as in "a/b/c"
     * @return The List of tables, if any, or Optional.empty() otherwise.
     */
    @SuppressWarnings("unchecked")
    public Optional<List<Map<Object, Object>>> getTableArray(String path) {
        Optional<List> list = getArray(path);
        if (list.isPresent()) {
            List<Map<Object, Object>> tables = (List<Map<Object, Object>>) list.get();
            return Optional.of(tables);
        }
        return Optional.empty();
    }

    /**
     * Utility method to write a JSON representation of the Java object tree.
     *
     * @param out The Writer where the JSON representation is expected.
     * @throws IOException If an I/O error happens.
     */
    public void writeJSON(Writer out) throws IOException {
        TOMLJSON.write(root, out, false);
    }

    /**
     * Utility method to write a JSON representation of the Java object tree.
     *
     * @param out The Writer where the JSON representation is expected.
     * @throws IOException If an I/O error happens.
     */
    public void writeJSON(PrintStream out) throws IOException {
        PrintWriter pw = new PrintWriter(out) {
            @Override
            public void close() {
                // empty
            }
        };
        writeJSON(pw);
        pw.flush();
    }

}
