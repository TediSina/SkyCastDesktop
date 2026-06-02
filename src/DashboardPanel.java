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
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Main authenticated screen for searching and displaying weather data.
 */
public class DashboardPanel extends SkyBackgroundPanel {
    private final SkyCastApp app;
    private final Database database;
    private final WeatherService weatherService;

    private User user;
    private WeatherData currentWeather;
    private boolean loading;
    private final JTextField cityField = AppTheme.textField();
    private final JButton searchButton = AppTheme.primaryButton("Kërko");
    private final JButton loadSavedCityButton = AppTheme.secondaryButton("Hap të ruajturin");
    private final JButton saveCityButton = AppTheme.warmButton("Ruaj në listë");
    private final JButton logoutButton = AppTheme.secondaryButton("Dil");
    private final DefaultComboBoxModel<SavedCity> savedCityModel = new DefaultComboBoxModel<>();
    private final JComboBox<SavedCity> savedCityBox = new JComboBox<>(savedCityModel);
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

    /**
     * Creates the dashboard and wires weather actions.
     *
     * @param app parent frame used for navigation and shared services
     * @param database local database for weather history
     * @param weatherService Open-Meteo service client
     */
    public DashboardPanel(SkyCastApp app, Database database, WeatherService weatherService) {
        this.app = app;
        this.database = database;
        this.weatherService = weatherService;

        setLayout(new BorderLayout(18, 18));
        setBorder(BorderFactory.createEmptyBorder(22, 24, 24, 24));

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        styleSavedCityBox();
        loadSavedCityButton.setToolTipText("Ngarkon qytetin e zgjedhur nga lista jote.");
        saveCityButton.setToolTipText("Shton qytetin që po shfaqet në listën tënde të ruajtur.");
        loadSavedCityButton.setEnabled(false);
        saveCityButton.setEnabled(false);
        savedCityBox.setEnabled(false);

        cityField.addActionListener(event -> searchCurrentCity());
        searchButton.addActionListener(event -> searchCurrentCity());
        loadSavedCityButton.addActionListener(event -> loadSelectedSavedCity());
        saveCityButton.addActionListener(event -> saveCurrentCity());
        logoutButton.addActionListener(event -> app.showLogin());
    }

    /**
     * Binds the dashboard to the logged-in user and loads their saved city.
     *
     * @param user authenticated user
     */
    public void setUser(User user) {
        this.user = user;
        this.currentWeather = null;
        saveCityButton.setEnabled(false);
        welcomeLabel.setText("Mirë se erdhe, " + user.username());
        cityField.setText(user.preferredCity());
        loadSavedCities();
        loadHistory();
        if (user.preferredCity() != null && !user.preferredCity().isBlank()) {
            searchCurrentCity();
        }
    }

    /**
     * Returns the dashboard default Enter action.
     *
     * @return search button
     */
    public JButton defaultButton() {
        return searchButton;
    }

    /**
     * Applies app styling to the saved-city selector.
     */
    private void styleSavedCityBox() {
        savedCityBox.setFont(AppTheme.BODY);
        savedCityBox.setForeground(AppTheme.TEXT);
        savedCityBox.setBackground(AppTheme.FIELD);
        savedCityBox.setBorder(AppTheme.inputBorder());
        savedCityBox.setPreferredSize(new Dimension(360, 44));
    }

    /**
     * Builds the top application header.
     */
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

    /**
     * Builds the dashboard body: search, current weather, forecast, and history.
     */
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

    /**
     * Builds the city search and saved-city controls.
     */
    private JPanel createSearchPanel() {
        SurfacePanel panel = new SurfacePanel(new Color(255, 255, 255, 232), new Color(194, 218, 226));
        panel.setLayout(new BorderLayout(14, 8));
        panel.setBorder(AppTheme.compactCardBorder());

        JPanel rows = new JPanel(new GridBagLayout());
        rows.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        rows.add(createSearchRow(), constraints);

        constraints.gridy = 1;
        constraints.insets = new Insets(10, 0, 0, 0);
        rows.add(createSavedCitiesRow(), constraints);

        panel.add(AppTheme.section("Moti sipas qytetit"), BorderLayout.NORTH);
        panel.add(rows, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Builds the direct city-search row.
     */
    private JPanel createSearchRow() {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.add(cityField, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(searchButton);
        row.add(actions, BorderLayout.EAST);
        return row;
    }

    /**
     * Builds controls for saved-city selection and saving.
     */
    private JPanel createSavedCitiesRow() {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JPanel savedPicker = new JPanel(new BorderLayout(8, 0));
        savedPicker.setOpaque(false);
        JLabel label = AppTheme.eyebrow("QYTETE TË RUAJTURA");
        label.setBorder(BorderFactory.createEmptyBorder(11, 0, 0, 0));
        savedPicker.add(label, BorderLayout.WEST);
        savedPicker.add(savedCityBox, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(loadSavedCityButton);
        actions.add(saveCityButton);

        row.add(savedPicker, BorderLayout.CENTER);
        row.add(actions, BorderLayout.EAST);
        return row;
    }

    /**
     * Builds the prominent current-weather card.
     */
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

    /**
     * Builds the 7-day forecast table panel.
     */
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

    /**
     * Builds the recent-search history table panel.
     */
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

    /**
     * Adds one small metric card to the current-weather details grid.
     */
    private void addMetric(JPanel panel, int x, int y, String label, JLabel value) {
        SurfacePanel metric = new SurfacePanel(new Color(255, 255, 255, 215), new Color(255, 255, 255, 80));
        metric.setLayout(new BorderLayout(2, 1));
        metric.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        metric.setMinimumSize(new Dimension(0, 0));
        JLabel metricLabel = AppTheme.muted(label);
        metricLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        metricLabel.setForeground(new Color(66, 86, 96));
        metric.add(metricLabel, BorderLayout.NORTH);
        metric.add(value, BorderLayout.CENTER);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(3, 3, 3, 3);
        panel.add(metric, constraints);
    }

    /**
     * Creates a bold label for metric values.
     */
    private static JLabel valueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(AppTheme.TEXT);
        return label;
    }

    /**
     * Resolves the city, fetches weather data, and saves the lookup to history.
     */
    private void searchCurrentCity() {
        String city = cityField.getText().trim();
        if (city.isBlank()) {
            JOptionPane.showMessageDialog(this, "Shkruaj emrin e qytetit.", "Mungon qyteti", JOptionPane.WARNING_MESSAGE);
            return;
        }

        loadWeather(city, null, false);
    }

    /**
     * Loads the selected saved city into the dashboard.
     */
    private void loadSelectedSavedCity() {
        SavedCity savedCity = (SavedCity) savedCityBox.getSelectedItem();
        if (savedCity == null) {
            JOptionPane.showMessageDialog(this, "Nuk ka qytet të ruajtur për t'u hapur.", "Lista është bosh", JOptionPane.WARNING_MESSAGE);
            return;
        }

        cityField.setText(savedCity.city());
        loadWeather(savedCity.displayName(), savedCity.toLocationResult(), true);
    }

    /**
     * Fetches weather data from a typed query or a previously saved location.
     */
    private void loadWeather(String city, LocationResult savedLocation, boolean fromSavedCity) {
        User activeUser = user;
        if (activeUser == null) {
            return;
        }

        setLoading(true, "Po ngarkohet moti për " + city + "...");
        // Network calls run off the Swing event dispatch thread so the UI remains responsive.
        SwingWorker<WeatherData, Void> worker = new SwingWorker<>() {
            @Override
            protected WeatherData doInBackground() throws Exception {
                LocationResult location = savedLocation == null ? weatherService.searchLocation(city) : savedLocation;
                return weatherService.fetchWeather(location);
            }

            @Override
            protected void done() {
                try {
                    WeatherData weather = get();
                    showWeather(weather);
                    database.saveWeatherSearch(activeUser, weather);
                    if (fromSavedCity && savedLocation == null) {
                        database.saveCity(activeUser, weather.location());
                        loadSavedCities();
                        selectSavedCity(weather.location());
                    }
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

    /**
     * Copies a fetched weather response into the visible dashboard components.
     */
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

    /**
     * Loads recent searches from SQLite into the history table.
     */
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

    /**
     * Loads the user's saved city shortcuts into the selector.
     */
    private void loadSavedCities() {
        savedCityModel.removeAllElements();
        if (user == null) {
            updateSavedCityControls();
            return;
        }

        try {
            List<SavedCity> cities = database.savedCities(user);
            for (SavedCity city : cities) {
                savedCityModel.addElement(city);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Qytetet e ruajtura nuk mund të ngarkoheshin.\n\n" + ex.getMessage(),
                    "Gabim gjatë ngarkimit",
                    JOptionPane.ERROR_MESSAGE
            );
        }
        updateSavedCityControls();
    }

    /**
     * Selects the saved-city combo item that matches the displayed weather.
     */
    private void selectSavedCity(LocationResult location) {
        for (int i = 0; i < savedCityModel.getSize(); i++) {
            SavedCity savedCity = savedCityModel.getElementAt(i);
            if (matches(savedCity, location)) {
                savedCityBox.setSelectedItem(savedCity);
                return;
            }
        }
    }

    /**
     * Keeps saved-city controls disabled while loading or when the list is empty.
     */
    private void updateSavedCityControls() {
        boolean hasSavedCities = savedCityModel.getSize() > 0;
        savedCityBox.setEnabled(!loading && hasSavedCities);
        loadSavedCityButton.setEnabled(!loading && hasSavedCities);
    }

    /**
     * Enables/disables controls while a weather request is running.
     */
    private void setLoading(boolean loading, String text) {
        this.loading = loading;
        searchButton.setEnabled(!loading);
        saveCityButton.setEnabled(!loading && currentWeather != null);
        cityField.setEnabled(!loading);
        updateSavedCityControls();
        statusLabel.setText(text);
    }

    /**
     * Saves the currently displayed city in the user's reusable city list.
     */
    private void saveCurrentCity() {
        if (user == null || currentWeather == null) {
            JOptionPane.showMessageDialog(this, "Kërko fillimisht një qytet.", "Nuk ka qytet të ngarkuar", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            database.saveCity(user, currentWeather.location());
            loadSavedCities();
            selectSavedCity(currentWeather.location());
            statusLabel.setText(currentWeather.location().displayName() + " u ruajt në listën tënde.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Qyteti nuk mund të ruhej.\n\n" + ex.getMessage(),
                    "Ruajtja dështoi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Compares a saved shortcut with a resolved weather location.
     */
    private boolean matches(SavedCity savedCity, LocationResult location) {
        return sameText(savedCity.city(), location.name())
                && sameText(savedCity.country(), location.country())
                && sameText(savedCity.adminArea(), location.adminArea());
    }

    /**
     * Null-safe case-insensitive text comparison.
     */
    private boolean sameText(String first, String second) {
        String cleanFirst = first == null ? "" : first.trim();
        String cleanSecond = second == null ? "" : second.trim();
        return cleanFirst.equalsIgnoreCase(cleanSecond);
    }

    /**
     * Formats weather numeric values with one decimal place.
     */
    private String format(double value) {
        return String.format(Locale.US, "%.1f", value);
    }
}
