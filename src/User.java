public class User {
    private final int id;
    private final String username;
    private final String email;
    private String preferredCity;

    public User(int id, String username, String email, String preferredCity) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.preferredCity = preferredCity;
    }

    public int id() {
        return id;
    }

    public String username() {
        return username;
    }

    public String email() {
        return email;
    }

    public String preferredCity() {
        return preferredCity;
    }

    public void setPreferredCity(String preferredCity) {
        this.preferredCity = preferredCity;
    }
}
