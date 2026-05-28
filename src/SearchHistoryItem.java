public record SearchHistoryItem(
        String city,
        String country,
        double temperature,
        int weatherCode,
        String searchedAt
) {
    public String displayCity() {
        if (country == null || country.isBlank()) {
            return city;
        }
        return city + ", " + country;
    }
}
