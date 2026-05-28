import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Client for Open-Meteo geocoding and forecast APIs.
 */
public class WeatherService {
    private static final String GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search";
    private static final String FORECAST_URL = "https://api.open-meteo.com/v1/forecast";

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Finds the best matching location for a city name.
     *
     * @param city city typed by the user
     * @return first location result returned by Open-Meteo
     * @throws IOException when the API response is invalid or no city is found
     * @throws InterruptedException when the HTTP request is interrupted
     */
    public LocationResult searchLocation(String city) throws IOException, InterruptedException {
        String query = GEOCODING_URL
                + "?name=" + encode(city)
                + "&count=1&language=sq&format=json";
        Map<String, Object> response = requestJson(query);
        List<Object> results = list(response.get("results"));
        if (results.isEmpty()) {
            throw new IOException("Nuk u gjet asnjë qytet për \"" + city + "\".");
        }

        Map<String, Object> first = object(results.get(0));
        return new LocationResult(
                text(first.get("name")),
                text(first.get("country")),
                text(first.get("admin1")),
                number(first.get("latitude")),
                number(first.get("longitude")),
                text(first.get("timezone"))
        );
    }

    /**
     * Fetches current and daily weather for a resolved location.
     *
     * @param location resolved Open-Meteo location
     * @return current weather plus up to seven daily forecast rows
     * @throws IOException when the API response cannot be loaded or parsed
     * @throws InterruptedException when the HTTP request is interrupted
     */
    public WeatherData fetchWeather(LocationResult location) throws IOException, InterruptedException {
        String query = String.format(Locale.US,
                "%s?latitude=%.6f&longitude=%.6f"
                        + "&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,"
                        + "precipitation,weather_code,wind_speed_10m,wind_direction_10m"
                        + "&daily=weather_code,temperature_2m_max,temperature_2m_min,"
                        + "precipitation_sum,wind_speed_10m_max"
                        + "&timezone=auto",
                FORECAST_URL,
                location.latitude(),
                location.longitude()
        );

        Map<String, Object> response = requestJson(query);
        Map<String, Object> current = object(response.get("current"));
        Map<String, Object> daily = object(response.get("daily"));

        return new WeatherData(
                location,
                text(current.get("time")),
                number(current.get("temperature_2m")),
                number(current.get("apparent_temperature")),
                integer(current.get("relative_humidity_2m")),
                number(current.get("precipitation")),
                number(current.get("wind_speed_10m")),
                integer(current.get("wind_direction_10m")),
                integer(current.get("weather_code")),
                readDailyForecasts(daily)
        );
    }

    /**
     * Converts Open-Meteo daily arrays into row-oriented forecast objects.
     */
    private List<DailyForecast> readDailyForecasts(Map<String, Object> daily) {
        List<Object> dates = list(daily.get("time"));
        List<Object> codes = list(daily.get("weather_code"));
        List<Object> highs = list(daily.get("temperature_2m_max"));
        List<Object> lows = list(daily.get("temperature_2m_min"));
        List<Object> rain = list(daily.get("precipitation_sum"));
        List<Object> wind = list(daily.get("wind_speed_10m_max"));

        int count = Math.min(dates.size(), 7);
        List<DailyForecast> forecasts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            forecasts.add(new DailyForecast(
                    text(dates.get(i)),
                    integer(codes.get(i)),
                    number(highs.get(i)),
                    number(lows.get(i)),
                    number(rain.get(i)),
                    number(wind.get(i))
            ));
        }
        return forecasts;
    }

    /**
     * Executes a GET request and parses the JSON response body.
     */
    private Map<String, Object> requestJson(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Shërbimi i motit ktheu kodin HTTP " + response.statusCode() + ".");
        }

        Map<String, Object> json = SimpleJsonParser.parseObject(response.body());
        if (Boolean.TRUE.equals(json.get("error"))) {
            throw new IOException(text(json.get("reason")));
        }
        return json;
    }

    /**
     * URL-encodes user-provided city names.
     */
    private String encode(String value) {
        return URLEncoder.encode(value.trim(), StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    /**
     * Casts a parsed JSON value to an object map with a helpful failure path.
     */
    private Map<String, Object> object(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new IllegalArgumentException("Formati i përgjigjes nga API nuk pritej.");
    }

    @SuppressWarnings("unchecked")
    /**
     * Casts a parsed JSON value to a list, returning an empty list for missing arrays.
     */
    private List<Object> list(Object value) {
        if (value instanceof List<?> list) {
            return (List<Object>) list;
        }
        return List.of();
    }

    /**
     * Converts optional JSON values to display-safe text.
     */
    private String text(Object value) {
        return value == null ? "" : value.toString();
    }

    /**
     * Converts Open-Meteo numeric values that may arrive as numbers or strings.
     */
    private double number(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(text(value));
    }

    /**
     * Converts a numeric JSON value to an integer weather/humidity code.
     */
    private int integer(Object value) {
        return (int) Math.round(number(value));
    }
}
