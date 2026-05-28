import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;

public final class AppTheme {
    public static final Color BACKGROUND = new Color(244, 248, 250);
    public static final Color SURFACE = Color.WHITE;
    public static final Color TEXT = new Color(33, 45, 53);
    public static final Color MUTED = new Color(102, 118, 128);
    public static final Color PRIMARY = new Color(16, 128, 132);
    public static final Color PRIMARY_DARK = new Color(9, 93, 99);
    public static final Color ACCENT = new Color(238, 139, 62);
    public static final Color BORDER = new Color(216, 226, 231);
    public static final Color FIELD = new Color(250, 253, 254);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BODY_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font TITLE = new Font("Segoe UI", Font.BOLD, 30);
    public static final Font SECTION = new Font("Segoe UI", Font.BOLD, 18);

    private AppTheme() {
    }

    public static void install() {
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("Label.font", BODY);
        UIManager.put("Button.font", BODY_BOLD);
        UIManager.put("TextField.font", BODY);
        UIManager.put("PasswordField.font", BODY);
        UIManager.put("Table.font", BODY);
        UIManager.put("Table.rowHeight", 34);
        UIManager.put("TableHeader.font", BODY_BOLD);
        UIManager.put("OptionPane.messageFont", BODY);
        UIManager.put("OptionPane.buttonFont", BODY_BOLD);
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(22, 24, 22, 24)
        );
    }

    public static Border inputBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(9, 12, 9, 12)
        );
    }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(TITLE);
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel section(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SECTION);
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setFont(BODY);
        label.setForeground(MUTED);
        return label;
    }

    public static JTextField textField() {
        JTextField field = new JTextField();
        styleInput(field);
        return field;
    }

    public static JPasswordField passwordField() {
        JPasswordField field = new JPasswordField();
        styleInput(field);
        return field;
    }

    public static JButton primaryButton(String text) {
        JButton button = button(text, PRIMARY, Color.WHITE);
        button.setRolloverEnabled(true);
        return button;
    }

    public static JButton secondaryButton(String text) {
        return button(text, SURFACE, PRIMARY);
    }

    public static JButton warmButton(String text) {
        return button(text, ACCENT, Color.WHITE);
    }

    private static JButton button(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setMargin(new Insets(10, 16, 10, 16));
        button.setPreferredSize(new Dimension(120, 42));
        return button;
    }

    private static void styleInput(JComponent field) {
        field.setBackground(FIELD);
        field.setForeground(TEXT);
        field.setBorder(inputBorder());
        field.setPreferredSize(new Dimension(260, 42));
    }
}
