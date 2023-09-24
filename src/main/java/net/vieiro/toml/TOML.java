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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * TOML is the result from parsing a TOML file.
 */
public final class TOML {

    final Map<Object, Object> root;
    final List<String> errors;

    TOML(Map<Object, Object> root, List<String> errors) {
        this.root = root;
        this.errors = Collections.unmodifiableList(errors);
    }

    public List<String> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return root == null ? "null" : root.toString();
    }

    private <T> Optional<T> get(String path, Class<T> clazz) {
        String[] parts = path.split("/");
        Map<Object, Object> map = root;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (!map.containsKey(part)) {
                return Optional.empty();
            }
            Object o = map.get(part);
            if (i < parts.length - 1) {
                if (!(o instanceof Map)) {
                    return Optional.empty();
                }
                map = (Map) o;
            } else {
                if (clazz.isAssignableFrom(o.getClass())) {
                    return Optional.of((T) o);
                }
            }
        }
        return Optional.empty();
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
     * Retrieves a list of tables from this TOML object.
     * @param path The path, separated by forward slashes, as in "a/b/c"
     * @return The List of tables, if any, or Optional.empty() otherwise.
     */
    public Optional<List<Map<Object, Object>>> getTableArray(String path) {
        Optional<List> list = getArray(path);
        if (list.isPresent()) {
            List<Map<Object, Object>> tables = (List<Map<Object, Object>>) list.get();
            return Optional.of(tables);
        }
        return Optional.empty();
    }

}
