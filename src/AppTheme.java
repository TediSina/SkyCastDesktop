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

/**
 * Centralized visual system for SkyCast.
 *
 * <p>The app does not use a third-party look-and-feel library, so this class
 * keeps colors, fonts, borders, buttons, fields, and table styling consistent
 * across all Swing screens.</p>
 */
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

    /**
     * Applies global Swing defaults before any application panels are created.
     */
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

    /**
     * Creates the default card padding used by larger content panels.
     *
     * @return an empty border with the app's standard card spacing
     */
    public static Border cardBorder() {
        return BorderFactory.createEmptyBorder(22, 24, 22, 24);
    }

    /**
     * Creates tighter padding for compact panels such as headers and search bars.
     *
     * @return an empty border with compact card spacing
     */
    public static Border compactCardBorder() {
        return BorderFactory.createEmptyBorder(18, 20, 18, 20);
    }

    /**
     * Builds the shared input border used by text and password fields.
     *
     * @return a compound border with line and inner padding
     */
    public static Border inputBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(192, 214, 222)),
                BorderFactory.createEmptyBorder(10, 13, 10, 13)
        );
    }

    /**
     * Creates a title label using the app title typography.
     *
     * @param text label text
     * @return styled label
     */
    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(TITLE);
        label.setForeground(TEXT);
        return label;
    }

    /**
     * Creates a large display label used for brand and page headings.
     *
     * @param text label text
     * @return styled label
     */
    public static JLabel display(String text) {
        JLabel label = new JLabel(text);
        label.setFont(DISPLAY);
        label.setForeground(TEXT);
        return label;
    }

    /**
     * Creates a section heading label.
     *
     * @param text label text
     * @return styled label
     */
    public static JLabel section(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SECTION);
        label.setForeground(TEXT);
        return label;
    }

    /**
     * Creates secondary body text.
     *
     * @param text label text
     * @return styled label
     */
    public static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setFont(BODY);
        label.setForeground(MUTED);
        return label;
    }

    /**
     * Creates a small uppercase label used for metadata and status chips.
     *
     * @param text label text
     * @return styled label
     */
    public static JLabel eyebrow(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SMALL_CAP);
        label.setForeground(PRIMARY);
        return label;
    }

    /**
     * Creates a styled text field.
     *
     * @return text field with SkyCast input styling
     */
    public static JTextField textField() {
        JTextField field = new JTextField();
        styleInput(field);
        return field;
    }

    /**
     * Creates a styled password field.
     *
     * @return password field with SkyCast input styling
     */
    public static JPasswordField passwordField() {
        JPasswordField field = new JPasswordField();
        styleInput(field);
        return field;
    }

    /**
     * Creates the primary action button.
     *
     * @param text button text
     * @return gradient primary button
     */
    public static JButton primaryButton(String text) {
        return new GradientButton(text, PRIMARY, OCEAN, Color.WHITE, false);
    }

    /**
     * Creates a secondary action button.
     *
     * @param text button text
     * @return outlined secondary button
     */
    public static JButton secondaryButton(String text) {
        return new GradientButton(text, Color.WHITE, new Color(240, 248, 249), PRIMARY_DARK, true);
    }

    /**
     * Creates an accent button for save/commit style actions.
     *
     * @param text button text
     * @return warm gradient button
     */
    public static JButton warmButton(String text) {
        return new GradientButton(text, ACCENT, SUN, Color.WHITE, false);
    }

    /**
     * Applies the shared table style and row renderer.
     *
     * @param table table to style
     */
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

    /**
     * Applies shared input styling to both text and password fields.
     */
    private static void styleInput(JComponent field) {
        field.setBackground(FIELD);
        field.setForeground(TEXT);
        field.setBorder(inputBorder());
        field.setPreferredSize(new Dimension(282, 44));
    }
}
