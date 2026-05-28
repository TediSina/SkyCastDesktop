import java.awt.BorderLayout;
import java.awt.Color;
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

public class DashboardPanel extends SkyBackgroundPanel {
    private final SkyCastApp app;
    private final Database database;
    private final WeatherService weatherService;

    private User user;
    private WeatherData currentWeather;
    private final JTextField cityField = AppTheme.textField();
    private final JButton searchButton = AppTheme.primaryButton("Kërko");
    private final JButton loadSavedCityButton = AppTheme.secondaryButton("Hap qytetin");
    private final JButton saveCityButton = AppTheme.warmButton("Ruaj qytetin");
    private final JButton logoutButton = AppTheme.secondaryButton("Dil");
    private final JLabel welcomeLabel = AppTheme.section("");
    private final JLabel statusLabel = AppTheme.muted("Kërko një qytet për të ngarkuar motin drejtpërdrejt nga Open-Meteo.");
    private final JLabel locationLabel = AppTheme.section("Nuk ka qytet të ngarkuar");
    private final WeatherIconView weatherIconView = new WeatherIconView();
    private final WeatherCardPanel currentWeatherCard = new WeatherCardPanel();
    private final JLabel temperatureLabel = new JLabel("-- C");
    private final JLabel conditionLabel = AppTheme.muted("Kushtet aktuale do të shfaqen këtu.");
    private final JLabel feelsLikeLabel = valueLabel("--");
    private final JLabel humidityLabel = valueLabel("--");
    private final JLabel windLabel = valueLabel("--");
    private final JLabel rainLabel = valueLabel("--");
    private final JLabel observedLabel = AppTheme.muted("--");
    private final DefaultTableModel forecastModel = new DefaultTableModel(
            new String[]{"Data", "Gjendja", "Maks.", "Min.", "Reshjet", "Era"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel historyModel = new DefaultTableModel(
            new String[]{"Qyteti", "Temp.", "Gjendja", "Kërkuar më"}, 0
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
        setBorder(BorderFactory.createEmptyBorder(22, 24, 24, 24));

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        loadSavedCityButton.setToolTipText("Ngarkon qytetin e preferuar të ruajtur në llogari.");
        saveCityButton.setToolTipText("Ruan qytetin që po shfaqet si qytetin tënd të preferuar.");
        saveCityButton.setEnabled(false);

        cityField.addActionListener(event -> searchCurrentCity());
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
        welcomeLabel.setText("Mirë se erdhe, " + user.username());
        cityField.setText(user.preferredCity());
        loadHistory();
        if (user.preferredCity() != null && !user.preferredCity().isBlank()) {
            searchCurrentCity();
        }
    }

    public JButton defaultButton() {
        return searchButton;
    }

    private JPanel createHeader() {
        SurfacePanel header = new SurfacePanel(new Color(255, 255, 255, 220), new Color(196, 218, 224));
        header.setLayout(new BorderLayout(16, 8));
        header.setBorder(AppTheme.compactCardBorder());

        JPanel titlePanel = new JPanel(new GridBagLayout());
        titlePanel.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        titlePanel.add(AppTheme.display("SkyCast"), constraints);
        constraints.gridy = 1;
        titlePanel.add(welcomeLabel, constraints);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JLabel liveLabel = AppTheme.eyebrow("DREJTPËRDREJT NGA OPEN-METEO");
        liveLabel.setBorder(BorderFactory.createEmptyBorder(11, 0, 0, 12));
        actions.add(liveLabel);
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
        SurfacePanel panel = new SurfacePanel(new Color(255, 255, 255, 232), new Color(194, 218, 226));
        panel.setLayout(new BorderLayout(14, 8));
        panel.setBorder(AppTheme.compactCardBorder());

        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.add(cityField, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(loadSavedCityButton);
        actions.add(saveCityButton);
        actions.add(searchButton);
        row.add(actions, BorderLayout.EAST);

        panel.add(AppTheme.section("Moti sipas qytetit"), BorderLayout.NORTH);
        panel.add(row, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createCurrentWeatherPanel() {
        WeatherCardPanel panel = currentWeatherCard;
        panel.setLayout(new BorderLayout(12, 16));
        panel.setBorder(AppTheme.cardBorder());
        panel.setPreferredSize(new Dimension(430, 370));

        JPanel top = new JPanel(new GridBagLayout());
        top.setOpaque(false);
        locationLabel.setForeground(Color.WHITE);
        conditionLabel.setForeground(new Color(230, 249, 250));
        observedLabel.setForeground(new Color(221, 241, 244));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        top.add(locationLabel, constraints);
        constraints.gridy = 1;
        top.add(conditionLabel, constraints);

        temperatureLabel.setFont(new Font("Segoe UI", Font.BOLD, 56));
        temperatureLabel.setForeground(Color.WHITE);
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
        addMetric(details, 0, 0, "Ndihet si", feelsLikeLabel);
        addMetric(details, 1, 0, "Lagështia", humidityLabel);
        addMetric(details, 0, 1, "Era", windLabel);
        addMetric(details, 1, 1, "Reshjet", rainLabel);

        panel.add(top, BorderLayout.NORTH);
        panel.add(details, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createForecastPanel() {
        SurfacePanel panel = new SurfacePanel();
        panel.setLayout(new BorderLayout(10, 12));
        panel.setBorder(AppTheme.cardBorder());

        JTable table = new JTable(forecastModel);
        AppTheme.styleTable(table);
        table.setRowSelectionAllowed(false);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(AppTheme.section("Parashikimi 7-ditor"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createHistoryPanel() {
        SurfacePanel panel = new SurfacePanel(new Color(255, 255, 255, 236), new Color(197, 218, 226));
        panel.setLayout(new BorderLayout(10, 12));
        panel.setBorder(AppTheme.cardBorder());
        panel.setPreferredSize(new Dimension(0, 210));

        JTable table = new JTable(historyModel);
        AppTheme.styleTable(table);
        table.setRowSelectionAllowed(false);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(AppTheme.section("Kërkimet e fundit"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void addMetric(JPanel panel, int x, int y, String label, JLabel value) {
        SurfacePanel metric = new SurfacePanel(new Color(255, 255, 255, 215), new Color(255, 255, 255, 80));
        metric.setLayout(new BorderLayout(2, 4));
        metric.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        JLabel metricLabel = AppTheme.muted(label);
        metricLabel.setForeground(new Color(66, 86, 96));
        metric.add(metricLabel, BorderLayout.NORTH);
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
            JOptionPane.showMessageDialog(this, "Shkruaj emrin e qytetit.", "Mungon qyteti", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (activeUser == null) {
            return;
        }

        setLoading(true, "Po ngarkohet moti për " + city + "...");
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
                    statusLabel.setText("Të dhënat u ngarkuan drejtpërdrejt nga Open-Meteo.");
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    statusLabel.setText("Moti nuk mund të ngarkohej.");
                    JOptionPane.showMessageDialog(
                            DashboardPanel.this,
                            cause.getMessage(),
                            "Gabim moti",
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
        currentWeatherCard.setWeatherCode(weather.weatherCode());
        locationLabel.setText(weather.location().displayName());
        weatherIconView.setIcon(WeatherCodes.icon(weather.weatherCode()));
        conditionLabel.setText(WeatherCodes.describeWithIcon(weather.weatherCode()));
        temperatureLabel.setText(format(weather.temperature()) + " °C");
        feelsLikeLabel.setText(format(weather.apparentTemperature()) + " °C");
        humidityLabel.setText(weather.humidity() + "%");
        windLabel.setText(format(weather.windSpeed()) + " km/h");
        rainLabel.setText(format(weather.precipitation()) + " mm");
        observedLabel.setText("Vëzhguar më: " + weather.observedAt());

        forecastModel.setRowCount(0);
        for (DailyForecast day : weather.dailyForecasts()) {
            forecastModel.addRow(new Object[]{
                    day.date(),
                    WeatherCodes.describeWithIcon(day.weatherCode()),
                    format(day.highTemperature()) + " °C",
                    format(day.lowTemperature()) + " °C",
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
                        format(item.temperature()) + " °C",
                        WeatherCodes.describeWithIcon(item.weatherCode()),
                        item.searchedAt()
                });
            }
        } catch (SQLException ex) {
            historyModel.setRowCount(0);
            historyModel.addRow(new Object[]{"Historiku nuk është i disponueshëm", "", "", ""});
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
            JOptionPane.showMessageDialog(this, "Kërko fillimisht një qytet.", "Nuk ka qytet të ngarkuar", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            app.authService().updatePreferredCity(user, currentWeather.location().name());
            statusLabel.setText(currentWeather.location().displayName() + " u ruajt si qyteti yt i preferuar.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Qyteti nuk mund të ruhej.\n\n" + ex.getMessage(),
                    "Ruajtja dështoi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String format(double value) {
        return String.format(Locale.US, "%.1f", value);
    }
}
