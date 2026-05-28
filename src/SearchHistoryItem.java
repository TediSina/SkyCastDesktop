/**
 * Lightweight projection for the recent-searches table.
 *
 * @param city searched city
 * @param country country returned by geocoding
 * @param temperature temperature at the time of the search
 * @param weatherCode weather condition code at the time of the search
 * @param searchedAt timestamp when the search was saved
 */
public record SearchHistoryItem(
        String city,
        String country,
        double temperature,
        int weatherCode,
        String searchedAt
) {
    /**
     * Combines city and country for table display.
     *
     * @return city alone, or city and country when country is available
     */
    public String displayCity() {
        if (country == null || country.isBlank()) {
            return city;
        }
        return city + ", " + country;
    }
}
