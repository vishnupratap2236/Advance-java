package com.shopsphere.util;

/**
 * Minimal JSON writer for the small REST endpoints of the application.
 *
 * <p>Values are escaped so that they can never break out of the JSON document.
 */
public final class Json {

    private Json() {
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    public static String object(String... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("Expected alternating key/value pairs");
        }
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('"').append(escape(keyValuePairs[i])).append("\":");
            String value = keyValuePairs[i + 1];
            if (isJsonLiteral(value)) {
                sb.append(value);
            } else {
                sb.append('"').append(escape(value)).append('"');
            }
        }
        return sb.append('}').toString();
    }

    public static String array(java.util.List<String> jsonValues) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < jsonValues.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(jsonValues.get(i));
        }
        return sb.append(']').toString();
    }

    private static boolean isJsonLiteral(String value) {
        return "null".equals(value) || "true".equals(value) || "false".equals(value)
                || (value.startsWith("{") && value.endsWith("}"))
                || (value.startsWith("[") && value.endsWith("]"));
    }
}
