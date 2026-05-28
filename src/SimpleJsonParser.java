import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal JSON parser used for Open-Meteo responses.
 *
 * <p>The project intentionally avoids an additional JSON dependency, so this
 * parser supports the JSON constructs needed by the API: objects, arrays,
 * strings, numbers, booleans, and null.</p>
 */
public class SimpleJsonParser {
    private final String json;
    private int index;

    private SimpleJsonParser(String json) {
        this.json = json;
    }

    /**
     * Parses a JSON string whose root value must be an object.
     *
     * @param json JSON text
     * @return root object as a map
     * @throws IllegalArgumentException when the JSON is invalid or not an object
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        Object value = new SimpleJsonParser(json).parse();
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new IllegalArgumentException("Rrënja e JSON-it nuk është objekt.");
    }

    /**
     * Parses the full input and verifies that no trailing tokens remain.
     */
    private Object parse() {
        skipWhitespace();
        Object value = readValue();
        skipWhitespace();
        if (index != json.length()) {
            throw error("Përmbajtje e papritur në fund të JSON-it");
        }
        return value;
    }

    /**
     * Dispatches to the correct JSON reader based on the next token.
     */
    private Object readValue() {
        skipWhitespace();
        if (index >= json.length()) {
            throw error("JSON-i përfundoi papritur");
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
                throw error("Element i papritur në JSON");
            }
        };
    }

    /**
     * Reads a JSON object into insertion-order map form.
     */
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

    /**
     * Reads a JSON array into a Java list.
     */
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

    /**
     * Reads a JSON string, including standard escape sequences.
     */
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
                throw error("Sekuencë ikjeje e papërfunduar në tekst");
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
                default -> throw error("Sekuencë ikjeje e pambështetur në tekst");
            }
        }
        throw error("Tekst JSON i pambyllur");
    }

    /**
     * Reads a four-hex-character unicode escape.
     */
    private char readUnicode() {
        if (index + 4 > json.length()) {
            throw error("Sekuencë unicode e pavlefshme");
        }
        String hex = json.substring(index, index + 4);
        index += 4;
        return (char) Integer.parseInt(hex, 16);
    }

    /**
     * Reads a JSON number and returns it as a {@link Double}.
     */
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

    /**
     * Advances over a required run of digits.
     */
    private void readDigits() {
        int start = index;
        while (index < json.length() && Character.isDigit(json.charAt(index))) {
            index++;
        }
        if (start == index) {
            throw error("Pritej një numër");
        }
    }

    /**
     * Reads a fixed JSON literal such as {@code true}, {@code false}, or {@code null}.
     */
    private Object readLiteral(String literal, Object value) {
        if (!json.startsWith(literal, index)) {
            throw error("Vlerë JSON e pavlefshme");
        }
        index += literal.length();
        return value;
    }

    /**
     * Skips JSON whitespace.
     */
    private void skipWhitespace() {
        while (index < json.length() && Character.isWhitespace(json.charAt(index))) {
            index++;
        }
    }

    /**
     * Consumes an expected character or throws a location-aware parser error.
     */
    private void expect(char expected) {
        if (index >= json.length() || json.charAt(index) != expected) {
            throw error("Pritej '" + expected + "'");
        }
        index++;
    }

    /**
     * Checks the next character without advancing.
     */
    private boolean peek(char expected) {
        return index < json.length() && json.charAt(index) == expected;
    }

    /**
     * Builds parser exceptions with the current character offset.
     */
    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException(message + " në karakterin " + index + ".");
    }
}
