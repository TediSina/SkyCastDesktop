import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

/**
 * Fixed-size emoji weather icon renderer.
 *
 * <p>A normal {@link javax.swing.JLabel} can clip color emoji glyphs on some
 * Windows font/rendering combinations. This component reserves extra vertical
 * space and draws the emoji with a manually centered baseline.</p>
 */
public class WeatherIconView extends JComponent {
    private static final Dimension SIZE = new Dimension(112, 104);
    private String icon = "\uD83C\uDF21\uFE0F";

    /**
     * Creates the icon view with a Windows emoji-friendly font.
     */
    public WeatherIconView() {
        setFont(new Font("Segoe UI Emoji", Font.PLAIN, 58));
        setForeground(new Color(33, 45, 53));
        setPreferredSize(SIZE);
        setMinimumSize(SIZE);
        setOpaque(false);
    }

    /**
     * Sets the emoji displayed by this view.
     *
     * @param icon emoji text; blank values fall back to the thermometer icon
     */
    public void setIcon(String icon) {
        this.icon = icon == null || icon.isBlank() ? "\uD83C\uDF21\uFE0F" : icon;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(SIZE);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(SIZE);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g2 = (Graphics2D) graphics.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(getFont());
            g2.setColor(getForeground());

            // Center by baseline instead of JLabel layout metrics to avoid top clipping.
            FontMetrics metrics = g2.getFontMetrics();
            int x = (getWidth() - metrics.stringWidth(icon)) / 2;
            int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent() + 3;
            g2.drawString(icon, Math.max(0, x), y);
        } finally {
            g2.dispose();
        }
    }
}
