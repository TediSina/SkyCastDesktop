import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.JButton;

/**
 * Custom Swing button that paints either a gradient-filled or outlined action.
 *
 * <p>This keeps the UI visually richer without requiring an external Swing
 * look-and-feel dependency.</p>
 */
public class GradientButton extends JButton {
    private final Color start;
    private final Color end;
    private final Color textColor;
    private final boolean outlined;

    /**
     * Creates a gradient or outlined button.
     *
     * @param text button text
     * @param start first gradient color, or outlined fill color
     * @param end second gradient color
     * @param textColor foreground color while enabled
     * @param outlined true to draw a light outlined button instead of a gradient
     */
    public GradientButton(String text, Color start, Color end, Color textColor, boolean outlined) {
        super(text);
        this.start = start;
        this.end = end;
        this.textColor = textColor;
        this.outlined = outlined;

        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setForeground(textColor);
        setMargin(new Insets(10, 17, 10, 17));
        setPreferredSize(new Dimension(128, 43));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth() - 1;
            int height = getHeight() - 1;
            int radius = 8;

            // Rollover brightening is painted manually because contentAreaFilled is disabled.
            Color paintStart = getModel().isRollover() ? brighten(start, 1.06f) : start;
            Color paintEnd = getModel().isRollover() ? brighten(end, 1.06f) : end;

            if (outlined) {
                g2.setColor(new Color(255, 255, 255, isEnabled() ? 232 : 150));
                g2.fillRoundRect(0, 0, width, height, radius, radius);
                g2.setColor(new Color(178, 211, 220));
                g2.drawRoundRect(0, 0, width, height, radius, radius);
            } else {
                g2.setPaint(new GradientPaint(0, 0, paintStart, width, height, paintEnd));
                g2.fillRoundRect(0, 0, width, height, radius, radius);
            }
        } finally {
            g2.dispose();
        }
        setForeground(isEnabled() ? textColor : new Color(135, 151, 158));
        super.paintComponent(graphics);
    }

    /**
     * Scales a color upward while clamping each channel to the valid RGB range.
     */
    private Color brighten(Color color, float amount) {
        return new Color(
                Math.min(255, Math.round(color.getRed() * amount)),
                Math.min(255, Math.round(color.getGreen() * amount)),
                Math.min(255, Math.round(color.getBlue() * amount))
        );
    }
}
