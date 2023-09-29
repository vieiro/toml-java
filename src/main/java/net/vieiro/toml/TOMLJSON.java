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
import java.io.Writer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Internal class for dumping a TOML generated Java object tree to JSON, with
 * minimum external dependencies.
 */
final class TOMLJSON {

    @SuppressWarnings("unchecked")
    static void write(Object object, Writer out, boolean quotes) throws IOException {
        if (object == null) {
            out.write("null");
        } else if (object instanceof Map) {
            writeMap((Map) object, out);
        } else if (object instanceof List) {
            writeList((List) object, out);
        } else {
            writeLiteral(object, out, quotes);
        }
    }

    @SuppressWarnings("unchecked")
    private static void writeMap(Map<Object, Object> table, Writer out) throws IOException {
        out.write('{');
        int i = 0;
        int n = table.entrySet().size();
        for (Map.Entry<Object, Object> entry : table.entrySet()) {
            write(entry.getKey(), out, true);
            out.write(':');
            write(entry.getValue(), out, false);
            if (i < n - 1) {
                out.write(',');
            }
            i++;
        }
        out.write('}');
    }

    @SuppressWarnings("unchecked")
    private static void writeList(List<Object> list, Writer out) throws IOException {
        out.write('[');
        int n = list.size();
        for (int i = 0; i < list.size(); i++) {
            write(list.get(i), out, false);
            if (i < n - 1) {
                out.write(',');
            }
        }
        out.write(']');
    }

    @SuppressWarnings("unchecked")
    private static void writeLiteral(Object o, Writer out, boolean quotes) throws IOException {
        if (quotes) {
            out.write('\"');
        }
        if (o instanceof String) {
            if (!quotes) {
                out.write('\"');
            }
            writeString((String) o, out);
            if (!quotes) {
                out.write('\"');
            }
        } else if (o instanceof Long) {
            out.write(Long.toString((Long) o));
        } else if (o instanceof Double) {
            Double d = (Double) o;
            if (Double.isNaN(d)) {
                out.write("null");
            } else if (d.isInfinite()) {
                out.write(d == Double.POSITIVE_INFINITY ? "null" : "null");
            } else {
                out.write(Double.toString(d));
            }
        } else if (o instanceof Boolean) {
            out.write(Boolean.toString((Boolean) o));
        } else if (o instanceof OffsetDateTime) {
            if (!quotes) {
                out.write('\"');
            }
            out.write(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format((OffsetDateTime) o));
            if (!quotes) {
                out.write('\"');
            }
        } else if (o instanceof LocalDateTime) {
            if (!quotes) {
                out.write('\"');
            }
            out.write(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format((LocalDateTime) o));
            if (!quotes) {
                out.write('\"');
            }
        } else if (o instanceof LocalDate) {
            if (!quotes) {
                out.write('\"');
            }
            out.write(DateTimeFormatter.ISO_LOCAL_DATE.format((LocalDate) o));
            if (!quotes) {
                out.write('"');
            }
        } else if (o instanceof LocalTime) {
            if (!quotes) {
                out.write('"');
            }
            out.write(DateTimeFormatter.ISO_LOCAL_TIME.format((LocalTime) o));
            if (!quotes) {
                out.write('"');
            }
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        if (quotes) {
            out.write('"');
        }
    }

    /**
     * Writes a String in JSON format. Adapted from
     * https://github.com/fangyidong/json-simple/blob/2f4b7b5bed38d7518bf9c6a902ea909226910ae3/src/main/java/org/json/simple/JSONValue.java#L270
     * With APLv2 license
     *
     * @param s - The string (not null)
     * @param out - The output
     */
    private static void writeString(String s, Writer sb) throws IOException {
        final int len = s.length();
        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"':
                    sb.write("\\\"");
                    break;
                case '\\':
                    sb.write("\\\\");
                    break;
                case '\b':
                    sb.write("\\b");
                    break;
                case '\f':
                    sb.write("\\f");
                    break;
                case '\n':
                    sb.write("\\n");
                    break;
                case '\r':
                    sb.write("\\r");
                    break;
                case '\t':
                    sb.write("\\t");
                    break;
                case '/':
                    sb.write("\\/");
                    break;
                default:
                    //Reference: http://www.unicode.org/versions/Unicode5.1.0/
                    if ((ch >= '\u0000' && ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F') || (ch >= '\u2000' && ch <= '\u20FF')) {
                        String ss = Integer.toHexString(ch);
                        sb.write("\\u");
                        for (int k = 0; k < 4 - ss.length(); k++) {
                            sb.write('0');
                        }
                        sb.write(ss.toUpperCase());
                    } else {
                        sb.write(ch);
                    }
            }
        }//for
    }

}
