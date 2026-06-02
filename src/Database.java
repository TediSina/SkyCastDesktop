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
import java.util.Locale;

/**
 * SQLite persistence layer for users and weather-search history.
 */
public class Database {
    private static final Path DATA_DIRECTORY = Path.of("data");
    private static final Path DATABASE_PATH = DATA_DIRECTORY.resolve("skycast.db");
    private static final String URL = "jdbc:sqlite:" + DATABASE_PATH;

    /**
     * Creates the data directory and all required database tables/indexes.
     *
     * @throws IOException when the local data directory cannot be created
     * @throws SQLException when schema creation fails
     */
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
                    CREATE TABLE IF NOT EXISTS saved_cities (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        city TEXT NOT NULL,
                        country TEXT,
                        admin_area TEXT,
                        latitude REAL,
                        longitude REAL,
                        timezone TEXT,
                        city_key TEXT NOT NULL,
                        saved_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                        UNIQUE (user_id, city_key)
                    )
                    """);

            statement.execute("""
                    CREATE INDEX IF NOT EXISTS idx_weather_searches_user_time
                    ON weather_searches (user_id, searched_at DESC)
                    """);

            statement.execute("""
                    CREATE INDEX IF NOT EXISTS idx_saved_cities_user_name
                    ON saved_cities (user_id, city COLLATE NOCASE)
                    """);

            statement.execute("""
                    CREATE TRIGGER IF NOT EXISTS trg_users_saved_city_after_insert
                    AFTER INSERT ON users
                    WHEN NEW.preferred_city IS NOT NULL AND trim(NEW.preferred_city) <> ''
                    BEGIN
                        INSERT OR IGNORE INTO saved_cities (user_id, city, city_key)
                        VALUES (NEW.id, trim(NEW.preferred_city), lower(trim(NEW.preferred_city)));
                    END
                    """);

            statement.execute("""
                    INSERT OR IGNORE INTO saved_cities (user_id, city, city_key)
                    SELECT id, trim(preferred_city), lower(trim(preferred_city))
                    FROM users
                    WHERE preferred_city IS NOT NULL AND trim(preferred_city) <> ''
                    """);
        }
    }

    /**
     * Opens a SQLite connection with foreign-key enforcement enabled.
     *
     * @return active database connection; caller must close it
     * @throws SQLException when the connection cannot be opened
     */
    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(URL);
        try (Statement statement = connection.createStatement()) {
            // SQLite keeps foreign keys disabled by default per connection.
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    /**
     * Stores a single weather lookup in the current user's history.
     *
     * @param user user who performed the search
     * @param weather weather response to persist
     * @throws SQLException when the insert fails
     */
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

    /**
     * Loads the latest weather searches for a user.
     *
     * @param user owner of the history rows
     * @param limit maximum number of rows to return
     * @return newest search-history items first
     * @throws SQLException when the query fails
     */
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

    /**
     * Saves a resolved city for quick loading later.
     *
     * @param user owner of the saved city
     * @param location resolved city/location
     * @throws SQLException when the save fails
     */
    public void saveCity(User user, LocationResult location) throws SQLException {
        if (location == null || location.name() == null || location.name().isBlank()) {
            return;
        }

        String city = location.name().trim();
        String country = trimToNull(location.country());
        String adminArea = trimToNull(location.adminArea());
        String timezone = trimToNull(location.timezone());
        String cityKey = savedCityKey(city, country, adminArea);

        String deleteNameOnlySql = """
                DELETE FROM saved_cities
                WHERE user_id = ?
                  AND lower(city) = lower(?)
                  AND (country IS NULL OR trim(country) = '')
                  AND (admin_area IS NULL OR trim(admin_area) = '')
                """;
        String insertSql = """
                INSERT INTO saved_cities (
                    user_id, city, country, admin_area, latitude, longitude, timezone, city_key
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(user_id, city_key) DO UPDATE SET
                    city = excluded.city,
                    country = excluded.country,
                    admin_area = excluded.admin_area,
                    latitude = excluded.latitude,
                    longitude = excluded.longitude,
                    timezone = excluded.timezone,
                    saved_at = CURRENT_TIMESTAMP
                """;

        try (Connection connection = getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(deleteNameOnlySql)) {
                    statement.setInt(1, user.id());
                    statement.setString(2, city);
                    statement.executeUpdate();
                }

                try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
                    statement.setInt(1, user.id());
                    statement.setString(2, city);
                    statement.setString(3, country);
                    statement.setString(4, adminArea);
                    statement.setDouble(5, location.latitude());
                    statement.setDouble(6, location.longitude());
                    statement.setString(7, timezone);
                    statement.setString(8, cityKey);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    /**
     * Loads all cities saved by the user.
     *
     * @param user owner of the saved cities
     * @return saved cities sorted by name
     * @throws SQLException when the query fails
     */
    public List<SavedCity> savedCities(User user) throws SQLException {
        String sql = """
                SELECT id, city, country, admin_area, latitude, longitude, timezone, saved_at
                FROM saved_cities
                WHERE user_id = ?
                ORDER BY city COLLATE NOCASE, country COLLATE NOCASE, admin_area COLLATE NOCASE
                """;
        List<SavedCity> items = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, user.id());
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    items.add(new SavedCity(
                            result.getInt("id"),
                            result.getString("city"),
                            result.getString("country"),
                            result.getString("admin_area"),
                            nullableDouble(result, "latitude"),
                            nullableDouble(result, "longitude"),
                            result.getString("timezone"),
                            result.getString("saved_at")
                    ));
                }
            }
        }

        return items;
    }

    /**
     * Verifies that the SQLite JDBC driver is present before opening connections.
     */
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

    /**
     * Builds a stable uniqueness key for a user's saved city list.
     */
    private String savedCityKey(String city, String country, String adminArea) {
        return String.join("|",
                city == null ? "" : city.trim().toLowerCase(Locale.ROOT),
                country == null ? "" : country.trim().toLowerCase(Locale.ROOT),
                adminArea == null ? "" : adminArea.trim().toLowerCase(Locale.ROOT)
        );
    }

    /**
     * Trims optional API text and stores blanks as SQL null.
     */
    private String trimToNull(String value) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }
        return value.trim();
    }

    /**
     * Reads a nullable SQLite REAL column.
     */
    private Double nullableDouble(ResultSet result, String column) throws SQLException {
        double value = result.getDouble(column);
        return result.wasNull() ? null : value;
    }
}
