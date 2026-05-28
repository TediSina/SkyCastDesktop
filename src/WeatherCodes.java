public final class WeatherCodes {
    private WeatherCodes() {
    }

    public static String describe(int code) {
        return switch (code) {
            case 0 -> "Clear sky";
            case 1 -> "Mainly clear";
            case 2 -> "Partly cloudy";
            case 3 -> "Overcast";
            case 45, 48 -> "Fog";
            case 51, 53, 55 -> "Drizzle";
            case 56, 57 -> "Freezing drizzle";
            case 61, 63, 65 -> "Rain";
            case 66, 67 -> "Freezing rain";
            case 71, 73, 75 -> "Snowfall";
            case 77 -> "Snow grains";
            case 80, 81, 82 -> "Rain showers";
            case 85, 86 -> "Snow showers";
            case 95 -> "Thunderstorm";
            case 96, 99 -> "Thunderstorm with hail";
            default -> "Unknown";
        };
    }

    public static String icon(int code) {
        return switch (code) {
            case 0 -> "\u2600\uFE0F";
            case 1 -> "\uD83C\uDF24\uFE0F";
            case 2 -> "\u26C5";
            case 3 -> "\u2601\uFE0F";
            case 45, 48 -> "\uD83C\uDF2B\uFE0F";
            case 51, 53, 55, 56, 57 -> "\uD83C\uDF26\uFE0F";
            case 61, 63, 65, 66, 67, 80, 81, 82 -> "\uD83C\uDF27\uFE0F";
            case 71, 73, 75, 77, 85, 86 -> "\u2744\uFE0F";
            case 95, 96, 99 -> "\u26C8\uFE0F";
            default -> "\uD83C\uDF21\uFE0F";
        };
    }

    public static String describeWithIcon(int code) {
        return icon(code) + " " + describe(code);
    }
}
