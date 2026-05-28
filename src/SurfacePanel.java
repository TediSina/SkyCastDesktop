import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class SurfacePanel extends JPanel {
    private final Color fill;
    private final Color border;

    public SurfacePanel() {
        this(AppTheme.SURFACE, new Color(197, 218, 226));
    }

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
