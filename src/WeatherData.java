import java.util.List;

public record WeatherData(
        LocationResult location,
        String observedAt,
        double temperature,
        double apparentTemperature,
        int humidity,
        double precipitation,
        double windSpeed,
        int windDirection,
        int weatherCode,
        List<DailyForecast> dailyForecasts
) {
}
