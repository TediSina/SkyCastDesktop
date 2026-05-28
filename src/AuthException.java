/**
 * Checked exception for authentication and registration validation failures.
 */
public class AuthException extends Exception {
    /**
     * Creates an authentication exception with a user-facing message.
     *
     * @param message explanation of the authentication problem
     */
    public AuthException(String message) {
        super(message);
    }
}
