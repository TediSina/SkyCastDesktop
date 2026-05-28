import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SimpleJsonParser {
    private final String json;
    private int index;

    private SimpleJsonParser(String json) {
        this.json = json;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        Object value = new SimpleJsonParser(json).parse();
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new IllegalArgumentException("JSON root is not an object.");
    }

    private Object parse() {
        skipWhitespace();
        Object value = readValue();
        skipWhitespace();
        if (index != json.length()) {
            throw error("Unexpected trailing JSON content");
        }
        return value;
    }

    private Object readValue() {
        skipWhitespace();
        if (index >= json.length()) {
            throw error("Unexpected end of JSON");
        }

        char current = json.charAt(index);
        return switch (current) {
            case '{' -> readObject();
            case '[' -> readArray();
            case '"' -> readString();
            case 't' -> readLiteral("true", Boolean.TRUE);
            case 'f' -> readLiteral("false", Boolean.FALSE);
            case 'n' -> readLiteral("null", null);
            default -> {
                if (current == '-' || Character.isDigit(current)) {
                    yield readNumber();
                }
                throw error("Unexpected JSON token");
            }
        };
    }

    private Map<String, Object> readObject() {
        expect('{');
        Map<String, Object> object = new LinkedHashMap<>();
        skipWhitespace();
        if (peek('}')) {
            expect('}');
            return object;
        }

        while (true) {
            String key = readString();
            skipWhitespace();
            expect(':');
            Object value = readValue();
            object.put(key, value);
            skipWhitespace();
            if (peek('}')) {
                expect('}');
                return object;
            }
            expect(',');
            skipWhitespace();
        }
    }

    private List<Object> readArray() {
        expect('[');
        List<Object> values = new ArrayList<>();
        skipWhitespace();
        if (peek(']')) {
            expect(']');
            return values;
        }

        while (true) {
            values.add(readValue());
            skipWhitespace();
            if (peek(']')) {
                expect(']');
                return values;
            }
            expect(',');
        }
    }

    private String readString() {
        expect('"');
        StringBuilder builder = new StringBuilder();
        while (index < json.length()) {
            char current = json.charAt(index++);
            if (current == '"') {
                return builder.toString();
            }
            if (current != '\\') {
                builder.append(current);
                continue;
            }
            if (index >= json.length()) {
                throw error("Unfinished string escape");
            }
            char escaped = json.charAt(index++);
            switch (escaped) {
                case '"' -> builder.append('"');
                case '\\' -> builder.append('\\');
                case '/' -> builder.append('/');
                case 'b' -> builder.append('\b');
                case 'f' -> builder.append('\f');
                case 'n' -> builder.append('\n');
                case 'r' -> builder.append('\r');
                case 't' -> builder.append('\t');
                case 'u' -> builder.append(readUnicode());
                default -> throw error("Unsupported string escape");
            }
        }
        throw error("Unterminated JSON string");
    }

    private char readUnicode() {
        if (index + 4 > json.length()) {
            throw error("Invalid unicode escape");
        }
        String hex = json.substring(index, index + 4);
        index += 4;
        return (char) Integer.parseInt(hex, 16);
    }

    private Object readNumber() {
        int start = index;
        if (peek('-')) {
            index++;
        }
        readDigits();
        if (peek('.')) {
            index++;
            readDigits();
        }
        if (peek('e') || peek('E')) {
            index++;
            if (peek('+') || peek('-')) {
                index++;
            }
            readDigits();
        }
        return Double.parseDouble(json.substring(start, index));
    }

    private void readDigits() {
        int start = index;
        while (index < json.length() && Character.isDigit(json.charAt(index))) {
            index++;
        }
        if (start == index) {
            throw error("Expected a number");
        }
    }

    private Object readLiteral(String literal, Object value) {
        if (!json.startsWith(literal, index)) {
            throw error("Invalid JSON literal");
        }
        index += literal.length();
        return value;
    }

    private void skipWhitespace() {
        while (index < json.length() && Character.isWhitespace(json.charAt(index))) {
            index++;
        }
    }

    private void expect(char expected) {
        if (index >= json.length() || json.charAt(index) != expected) {
            throw error("Expected '" + expected + "'");
        }
        index++;
    }

    private boolean peek(char expected) {
        return index < json.length() && json.charAt(index) == expected;
    }

    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException(message + " at character " + index + ".");
    }
}
