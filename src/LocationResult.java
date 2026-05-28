public record LocationResult(
        String name,
        String country,
        String adminArea,
        double latitude,
        double longitude,
        String timezone
) {
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
