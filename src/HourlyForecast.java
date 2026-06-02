/**
 * One hourly forecast point used by the 12-hour dashboard graph.
 *
 * @param time ISO local time from Open-Meteo
 * @param temperature air temperature in Celsius
 * @param apparentTemperature perceived temperature in Celsius
 * @param precipitation precipitation amount in millimeters
 * @param windSpeed wind speed in km/h
 * @param weatherCode Open-Meteo weather condition code
 */
public record HourlyForecast(
        String time,
        double temperature,
        double apparentTemperature,
        double precipitation,
        double windSpeed,
        int weatherCode
) {
}
