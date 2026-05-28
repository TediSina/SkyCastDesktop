public record DailyForecast(
        String date,
        int weatherCode,
        double highTemperature,
        double lowTemperature,
        double precipitation,
        double maxWindSpeed
) {
}
