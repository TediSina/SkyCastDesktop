import java.awt.BorderLayout;
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
import javax.swing.JTextField;

public class LoginPanel extends JPanel {
    private final SkyCastApp app;
    private final AuthService authService;
    private final JTextField loginField = AppTheme.textField();
    private final JPasswordField passwordField = AppTheme.passwordField();

    public LoginPanel(SkyCastApp app, AuthService authService) {
        this.app = app;
        this.authService = authService;

        setLayout(new GridBagLayout());
        setBackground(AppTheme.BACKGROUND);
        add(createCard());
    }

    private JPanel createCard() {
        JPanel card = new JPanel(new BorderLayout(0, 22));
        card.setBackground(AppTheme.SURFACE);
        card.setBorder(AppTheme.cardBorder());

        JPanel heading = new JPanel(new GridBagLayout());
        heading.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        heading.add(AppTheme.title("Welcome Back"), constraints);
        constraints.gridy = 1;
        constraints.insets = new Insets(8, 0, 0, 0);
        heading.add(AppTheme.muted("Sign in to check your saved city and recent forecasts."), constraints);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        addField(fields, 0, "Username or email", loginField);
        addField(fields, 1, "Password", passwordField);

        JButton loginButton = AppTheme.primaryButton("Login");
        JButton registerButton = AppTheme.secondaryButton("Create Account");

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

    private void addField(JPanel parent, int row, String label, JTextField field) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row * 2;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(row == 0 ? 0 : 14, 0, 5, 0);
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

    private void login() {
        try {
            User user = authService.login(loginField.getText(), passwordField.getPassword());
            passwordField.setText("");
            app.showDashboard(user);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
