import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class DashboardPanel extends JPanel {
    private final SkyCastApp app;
    private final Database database;
    private final WeatherService weatherService;

    private User user;
    private WeatherData currentWeather;
    private final JTextField cityField = AppTheme.textField();
    private final JButton searchButton = AppTheme.primaryButton("Search");
    private final JButton loadSavedCityButton = AppTheme.secondaryButton("Load Saved");
    private final JButton saveCityButton = AppTheme.warmButton("Save City");
    private final JButton logoutButton = AppTheme.secondaryButton("Logout");
    private final JLabel welcomeLabel = AppTheme.section("");
    private final JLabel statusLabel = AppTheme.muted("Search a city to load live Open-Meteo weather.");
    private final JLabel locationLabel = AppTheme.section("No city loaded");
    private final WeatherIconView weatherIconView = new WeatherIconView();
    private final JLabel temperatureLabel = new JLabel("-- C");
    private final JLabel conditionLabel = AppTheme.muted("Current conditions will appear here.");
    private final JLabel feelsLikeLabel = valueLabel("--");
    private final JLabel humidityLabel = valueLabel("--");
    private final JLabel windLabel = valueLabel("--");
    private final JLabel rainLabel = valueLabel("--");
    private final JLabel observedLabel = AppTheme.muted("--");
    private final DefaultTableModel forecastModel = new DefaultTableModel(
            new String[]{"Date", "Condition", "High", "Low", "Rain", "Wind"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel historyModel = new DefaultTableModel(
            new String[]{"City", "Temp", "Condition", "Searched"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public DashboardPanel(SkyCastApp app, Database database, WeatherService weatherService) {
        this.app = app;
        this.database = database;
        this.weatherService = weatherService;

        setLayout(new BorderLayout(18, 18));
        setBackground(AppTheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(22, 24, 24, 24));

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        loadSavedCityButton.setToolTipText("Loads the preferred city saved on your account.");
        saveCityButton.setToolTipText("Saves the city currently displayed as your preferred city.");
        saveCityButton.setEnabled(false);

        searchButton.addActionListener(event -> searchCurrentCity());
        loadSavedCityButton.addActionListener(event -> {
            if (user != null) {
                cityField.setText(user.preferredCity());
                searchCurrentCity();
            }
        });
        saveCityButton.addActionListener(event -> saveCurrentCity());
        logoutButton.addActionListener(event -> app.showLogin());
    }

    public void setUser(User user) {
        this.user = user;
        this.currentWeather = null;
        saveCityButton.setEnabled(false);
        welcomeLabel.setText("Welcome, " + user.username());
        cityField.setText(user.preferredCity());
        loadHistory();
        if (user.preferredCity() != null && !user.preferredCity().isBlank()) {
            searchCurrentCity();
        }
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 8));
        header.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridBagLayout());
        titlePanel.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        titlePanel.add(AppTheme.title("SkyCast"), constraints);
        constraints.gridy = 1;
        titlePanel.add(welcomeLabel, constraints);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(logoutButton);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(18, 18));
        content.setOpaque(false);
        content.add(createSearchPanel(), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 0, 0, 18);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.48;
        constraints.weighty = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        center.add(createCurrentWeatherPanel(), constraints);

        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.weightx = 0.52;
        constraints.gridx = 1;
        center.add(createForecastPanel(), constraints);

        content.add(center, BorderLayout.CENTER);
        content.add(createHistoryPanel(), BorderLayout.SOUTH);
        return content;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(14, 8));
        panel.setBackground(AppTheme.SURFACE);
        panel.setBorder(AppTheme.cardBorder());

        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.add(cityField, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(loadSavedCityButton);
        actions.add(saveCityButton);
        actions.add(searchButton);
        row.add(actions, BorderLayout.EAST);

        panel.add(AppTheme.section("City Weather"), BorderLayout.NORTH);
        panel.add(row, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createCurrentWeatherPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 16));
        panel.setBackground(AppTheme.SURFACE);
        panel.setBorder(AppTheme.cardBorder());
        panel.setPreferredSize(new Dimension(420, 360));

        JPanel top = new JPanel(new GridBagLayout());
        top.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        top.add(locationLabel, constraints);
        constraints.gridy = 1;
        top.add(conditionLabel, constraints);

        temperatureLabel.setFont(new Font("Segoe UI", Font.BOLD, 56));
        temperatureLabel.setForeground(AppTheme.PRIMARY_DARK);
        constraints.gridy = 2;
        constraints.insets = new Insets(12, 0, 0, 0);
        top.add(temperatureLabel, constraints);
        constraints.gridy = 3;
        constraints.insets = new Insets(0, 0, 0, 0);
        top.add(observedLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridheight = 4;
        constraints.insets = new Insets(0, 28, 0, 0);
        constraints.anchor = GridBagConstraints.NORTHWEST;
        top.add(weatherIconView, constraints);

        JPanel details = new JPanel(new GridBagLayout());
        details.setOpaque(false);
        addMetric(details, 0, 0, "Feels like", feelsLikeLabel);
        addMetric(details, 1, 0, "Humidity", humidityLabel);
        addMetric(details, 0, 1, "Wind", windLabel);
        addMetric(details, 1, 1, "Rain", rainLabel);

        panel.add(top, BorderLayout.NORTH);
        panel.add(details, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createForecastPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 12));
        panel.setBackground(AppTheme.SURFACE);
        panel.setBorder(AppTheme.cardBorder());

        JTable table = new JTable(forecastModel);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setRowSelectionAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);

        panel.add(AppTheme.section("7-Day Forecast"), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 12));
        panel.setBackground(AppTheme.SURFACE);
        panel.setBorder(AppTheme.cardBorder());
        panel.setPreferredSize(new Dimension(0, 210));

        JTable table = new JTable(historyModel);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setRowSelectionAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);

        panel.add(AppTheme.section("Recent Searches"), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void addMetric(JPanel panel, int x, int y, String label, JLabel value) {
        JPanel metric = new JPanel(new BorderLayout(2, 4));
        metric.setOpaque(false);
        metric.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        metric.add(AppTheme.muted(label), BorderLayout.NORTH);
        metric.add(value, BorderLayout.CENTER);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(6, 6, 6, 6);
        panel.add(metric, constraints);
    }

    private static JLabel valueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(AppTheme.TEXT);
        return label;
    }

    private void searchCurrentCity() {
        User activeUser = user;
        String city = cityField.getText().trim();
        if (city.isBlank()) {
            JOptionPane.showMessageDialog(this, "Enter a city name.", "Missing City", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (activeUser == null) {
            return;
        }

        setLoading(true, "Loading weather for " + city + "...");
        SwingWorker<WeatherData, Void> worker = new SwingWorker<>() {
            @Override
            protected WeatherData doInBackground() throws Exception {
                LocationResult location = weatherService.searchLocation(city);
                return weatherService.fetchWeather(location);
            }

            @Override
            protected void done() {
                try {
                    WeatherData weather = get();
                    showWeather(weather);
                    database.saveWeatherSearch(activeUser, weather);
                    loadHistory();
                    statusLabel.setText("Live data loaded from Open-Meteo.");
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    statusLabel.setText("Could not load weather.");
                    JOptionPane.showMessageDialog(
                            DashboardPanel.this,
                            cause.getMessage(),
                            "Weather Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setLoading(false, statusLabel.getText());
                }
            }
        };
        worker.execute();
    }

    private void showWeather(WeatherData weather) {
        currentWeather = weather;
        saveCityButton.setEnabled(true);
        locationLabel.setText(weather.location().displayName());
        weatherIconView.setIcon(WeatherCodes.icon(weather.weatherCode()));
        conditionLabel.setText(WeatherCodes.describeWithIcon(weather.weatherCode()));
        temperatureLabel.setText(format(weather.temperature()) + " C");
        feelsLikeLabel.setText(format(weather.apparentTemperature()) + " C");
        humidityLabel.setText(weather.humidity() + "%");
        windLabel.setText(format(weather.windSpeed()) + " km/h");
        rainLabel.setText(format(weather.precipitation()) + " mm");
        observedLabel.setText("Observed: " + weather.observedAt());

        forecastModel.setRowCount(0);
        for (DailyForecast day : weather.dailyForecasts()) {
            forecastModel.addRow(new Object[]{
                    day.date(),
                    WeatherCodes.describeWithIcon(day.weatherCode()),
                    format(day.highTemperature()) + " C",
                    format(day.lowTemperature()) + " C",
                    format(day.precipitation()) + " mm",
                    format(day.maxWindSpeed()) + " km/h"
            });
        }
    }

    private void loadHistory() {
        if (user == null) {
            return;
        }

        try {
            List<SearchHistoryItem> items = database.recentSearches(user, 8);
            historyModel.setRowCount(0);
            for (SearchHistoryItem item : items) {
                historyModel.addRow(new Object[]{
                        item.displayCity(),
                        format(item.temperature()) + " C",
                        WeatherCodes.describeWithIcon(item.weatherCode()),
                        item.searchedAt()
                });
            }
        } catch (SQLException ex) {
            historyModel.setRowCount(0);
            historyModel.addRow(new Object[]{"History unavailable", "", "", ""});
        }
    }

    private void setLoading(boolean loading, String text) {
        searchButton.setEnabled(!loading);
        loadSavedCityButton.setEnabled(!loading);
        saveCityButton.setEnabled(!loading && currentWeather != null);
        cityField.setEnabled(!loading);
        statusLabel.setText(text);
    }

    private void saveCurrentCity() {
        if (user == null || currentWeather == null) {
            JOptionPane.showMessageDialog(this, "Search for a city first.", "No City Loaded", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            app.authService().updatePreferredCity(user, currentWeather.location().name());
            statusLabel.setText(currentWeather.location().displayName() + " is now your saved city.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not save the city.\n\n" + ex.getMessage(),
                    "Save Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String format(double value) {
        return String.format(Locale.US, "%.1f", value);
    }
}
