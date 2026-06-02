import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * Login form for existing SkyCast users.
 */
public class LoginPanel extends SkyBackgroundPanel {
    private final SkyCastApp app;
    private final AuthService authService;
    private final JTextField loginField = AppTheme.textField();
    private final JPasswordField passwordField = AppTheme.passwordField();
    private final JButton loginButton = AppTheme.primaryButton("Hyr");
    private final JButton registerButton = AppTheme.secondaryButton("Krijo llogari");

    /**
     * Creates the login form and wires Enter-key login behavior.
     *
     * @param app parent frame used for navigation
     * @param authService authentication service
     */
    public LoginPanel(SkyCastApp app, AuthService authService) {
        this.app = app;
        this.authService = authService;

        setLayout(new BorderLayout());
        setBorder(AppTheme.cardBorder());
        add(createScrollableShell(), BorderLayout.CENTER);

        loginField.addActionListener(event -> login());
        passwordField.addActionListener(event -> login());
    }

    /**
     * Returns the button used as this screen's default Enter action.
     *
     * @return login button
     */
    public JButton defaultButton() {
        return loginButton;
    }

    /**
     * Centers the auth card while allowing small windows to scroll.
     */
    private JScrollPane createScrollableShell() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.add(createShell());

        JScrollPane scrollPane = new JScrollPane(center);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        return scrollPane;
    }

    /**
     * Builds the two-column login layout.
     */
    private JPanel createShell() {
        JPanel shell = new JPanel(new BorderLayout(0, 0));
        shell.setOpaque(false);
        shell.setPreferredSize(new Dimension(860, 520));
        shell.setMinimumSize(new Dimension(0, 0));
        shell.add(new AuthShowcasePanel("Parashikime\nme qartësi"), BorderLayout.WEST);
        shell.add(createCard(), BorderLayout.CENTER);
        return shell;
    }

    /**
     * Builds the form card that contains labels, fields, and actions.
     */
    private JPanel createCard() {
        SurfacePanel card = new SurfacePanel();
        card.setLayout(new BorderLayout(0, 24));
        card.setBorder(AppTheme.cardBorder());

        JPanel heading = new JPanel(new GridBagLayout());
        heading.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        heading.add(AppTheme.eyebrow("LLOGARI LOKALE"), constraints);
        constraints.gridy = 1;
        constraints.insets = new Insets(10, 0, 0, 0);
        heading.add(AppTheme.display("Mirë se u riktheve"), constraints);
        constraints.gridy = 2;
        constraints.insets = new Insets(8, 0, 0, 0);
        heading.add(AppTheme.muted("Shiko qytetin e ruajtur dhe historikun e fundit të motit."), constraints);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        addField(fields, 0, "Përdoruesi ose emaili", loginField);
        addField(fields, 1, "Fjalëkalimi", passwordField);

        loginButton.addActionListener(event -> login());
        registerButton.addActionListener(event -> app.showRegistration());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(registerButton);
        actions.add(loginButton);

        card.add(heading, BorderLayout.NORTH);
        card.add(fields, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Adds a labeled input field to the login form.
     */
    private void addField(JPanel parent, int row, String label, JTextField field) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row * 2;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(row == 0 ? 0 : 16, 0, 6, 0);
        JLabel text = new JLabel(label);
        text.setFont(AppTheme.BODY_BOLD);
        text.setForeground(AppTheme.TEXT);
        parent.add(text, constraints);

        constraints.gridy = row * 2 + 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.insets = new Insets(0, 0, 0, 0);
        parent.add(field, constraints);
    }

    /**
     * Validates credentials and navigates to the dashboard on success.
     */
    private void login() {
        try {
            User user = authService.login(loginField.getText(), passwordField.getPassword());
            passwordField.setText("");
            app.showDashboard(user);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Hyrja dështoi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
