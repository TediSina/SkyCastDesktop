import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * Rounded card-like Swing panel with a subtle shadow.
 */
public class SurfacePanel extends JPanel {
    private final Color fill;
    private final Color border;

    /**
     * Creates a surface using the default app fill and border colors.
     */
    public SurfacePanel() {
        this(AppTheme.SURFACE, new Color(197, 218, 226));
    }

    /**
     * Creates a surface with custom fill and border colors.
     *
     * @param fill card fill color
     * @param border card border color
     */
    public SurfacePanel(Color fill, Color border) {
        this.fill = fill;
        this.border = border;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        try {
            int width = getWidth() - 1;
            int height = getHeight() - 4;
            int radius = 8;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Paint the shadow first, then the card body, so child components remain crisp.
            g2.setColor(new Color(25, 72, 83, 24));
            g2.fillRoundRect(3, 4, width - 4, height - 1, radius, radius);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, width - 3, height - 3, radius, radius);
            g2.setColor(border);
            g2.drawRoundRect(0, 0, width - 3, height - 3, radius, radius);
        } finally {
            g2.dispose();
        }
        super.paintComponent(graphics);
    }
}
