import java.util.List;

/**
 * Full weather response used by the dashboard and history persistence.
 *
 * @param location resolved location
 * @param observedAt timestamp of current weather observation
 * @param temperature current air temperature in Celsius
 * @param apparentTemperature perceived temperature in Celsius
 * @param humidity relative humidity percentage
 * @param precipitation current precipitation in millimeters
 * @param windSpeed current wind speed in km/h
 * @param windDirection current wind direction in degrees
 * @param weatherCode Open-Meteo weather condition code
 * @param dailyForecasts daily forecast rows, usually seven days
 */
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
