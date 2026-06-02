/**
 * City saved by a user for quick dashboard loading.
 *
 * @param id database identifier
 * @param city city or place name
 * @param country country returned by geocoding, when available
 * @param adminArea administrative region returned by geocoding, when available
 * @param latitude saved latitude, when the city has been resolved
 * @param longitude saved longitude, when the city has been resolved
 * @param timezone location timezone, when available
 * @param savedAt timestamp when the city was saved
 */
public record SavedCity(
        int id,
        String city,
        String country,
        String adminArea,
        Double latitude,
        Double longitude,
        String timezone,
        String savedAt
) {
    /**
     * @return compact city label for combo-box display
     */
    public String displayName() {
        StringBuilder builder = new StringBuilder(city);
        if (adminArea != null && !adminArea.isBlank() && !adminArea.equalsIgnoreCase(city)) {
            builder.append(", ").append(adminArea);
        }
        if (country != null && !country.isBlank()) {
            builder.append(", ").append(country);
        }
        return builder.toString();
    }

    /**
     * @return true when this city can be loaded without another geocoding request
     */
    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }

    /**
     * Converts a fully resolved saved city back to a weather-service location.
     *
     * @return location result, or null when the city was saved by name only
     */
    public LocationResult toLocationResult() {
        if (!hasCoordinates()) {
            return null;
        }
        return new LocationResult(city, country, adminArea, latitude, longitude, timezone);
    }

    @Override
    public String toString() {
        return displayName();
    }
}
