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

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A mini-query language used to search stuff in the generated object tree,
 * without requiring an advanced expression library (OGNL, JXPath, etc.)
 */
final class TOMLSimpleQuery {
    
    @SuppressWarnings("unchecked")
    static <T> Optional<T> get(Map<String, Object> root, String path, Class<T> clazz) {
        Optional<T> NOT_FOUND = Optional.empty();
        String[] parts = path.split("/");
        Object context = root;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isBlank()) {
                continue;
            }
            if (context instanceof Map) {
                // Context is a map (TOML Table)
                Map<Object, Object> map = (Map<Object, Object>) context;
                if (!map.containsKey(part)) {
                    return NOT_FOUND;
                }
                /* @NonNull */ Object o = map.get(part);
                if (i < parts.length - 1) {
                    context = o;
                } else {
                    return clazz.isAssignableFrom(o.getClass()) ? Optional.of((T) o) : NOT_FOUND;
                }
            } else if (context instanceof List) {
                List<Object> list = (List<Object>) context;
                // Context is a tree, let's use positive / negative indexes to access the array
                try {
                    int index = Integer.parseInt(part);
                    if (index < 0) {
                        index = list.size() + index;
                    }
                    if (index >= 0 && index < list.size()) {
                        Object o = list.get(index);
                        if (i < parts.length - 1) {
                            context = o;
                        } else {
                            return clazz.isAssignableFrom(o.getClass()) ? Optional.of((T) o) : NOT_FOUND;
                        }
                    }
                } catch (NumberFormatException nfe) {
                    return NOT_FOUND;
                }
            } else {
                // Context is not a container, it's a scalar: we're done
                return NOT_FOUND;
            }
        }
        return NOT_FOUND;
    }

}
