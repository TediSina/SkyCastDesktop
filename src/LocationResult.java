/**
 * Location selected from the Open-Meteo geocoding API.
 *
 * @param name city or place name
 * @param country localized country name
 * @param adminArea administrative region, when available
 * @param latitude geographic latitude
 * @param longitude geographic longitude
 * @param timezone location timezone name
 */
public record LocationResult(
        String name,
        String country,
        String adminArea,
        double latitude,
        double longitude,
        String timezone
) {
    /**
     * Builds a compact display label from city, region, and country.
     *
     * @return human-readable location name
     */
    public String displayName() {
        StringBuilder builder = new StringBuilder(name);
        if (adminArea != null && !adminArea.isBlank() && !adminArea.equalsIgnoreCase(name)) {
            builder.append(", ").append(adminArea);
        }
        if (country != null && !country.isBlank()) {
            builder.append(", ").append(country);
        }
        return builder.toString();
    }
}
