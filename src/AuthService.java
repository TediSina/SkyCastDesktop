import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Handles user registration, login, password hashing, and account preferences.
 */
public class AuthService {
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final int ITERATIONS = 120_000;

    private final Database database;
    private final SecureRandom random = new SecureRandom();

    /**
     * Creates an authentication service backed by the local SQLite database.
     *
     * @param database initialized database access object
     */
    public AuthService(Database database) {
        this.database = database;
    }

    /**
     * Registers a new user and stores a salted password hash.
     *
     * @param username requested username
     * @param email email address
     * @param password raw password characters; cleared before this method returns
     * @param preferredCity initial preferred city, defaults to Tirana when blank
     * @return newly created user
     * @throws SQLException when the database write fails
     * @throws AuthException when validation fails or the account already exists
     */
    public User register(String username, String email, char[] password, String preferredCity)
            throws SQLException, AuthException {
        String cleanUsername = trim(username);
        String cleanEmail = trim(email);
        String cleanCity = trim(preferredCity);

        validateRegistration(cleanUsername, cleanEmail, password);

        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        String saltText = Base64.getEncoder().encodeToString(salt);
        String hashText = hashPassword(password, salt);

        String sql = """
                INSERT INTO users (username, email, password_hash, salt, preferred_city)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, cleanUsername);
            statement.setString(2, cleanEmail);
            statement.setString(3, hashText);
            statement.setString(4, saltText);
            statement.setString(5, cleanCity.isBlank() ? "Tirana" : cleanCity);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new User(keys.getInt(1), cleanUsername, cleanEmail, cleanCity.isBlank() ? "Tirana" : cleanCity);
                }
            }
        } catch (SQLException ex) {
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("unique")) {
                throw new AuthException("Ky përdorues ose email është regjistruar tashmë.");
            }
            throw ex;
        } finally {
            clear(password);
        }

        throw new AuthException("Regjistrimi dështoi. Provo përsëri.");
    }

    /**
     * Authenticates a user by username or email.
     *
     * @param usernameOrEmail username or email typed by the user
     * @param password raw password characters; cleared before this method returns
     * @return matching user when credentials are valid
     * @throws SQLException when the database query fails
     * @throws AuthException when credentials are blank or invalid
     */
    public User login(String usernameOrEmail, char[] password) throws SQLException, AuthException {
        String login = trim(usernameOrEmail);
        if (login.isBlank() || password.length == 0) {
            clear(password);
            throw new AuthException("Shkruaj përdoruesin/emailin dhe fjalëkalimin.");
        }

        String sql = """
                SELECT id, username, email, password_hash, salt, preferred_city
                FROM users
                WHERE lower(username) = lower(?) OR lower(email) = lower(?)
                LIMIT 1
                """;

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, login);
            statement.setString(2, login);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new AuthException("Përdoruesi/emaili ose fjalëkalimi nuk është i saktë.");
                }

                byte[] salt = Base64.getDecoder().decode(result.getString("salt"));
                String expected = result.getString("password_hash");
                String actual = hashPassword(password, salt);
                if (!expected.equals(actual)) {
                    throw new AuthException("Përdoruesi/emaili ose fjalëkalimi nuk është i saktë.");
                }

                return new User(
                        result.getInt("id"),
                        result.getString("username"),
                        result.getString("email"),
                        result.getString("preferred_city")
                );
            }
        } finally {
            clear(password);
        }
    }

    /**
     * Persists the user's preferred city and updates the in-memory user object.
     *
     * @param user user whose preference should change
     * @param city city name to store
     * @throws SQLException when the database update fails
     */
    public void updatePreferredCity(User user, String city) throws SQLException {
        String cleanCity = trim(city);
        if (cleanCity.isBlank()) {
            return;
        }

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE users SET preferred_city = ? WHERE id = ?")) {
            statement.setString(1, cleanCity);
            statement.setInt(2, user.id());
            statement.executeUpdate();
        }
        user.setPreferredCity(cleanCity);
    }

    /**
     * Validates account fields that are independent from persistence.
     */
    private void validateRegistration(String username, String email, char[] password) throws AuthException {
        if (username.length() < 3) {
            throw new AuthException("Përdoruesi duhet të ketë të paktën 3 karaktere.");
        }
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new AuthException("Shkruaj një adresë emaili të vlefshme.");
        }
        if (password.length < 6) {
            throw new AuthException("Fjalëkalimi duhet të ketë të paktën 6 karaktere.");
        }
    }

    /**
     * Hashes a password with PBKDF2 and a per-user salt.
     */
    private String hashPassword(char[] password, byte[] salt) throws AuthException {
        try {
            // PBKDF2 keeps stored credentials safer than plain hashes if the DB is copied.
            KeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, HASH_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return Base64.getEncoder().encodeToString(factory.generateSecret(spec).getEncoded());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new AuthException("Hashimi i fjalëkalimit nuk është i disponueshëm në këtë version të Java-s.");
        }
    }

    /**
     * Null-safe string trim helper for form input.
     */
    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Clears password arrays so sensitive text is not kept in memory longer than needed.
     */
    private void clear(char[] value) {
        if (value == null) {
            return;
        }
        for (int i = 0; i < value.length; i++) {
            value[i] = '\0';
        }
    }
}
