/**
 * One row in the daily forecast table.
 *
 * @param date ISO date from Open-Meteo
 * @param weatherCode Open-Meteo daily weather condition code
 * @param highTemperature maximum temperature in Celsius
 * @param lowTemperature minimum temperature in Celsius
 * @param precipitation daily precipitation sum in millimeters
 * @param maxWindSpeed maximum wind speed in km/h
 */
public record DailyForecast(
        String date,
        int weatherCode,
        double highTemperature,
        double lowTemperature,
        double precipitation,
        double maxWindSpeed
) {
}
