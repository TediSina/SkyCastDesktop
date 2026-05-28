import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class SkyCastApp extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel root = new JPanel(cardLayout);
    private final AuthService authService;
    private final WeatherService weatherService;
    private final LoginPanel loginPanel;
    private final RegistrationPanel registrationPanel;
    private final DashboardPanel dashboardPanel;

    public SkyCastApp(Database database) {
        super("SkyCast për desktop");
        this.authService = new AuthService(database);
        this.weatherService = new WeatherService();
        this.loginPanel = new LoginPanel(this, authService);
        this.registrationPanel = new RegistrationPanel(this, authService);
        this.dashboardPanel = new DashboardPanel(this, database, weatherService);

        root.add(loginPanel, "login");
        root.add(registrationPanel, "registration");
        root.add(dashboardPanel, "dashboard");

        setContentPane(root);
        setMinimumSize(new Dimension(1180, 1000));
        setSize(new Dimension(1320, 1000));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        showLogin();
    }

    public AuthService authService() {
        return authService;
    }

    public void showLogin() {
        cardLayout.show(root, "login");
        getRootPane().setDefaultButton(loginPanel.defaultButton());
    }

    public void showRegistration() {
        cardLayout.show(root, "registration");
        getRootPane().setDefaultButton(registrationPanel.defaultButton());
    }

    public void showDashboard(User user) {
        dashboardPanel.setUser(user);
        cardLayout.show(root, "dashboard");
        getRootPane().setDefaultButton(dashboardPanel.defaultButton());
    }
}
