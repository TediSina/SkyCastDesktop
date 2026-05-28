import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * Full-screen background panel used by the main app screens.
 *
 * <p>It paints a soft sky gradient and subtle geometric texture so the app has
 * a weather-themed visual identity while still using plain Swing.</p>
 */
public class SkyBackgroundPanel extends JPanel {
    /**
     * Creates a transparent panel that paints its own background.
     */
    public SkyBackgroundPanel() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        try {
            int width = getWidth();
            int height = getHeight();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, new Color(222, 245, 248), width, height, new Color(250, 244, 231)));
            g2.fillRect(0, 0, width, height);

            // Diagonal strokes and bands add motion without using image assets.
            g2.setColor(new Color(255, 255, 255, 84));
            for (int x = -height; x < width + height; x += 56) {
                g2.drawLine(x, height, x + height, 0);
            }

            g2.setColor(new Color(0, 132, 141, 26));
            for (int y = 44; y < height; y += 92) {
                g2.fillRect(0, y, width, 2);
            }

            g2.setColor(new Color(238, 139, 62, 34));
            int[] xs = {0, width, width, 0};
            int[] ys = {height - 120, height - 270, height, height};
            g2.fillPolygon(xs, ys, xs.length);
        } finally {
            g2.dispose();
        }
        super.paintComponent(graphics);
    }
}
