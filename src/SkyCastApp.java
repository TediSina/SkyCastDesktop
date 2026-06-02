import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Main application frame and screen router.
 *
 * <p>The frame owns the shared services and switches between login,
 * registration, and dashboard panels using {@link CardLayout}.</p>
 */
public class SkyCastApp extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel root = new JPanel(cardLayout);
    private final AuthService authService;
    private final WeatherService weatherService;
    private final LoginPanel loginPanel;
    private final RegistrationPanel registrationPanel;
    private final DashboardPanel dashboardPanel;

    /**
     * Creates the application window and all primary screens.
     *
     * @param database initialized local database access object
     */
    public SkyCastApp(Database database) {
        super("SkyCast Desktop");
        this.authService = new AuthService(database);
        this.weatherService = new WeatherService();
        this.loginPanel = new LoginPanel(this, authService);
        this.registrationPanel = new RegistrationPanel(this, authService);
        this.dashboardPanel = new DashboardPanel(this, database, weatherService);

        root.add(loginPanel, "login");
        root.add(registrationPanel, "registration");
        root.add(dashboardPanel, "dashboard");

        setContentPane(root);
        setMinimumSize(new Dimension(760, 560));
        setSize(new Dimension(1180, 820));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        showLogin();
    }

    /**
     * Exposes authentication operations to panels that need account updates.
     *
     * @return shared authentication service
     */
    public AuthService authService() {
        return authService;
    }

    /**
     * Shows the login screen and makes Login the Enter-key default action.
     */
    public void showLogin() {
        cardLayout.show(root, "login");
        getRootPane().setDefaultButton(loginPanel.defaultButton());
    }

    /**
     * Shows the registration screen and makes Register the Enter-key default action.
     */
    public void showRegistration() {
        cardLayout.show(root, "registration");
        getRootPane().setDefaultButton(registrationPanel.defaultButton());
    }

    /**
     * Shows the dashboard for an authenticated user.
     *
     * @param user logged-in user
     */
    public void showDashboard(User user) {
        dashboardPanel.setUser(user);
        cardLayout.show(root, "dashboard");
        getRootPane().setDefaultButton(dashboardPanel.defaultButton());
    }
}
