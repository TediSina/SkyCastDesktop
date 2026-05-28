import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * Main current-weather card with a gradient that changes by weather code.
 */
public class WeatherCardPanel extends JPanel {
    private int weatherCode = 0;

    /**
     * Creates an initially clear-weather card.
     */
    public WeatherCardPanel() {
        setOpaque(false);
    }

    /**
     * Updates the weather condition used to select the card gradient.
     *
     * @param weatherCode Open-Meteo weather code
     */
    public void setWeatherCode(int weatherCode) {
        this.weatherCode = weatherCode;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        try {
            int width = getWidth() - 1;
            int height = getHeight() - 4;
            Color[] colors = colorsFor(weatherCode);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Paint shadow and gradient manually to make the card feel less like a default JPanel.
            g2.setColor(new Color(14, 50, 63, 34));
            g2.fillRoundRect(3, 4, width - 4, height - 1, 8, 8);
            g2.setPaint(new GradientPaint(0, 0, colors[0], width, height, colors[1]));
            g2.fillRoundRect(0, 0, width - 3, height - 3, 8, 8);

            g2.setColor(new Color(255, 255, 255, 34));
            for (int x = -height; x < width + height; x += 38) {
                g2.drawLine(x, height, x + height, 0);
            }

            g2.setColor(new Color(255, 255, 255, 82));
            g2.drawRoundRect(0, 0, width - 3, height - 3, 8, 8);
        } finally {
            g2.dispose();
        }
        super.paintComponent(graphics);
    }

    private Color[] colorsFor(int code) {
        // Group Open-Meteo codes into visual weather families.
        return switch (code) {
            case 0, 1 -> new Color[]{new Color(17, 141, 166), new Color(243, 154, 73)};
            case 2, 3, 45, 48 -> new Color[]{new Color(56, 103, 129), new Color(92, 158, 168)};
            case 51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 ->
                    new Color[]{new Color(31, 78, 103), new Color(36, 144, 160)};
            case 71, 73, 75, 77, 85, 86 ->
                    new Color[]{new Color(62, 116, 156), new Color(173, 220, 230)};
            case 95, 96, 99 -> new Color[]{new Color(38, 45, 67), new Color(170, 95, 65)};
            default -> new Color[]{AppTheme.PRIMARY_DARK, AppTheme.OCEAN};
        };
    }
}
