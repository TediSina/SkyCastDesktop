/**
 * Mutable session model for an authenticated user.
 */
public class User {
    private final int id;
    private final String username;
    private final String email;
    private String preferredCity;

    /**
     * Creates a user loaded from the database or just created by registration.
     *
     * @param id database identifier
     * @param username account username
     * @param email account email
     * @param preferredCity saved city preference
     */
    public User(int id, String username, String email, String preferredCity) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.preferredCity = preferredCity;
    }

    /**
     * @return database identifier
     */
    public int id() {
        return id;
    }

    /**
     * @return account username
     */
    public String username() {
        return username;
    }

    /**
     * @return account email
     */
    public String email() {
        return email;
    }

    /**
     * @return saved preferred city
     */
    public String preferredCity() {
        return preferredCity;
    }

    /**
     * Updates the in-memory preferred city after it is persisted.
     *
     * @param preferredCity new preferred city
     */
    public void setPreferredCity(String preferredCity) {
        this.preferredCity = preferredCity;
    }
}
