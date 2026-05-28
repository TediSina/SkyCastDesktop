import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;

public final class AppTheme {
    public static final Color BACKGROUND = new Color(238, 246, 248);
    public static final Color SURFACE = Color.WHITE;
    public static final Color TEXT = new Color(25, 38, 47);
    public static final Color MUTED = new Color(93, 112, 124);
    public static final Color PRIMARY = new Color(0, 132, 141);
    public static final Color PRIMARY_DARK = new Color(7, 79, 91);
    public static final Color OCEAN = new Color(22, 117, 170);
    public static final Color ACCENT = new Color(238, 139, 62);
    public static final Color SUN = new Color(255, 190, 82);
    public static final Color MINT = new Color(88, 190, 166);
    public static final Color BORDER = new Color(206, 222, 228);
    public static final Color FIELD = new Color(249, 253, 254);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BODY_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font DISPLAY = new Font("Bahnschrift", Font.BOLD, 34);
    public static final Font TITLE = new Font("Bahnschrift", Font.BOLD, 30);
    public static final Font SECTION = new Font("Bahnschrift", Font.BOLD, 18);
    public static final Font SMALL_CAP = new Font("Bahnschrift", Font.BOLD, 12);

    private AppTheme() {
    }

    public static void install() {
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("Label.font", BODY);
        UIManager.put("Button.font", BODY_BOLD);
        UIManager.put("TextField.font", BODY);
        UIManager.put("PasswordField.font", BODY);
        UIManager.put("Table.font", BODY);
        UIManager.put("Table.rowHeight", 40);
        UIManager.put("TableHeader.font", BODY_BOLD);
        UIManager.put("OptionPane.messageFont", BODY);
        UIManager.put("OptionPane.buttonFont", BODY_BOLD);
    }

    public static Border cardBorder() {
        return BorderFactory.createEmptyBorder(22, 24, 22, 24);
    }

    public static Border compactCardBorder() {
        return BorderFactory.createEmptyBorder(18, 20, 18, 20);
    }

    public static Border inputBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(192, 214, 222)),
                BorderFactory.createEmptyBorder(10, 13, 10, 13)
        );
    }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(TITLE);
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel display(String text) {
        JLabel label = new JLabel(text);
        label.setFont(DISPLAY);
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

    public static JLabel eyebrow(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SMALL_CAP);
        label.setForeground(PRIMARY);
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
        return new GradientButton(text, PRIMARY, OCEAN, Color.WHITE, false);
    }

    public static JButton secondaryButton(String text) {
        return new GradientButton(text, Color.WHITE, new Color(240, 248, 249), PRIMARY_DARK, true);
    }

    public static JButton warmButton(String text) {
        return new GradientButton(text, ACCENT, SUN, Color.WHITE, false);
    }

    public static void styleTable(JTable table) {
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowHeight(42);
        table.setSelectionBackground(new Color(213, 241, 239));
        table.setSelectionForeground(TEXT);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(new Color(232, 244, 247));
        table.getTableHeader().setForeground(PRIMARY_DARK);
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        table.setDefaultRenderer(Object.class, new WeatherTableCellRenderer());
    }

    private static void styleInput(JComponent field) {
        field.setBackground(FIELD);
        field.setForeground(TEXT);
        field.setBorder(inputBorder());
        field.setPreferredSize(new Dimension(282, 44));
    }
}
