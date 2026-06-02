import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.List;
import java.util.Locale;

/**
 * Responsive chart card for the next twelve hourly weather points.
 */
public class HourlyWeatherChartPanel extends SurfacePanel {
    private static final Color GRID = new Color(212, 226, 231);
    private static final Color TEMP = AppTheme.ACCENT;
    private static final Color WIND = AppTheme.MINT;
    private static final Color RAIN = new Color(53, 142, 186);
    private static final Color DOT = Color.WHITE;

    private List<HourlyForecast> forecasts = List.of();

    /**
     * Creates the hourly trend graph.
     */
    public HourlyWeatherChartPanel() {
        setBorder(AppTheme.cardBorder());
        setPreferredSize(new Dimension(420, 285));
        setMinimumSize(new Dimension(260, 235));
        setToolTipText("");
    }

    /**
     * Replaces the chart data and repaints the graph.
     */
    public void setForecasts(List<HourlyForecast> forecasts) {
        this.forecasts = forecasts == null ? List.of() : List.copyOf(forecasts);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g2 = (Graphics2D) graphics.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Insets insets = getInsets();
            int left = insets.left;
            int right = getWidth() - insets.right;
            int top = insets.top;
            int bottom = getHeight() - insets.bottom;

            drawTitle(g2, left, top, "12 orët e ardhshme", "Temperatura, reshjet dhe era");
            if (forecasts.isEmpty()) {
                drawEmptyState(g2, left, top, right, bottom);
                return;
            }

            int chartLeft = left + 44;
            int chartRight = Math.max(chartLeft + 1, right - 14);
            int chartTop = top + 76;
            int chartBottom = Math.max(chartTop + 1, bottom - 48);

            double minTemp = forecasts.stream().mapToDouble(HourlyForecast::temperature).min().orElse(0);
            double maxTemp = forecasts.stream().mapToDouble(HourlyForecast::temperature).max().orElse(1);
            double maxRain = Math.max(1.0, forecasts.stream().mapToDouble(HourlyForecast::precipitation).max().orElse(0));
            double maxWind = forecasts.stream().mapToDouble(HourlyForecast::windSpeed).max().orElse(0);
            double padding = Math.max(1.0, (maxTemp - minTemp) * 0.18);
            minTemp -= padding;
            maxTemp += padding;

            drawLegend(g2, left, right, top);
            drawGrid(g2, chartLeft, chartRight, chartTop, chartBottom, minTemp, maxTemp);
            drawRainBars(g2, chartLeft, chartRight, chartBottom, maxRain);
            drawWindLine(g2, chartLeft, chartRight, chartTop, chartBottom, maxWind);
            drawTemperatureLine(g2, chartLeft, chartRight, chartTop, chartBottom, minTemp, maxTemp);
            drawTimeLabels(g2, chartLeft, chartRight, chartBottom);
        } finally {
            g2.dispose();
        }
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if (forecasts.isEmpty()) {
            return null;
        }

        Insets insets = getInsets();
        int left = insets.left + 44;
        int right = Math.max(left + 1, getWidth() - insets.right - 14);
        int top = insets.top + 76;
        int bottom = Math.max(top + 1, getHeight() - insets.bottom - 48);

        double minTemp = forecasts.stream().mapToDouble(HourlyForecast::temperature).min().orElse(0);
        double maxTemp = forecasts.stream().mapToDouble(HourlyForecast::temperature).max().orElse(1);
        double padding = Math.max(1.0, (maxTemp - minTemp) * 0.18);
        minTemp -= padding;
        maxTemp += padding;
        double maxRain = Math.max(1.0, forecasts.stream().mapToDouble(HourlyForecast::precipitation).max().orElse(0));
        double maxWind = Math.max(1.0, forecasts.stream().mapToDouble(HourlyForecast::windSpeed).max().orElse(0));

        TooltipCandidate best = null;
        int count = forecasts.size();
        for (int i = 0; i < count; i++) {
            HourlyForecast forecast = forecasts.get(i);
            int x = xAt(i, count, left, right);
            int tempY = yAt(forecast.temperature(), minTemp, maxTemp, top, bottom);
            best = closer(best, pointTooltip(event, x, tempY, 11,
                    tooltip(hourLabel(forecast.time()), "Temp.", format(forecast.temperature()) + " °C")));

            int windY = windYAt(forecast.windSpeed(), maxWind, top, bottom);
            best = closer(best, pointTooltip(event, x, windY, 11,
                    tooltip(hourLabel(forecast.time()), "Erë", format(forecast.windSpeed()) + " km/h")));

            TooltipCandidate rain = rainTooltip(event, forecast, x, count, left, right, bottom, maxRain);
            best = closer(best, rain);
        }

        return best == null ? null : best.text();
    }

    private void drawTitle(Graphics2D g2, int x, int y, String title, String subtitle) {
        g2.setFont(AppTheme.SECTION);
        g2.setColor(AppTheme.TEXT);
        g2.drawString(title, x, y + 4);
        g2.setFont(AppTheme.BODY);
        g2.setColor(AppTheme.MUTED);
        g2.drawString(subtitle, x, y + 25);
    }

    private void drawLegend(Graphics2D g2, int left, int right, int top) {
        g2.setFont(AppTheme.BODY);
        FontMetrics metrics = g2.getFontMetrics();
        String temperatureLabel = "Temp.";
        String windLabel = "Erë";
        String rainLabel = "Reshje";
        int markerGap = 8;
        int itemGap = 20;
        int legendWidth = 18 + markerGap + metrics.stringWidth(temperatureLabel)
                + itemGap + 18 + markerGap + metrics.stringWidth(windLabel)
                + itemGap + 13 + markerGap + metrics.stringWidth(rainLabel);
        int x = Math.max(left, right - legendWidth);

        if (legendWidth > right - left) {
            drawTemperatureLegendItem(g2, left, top + 42, temperatureLabel);
            drawWindLegendItem(g2, left, top + 60, windLabel);
            drawRainLegendItem(g2, left, top + 78, rainLabel);
            return;
        }

        drawTemperatureLegendItem(g2, x, top + 4, temperatureLabel);
        int windX = x + 18 + markerGap + metrics.stringWidth(temperatureLabel) + itemGap;
        drawWindLegendItem(g2, windX, top + 4, windLabel);
        int rainX = windX + 18 + markerGap + metrics.stringWidth(windLabel) + itemGap;
        drawRainLegendItem(g2, rainX, top + 2, rainLabel);
    }

    private void drawTemperatureLegendItem(Graphics2D g2, int x, int y, String label) {
        g2.setColor(TEMP);
        g2.fillRoundRect(x, y, 18, 5, 5, 5);
        g2.setColor(AppTheme.MUTED);
        g2.drawString(label, x + 26, y + 6);
    }

    private void drawWindLegendItem(Graphics2D g2, int x, int y, String label) {
        g2.setColor(WIND);
        g2.fillRoundRect(x, y, 18, 5, 5, 5);
        g2.setColor(AppTheme.MUTED);
        g2.drawString(label, x + 26, y + 6);
    }

    private void drawRainLegendItem(Graphics2D g2, int x, int y, String label) {
        g2.setColor(RAIN);
        g2.fillRoundRect(x, y, 13, 9, 5, 5);
        g2.setColor(AppTheme.MUTED);
        g2.drawString(label, x + 21, y + 8);
    }

    private void drawGrid(Graphics2D g2, int left, int right, int top, int bottom, double min, double max) {
        g2.setFont(AppTheme.BODY);
        FontMetrics metrics = g2.getFontMetrics();
        g2.setStroke(new BasicStroke(1f));
        for (int i = 0; i <= 3; i++) {
            int y = top + ((bottom - top) * i / 3);
            double value = max - ((max - min) * i / 3);
            g2.setColor(GRID);
            g2.drawLine(left, y, right, y);
            g2.setColor(AppTheme.MUTED);
            String label = format(value) + "°";
            g2.drawString(label, left - metrics.stringWidth(label) - 8, y + 4);
        }
    }

    private void drawRainBars(Graphics2D g2, int left, int right, int bottom, double maxRain) {
        int count = forecasts.size();
        int width = Math.max(4, (right - left) / Math.max(18, count * 2));
        int maxHeight = 54;

        g2.setColor(new Color(RAIN.getRed(), RAIN.getGreen(), RAIN.getBlue(), 115));
        for (int i = 0; i < count; i++) {
            int x = xAt(i, count, left, right) - width / 2;
            int height = (int) Math.round((forecasts.get(i).precipitation() / maxRain) * maxHeight);
            if (forecasts.get(i).precipitation() > 0 && height < 3) {
                height = 3;
            }
            g2.fillRoundRect(x, bottom - height, width, height, 5, 5);
        }
    }

    private void drawWindLine(Graphics2D g2, int left, int right, int top, int bottom, double maxWind) {
        int count = forecasts.size();
        double scale = Math.max(1.0, maxWind);
        Path2D path = new Path2D.Double();
        for (int i = 0; i < count; i++) {
            int x = xAt(i, count, left, right);
            int y = windYAt(forecasts.get(i).windSpeed(), scale, top, bottom);
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }

        g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(WIND.getRed(), WIND.getGreen(), WIND.getBlue(), 210));
        g2.draw(path);

        for (int i = 0; i < count; i++) {
            int x = xAt(i, count, left, right);
            int y = windYAt(forecasts.get(i).windSpeed(), scale, top, bottom);
            g2.setColor(DOT);
            g2.fillOval(x - 3, y - 3, 6, 6);
            g2.setColor(WIND);
            g2.drawOval(x - 3, y - 3, 6, 6);
        }
    }

    private void drawTemperatureLine(Graphics2D g2, int left, int right, int top, int bottom, double min, double max) {
        int count = forecasts.size();
        Path2D path = new Path2D.Double();
        for (int i = 0; i < count; i++) {
            int x = xAt(i, count, left, right);
            int y = yAt(forecasts.get(i).temperature(), min, max, top, bottom);
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }

        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(TEMP);
        g2.draw(path);

        g2.setFont(AppTheme.BODY_BOLD);
        for (int i = 0; i < count; i++) {
            int x = xAt(i, count, left, right);
            int y = yAt(forecasts.get(i).temperature(), min, max, top, bottom);
            g2.setColor(DOT);
            g2.fillOval(x - 4, y - 4, 8, 8);
            g2.setColor(TEMP);
            g2.drawOval(x - 4, y - 4, 8, 8);
            if (i == 0 || i == count - 1 || i % 3 == 0) {
                g2.drawString(format(forecasts.get(i).temperature()) + "°", x - 14, y - 9);
            }
        }
    }

    private void drawTimeLabels(Graphics2D g2, int left, int right, int bottom) {
        g2.setFont(AppTheme.BODY);
        g2.setColor(AppTheme.MUTED);
        FontMetrics metrics = g2.getFontMetrics();
        int count = forecasts.size();
        int step = count <= 6 ? 1 : 2;
        for (int i = 0; i < count; i += step) {
            String label = hourLabel(forecasts.get(i).time());
            int x = xAt(i, count, left, right);
            g2.drawString(label, x - metrics.stringWidth(label) / 2, bottom + 25);
        }
    }

    private void drawEmptyState(Graphics2D g2, int left, int top, int right, int bottom) {
        g2.setFont(AppTheme.BODY);
        g2.setColor(AppTheme.MUTED);
        String message = "Grafiku do të shfaqet pasi të ngarkohet moti.";
        FontMetrics metrics = g2.getFontMetrics();
        g2.drawString(message, left + ((right - left) - metrics.stringWidth(message)) / 2, top + (bottom - top) / 2);
    }

    private int xAt(int index, int count, int left, int right) {
        if (count <= 1) {
            return left + (right - left) / 2;
        }
        return left + (int) Math.round((right - left) * (index / (double) (count - 1)));
    }

    private int yAt(double value, double min, double max, int top, int bottom) {
        if (Math.abs(max - min) < 0.001) {
            return top + (bottom - top) / 2;
        }
        double ratio = (value - min) / (max - min);
        return bottom - (int) Math.round((bottom - top) * ratio);
    }

    private int windYAt(double value, double max, int top, int bottom) {
        double ratio = Math.max(0, Math.min(1, value / max));
        return bottom - (int) Math.round((bottom - top) * ratio);
    }

    private TooltipCandidate pointTooltip(MouseEvent event, int x, int y, int radius, String text) {
        int dx = event.getX() - x;
        int dy = event.getY() - y;
        int distance = (dx * dx) + (dy * dy);
        return distance <= radius * radius ? new TooltipCandidate(distance, text) : null;
    }

    private TooltipCandidate rainTooltip(
            MouseEvent event,
            HourlyForecast forecast,
            int x,
            int count,
            int left,
            int right,
            int bottom,
            double maxRain
    ) {
        if (forecast.precipitation() <= 0) {
            return null;
        }

        int width = Math.max(4, (right - left) / Math.max(18, count * 2));
        int height = (int) Math.round((forecast.precipitation() / maxRain) * 54);
        if (height < 3) {
            height = 3;
        }

        int barLeft = x - width / 2;
        int barRight = barLeft + width;
        int barTop = bottom - height;
        if (event.getX() >= barLeft - 2 && event.getX() <= barRight + 2
                && event.getY() >= barTop - 2 && event.getY() <= bottom + 2) {
            int centerY = barTop + height / 2;
            int dx = event.getX() - x;
            int dy = event.getY() - centerY;
            int distance = (dx * dx) + (dy * dy);
            return new TooltipCandidate(distance,
                    tooltip(hourLabel(forecast.time()), "Reshje", format(forecast.precipitation()) + " mm"));
        }
        return null;
    }

    private TooltipCandidate closer(TooltipCandidate current, TooltipCandidate candidate) {
        if (candidate == null) {
            return current;
        }
        if (current == null || candidate.distance() < current.distance()) {
            return candidate;
        }
        return current;
    }

    private String tooltip(String label, String metric, String value) {
        return "<html><b>" + label + "</b><br>" + metric + ": " + value + "</html>";
    }

    private record TooltipCandidate(int distance, String text) {
    }

    private String hourLabel(String time) {
        int separator = time == null ? -1 : time.indexOf('T');
        if (separator >= 0 && time.length() >= separator + 3) {
            return time.substring(separator + 1, separator + 3) + ":00";
        }
        return time == null ? "" : time;
    }

    private String format(double value) {
        return String.format(Locale.US, "%.1f", value);
    }
}
