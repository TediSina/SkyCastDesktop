import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Application entry point.
 */
public class Main {
    /**
     * Starts SkyCast on the Swing event dispatch thread.
     *
     * @param args command-line arguments; currently unused
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                AppTheme.install();

                Database database = new Database();
                database.initialize();

                SkyCastApp app = new SkyCastApp(database);
                app.setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        null,
                        "SkyCast nuk mund të nisej.\n\n" + ex.getMessage(),
                        "Gabim gjatë nisjes",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
