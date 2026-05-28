import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final Path DATA_DIRECTORY = Path.of("data");
    private static final Path DATABASE_PATH = DATA_DIRECTORY.resolve("skycast.db");
    private static final String URL = "jdbc:sqlite:" + DATABASE_PATH;

    public void initialize() throws IOException, SQLException {
        loadDriver();
        Files.createDirectories(DATA_DIRECTORY);

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL UNIQUE,
                        email TEXT NOT NULL UNIQUE,
                        password_hash TEXT NOT NULL,
                        salt TEXT NOT NULL,
                        preferred_city TEXT NOT NULL DEFAULT 'Tirana',
                        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS weather_searches (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        city TEXT NOT NULL,
                        country TEXT,
                        latitude REAL NOT NULL,
                        longitude REAL NOT NULL,
                        temperature REAL NOT NULL,
                        apparent_temperature REAL NOT NULL,
                        humidity INTEGER NOT NULL,
                        precipitation REAL NOT NULL,
                        wind_speed REAL NOT NULL,
                        weather_code INTEGER NOT NULL,
                        observed_at TEXT NOT NULL,
                        searched_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                    )
                    """);

            statement.execute("""
                    CREATE INDEX IF NOT EXISTS idx_weather_searches_user_time
                    ON weather_searches (user_id, searched_at DESC)
                    """);
        }
    }

    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(URL);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    public void saveWeatherSearch(User user, WeatherData weather) throws SQLException {
        String sql = """
                INSERT INTO weather_searches (
                    user_id, city, country, latitude, longitude, temperature,
                    apparent_temperature, humidity, precipitation, wind_speed,
                    weather_code, observed_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, user.id());
            statement.setString(2, weather.location().name());
            statement.setString(3, weather.location().country());
            statement.setDouble(4, weather.location().latitude());
            statement.setDouble(5, weather.location().longitude());
            statement.setDouble(6, weather.temperature());
            statement.setDouble(7, weather.apparentTemperature());
            statement.setInt(8, weather.humidity());
            statement.setDouble(9, weather.precipitation());
            statement.setDouble(10, weather.windSpeed());
            statement.setInt(11, weather.weatherCode());
            statement.setString(12, weather.observedAt());
            statement.executeUpdate();
        }
    }

    public List<SearchHistoryItem> recentSearches(User user, int limit) throws SQLException {
        String sql = """
                SELECT city, country, temperature, weather_code, searched_at
                FROM weather_searches
                WHERE user_id = ?
                ORDER BY searched_at DESC
                LIMIT ?
                """;
        List<SearchHistoryItem> items = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, user.id());
            statement.setInt(2, limit);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    items.add(new SearchHistoryItem(
                            result.getString("city"),
                            result.getString("country"),
                            result.getDouble("temperature"),
                            result.getInt("weather_code"),
                            result.getString("searched_at")
                    ));
                }
            }
        }

        return items;
    }

    private void loadDriver() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(
                    "Biblioteka SQLite JDBC mungon. Mbaje lib/sqlite-jdbc-3.53.1.0.jar në classpath.",
                    ex
            );
        }
    }
}
