import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Decorative left-side panel for login and registration screens.
 *
 * <p>The component is deliberately self-painted so the auth screens have a
 * consistent branded illustration without requiring bundled image assets.</p>
 */
public class AuthShowcasePanel extends JPanel {
    private final String headline;

    /**
     * Creates a branded showcase panel with a multi-line headline.
     *
     * @param headline text split by {@code \n} into multiple display lines
     */
    public AuthShowcasePanel(String headline) {
        this.headline = headline;
        setOpaque(false);
        setPreferredSize(new Dimension(330, 500));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel labels = new JPanel(new GridBagLayout());
        labels.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;

        JLabel brand = new JLabel("SKYCAST");
        brand.setFont(new Font("Bahnschrift", Font.BOLD, 13));
        brand.setForeground(new Color(255, 236, 196));
        labels.add(brand, constraints);

        constraints.insets.top = 14;
        for (String line : headline.split("\\n")) {
            JLabel title = new JLabel(line);
            title.setFont(new Font("Bahnschrift", Font.BOLD, 32));
            title.setForeground(Color.WHITE);
            constraints.gridy++;
            labels.add(title, constraints);
            constraints.insets.top = 0;
        }

        add(labels, BorderLayout.NORTH);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        try {
            int width = getWidth() - 1;
            int height = getHeight() - 1;
            int radius = 8;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, new Color(8, 79, 91), width, height, new Color(238, 139, 62)));
            g2.fillRoundRect(0, 0, width, height, radius, radius);

            // The diagonal texture mirrors the main background and ties screens together.
            g2.setColor(new Color(255, 255, 255, 34));
            for (int x = -height; x < width + height; x += 42) {
                g2.drawLine(x, height, x + height, 0);
            }

            paintWeatherScene(g2, width, height);
        } finally {
            g2.dispose();
        }
        super.paintComponent(graphics);
    }

    private void paintWeatherScene(Graphics2D g2, int width, int height) {
        // Simple vector weather scene: sun, cloud, rain, and atmospheric bands.
        g2.setColor(new Color(255, 197, 85, 230));
        g2.fillOval(width - 124, 86, 78, 78);

        g2.setColor(new Color(255, 255, 255, 218));
        g2.fillRoundRect(52, height - 168, 210, 54, 8, 8);
        g2.fillOval(72, height - 202, 76, 76);
        g2.fillOval(130, height - 218, 96, 96);

        g2.setColor(new Color(255, 255, 255, 120));
        for (int i = 0; i < 8; i++) {
            int x = 72 + i * 22;
            g2.drawLine(x, height - 88, x - 14, height - 48);
        }

        g2.setColor(new Color(255, 236, 196, 160));
        g2.fillRoundRect(30, height - 270, 98, 6, 6, 6);
        g2.fillRoundRect(58, height - 248, 138, 6, 6, 6);
        g2.fillRoundRect(38, height - 226, 82, 6, 6, 6);
    }
}
