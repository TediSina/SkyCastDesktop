import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Registration form for creating a new local SkyCast account.
 */
public class RegistrationPanel extends SkyBackgroundPanel {
    private final SkyCastApp app;
    private final AuthService authService;
    private final JTextField usernameField = AppTheme.textField();
    private final JTextField emailField = AppTheme.textField();
    private final JTextField cityField = AppTheme.textField();
    private final JPasswordField passwordField = AppTheme.passwordField();
    private final JPasswordField confirmPasswordField = AppTheme.passwordField();
    private final JButton createButton = AppTheme.primaryButton("Regjistrohu");
    private final JButton loginButton = AppTheme.secondaryButton("Kthehu te hyrja");

    /**
     * Creates the registration form and wires Enter-key registration behavior.
     *
     * @param app parent frame used for navigation
     * @param authService authentication service
     */
    public RegistrationPanel(SkyCastApp app, AuthService authService) {
        this.app = app;
        this.authService = authService;

        cityField.setText("Tirana");
        setLayout(new GridBagLayout());
        setBorder(AppTheme.cardBorder());
        add(createShell());

        usernameField.addActionListener(event -> register());
        emailField.addActionListener(event -> register());
        passwordField.addActionListener(event -> register());
        confirmPasswordField.addActionListener(event -> register());
        cityField.addActionListener(event -> register());
    }

    /**
     * Returns the button used as this screen's default Enter action.
     *
     * @return register button
     */
    public JButton defaultButton() {
        return createButton;
    }

    /**
     * Builds the two-column registration layout.
     */
    private JPanel createShell() {
        JPanel shell = new JPanel(new BorderLayout(0, 0));
        shell.setOpaque(false);
        shell.setPreferredSize(new Dimension(980, 560));
        shell.add(new AuthShowcasePanel("Qyteti yt,\nme një vështrim"), BorderLayout.WEST);
        shell.add(createCard(), BorderLayout.CENTER);
        return shell;
    }

    /**
     * Builds the registration form card.
     */
    private JPanel createCard() {
        SurfacePanel card = new SurfacePanel();
        card.setLayout(new BorderLayout(0, 22));
        card.setBorder(AppTheme.cardBorder());

        JPanel heading = new JPanel(new GridBagLayout());
        heading.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        heading.add(AppTheme.eyebrow("PROFIL I RI"), constraints);
        constraints.gridy = 1;
        constraints.insets = new Insets(10, 0, 0, 0);
        heading.add(AppTheme.display("Krijo llogari"), constraints);
        constraints.gridy = 2;
        constraints.insets = new Insets(8, 0, 0, 0);
        heading.add(AppTheme.muted("Ruaj qytetin e preferuar dhe kërkimet e fundit lokalisht."), constraints);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        addField(fields, 0, 0, "Përdoruesi", usernameField);
        addField(fields, 1, 0, "Emaili", emailField);
        addField(fields, 0, 1, "Fjalëkalimi", passwordField);
        addField(fields, 1, 1, "Konfirmo fjalëkalimin", confirmPasswordField);
        addField(fields, 0, 2, "Qyteti i preferuar", cityField);

        createButton.addActionListener(event -> register());
        loginButton.addActionListener(event -> app.showLogin());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(loginButton);
        actions.add(createButton);

        card.add(heading, BorderLayout.NORTH);
        card.add(fields, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Adds a labeled field to the two-column form grid.
     */
    private void addField(JPanel parent, int x, int y, String label, JTextField field) {
        JPanel group = new JPanel(new BorderLayout(0, 6));
        group.setOpaque(false);
        JLabel text = new JLabel(label);
        text.setFont(AppTheme.BODY_BOLD);
        text.setForeground(AppTheme.TEXT);
        group.add(text, BorderLayout.NORTH);
        group.add(field, BorderLayout.CENTER);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.insets = new Insets(y == 0 ? 0 : 15, x == 0 ? 0 : 16, 0, 0);
        parent.add(group, constraints);
    }

    /**
     * Validates the form, creates the user, and opens the dashboard on success.
     */
    private void register() {
        char[] password = passwordField.getPassword();
        char[] confirm = confirmPasswordField.getPassword();
        if (!String.valueOf(password).equals(String.valueOf(confirm))) {
            clear(password);
            clear(confirm);
            JOptionPane.showMessageDialog(this, "Fjalëkalimet nuk përputhen.", "Regjistrimi dështoi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            User user = authService.register(
                    usernameField.getText(),
                    emailField.getText(),
                    password,
                    cityField.getText()
            );
            clear(confirm);
            passwordField.setText("");
            confirmPasswordField.setText("");
            app.showDashboard(user);
        } catch (Exception ex) {
            clear(confirm);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Regjistrimi dështoi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Clears temporary password confirmation data from memory.
     */
    private void clear(char[] value) {
        for (int i = 0; i < value.length; i++) {
            value[i] = '\0';
        }
    }
}
