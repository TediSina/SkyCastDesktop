public final class WeatherCodes {
    private WeatherCodes() {
    }

    public static String describe(int code) {
        return switch (code) {
            case 0 -> "Qiell i kthjellët";
            case 1 -> "Kryesisht kthjellët";
            case 2 -> "Pjesërisht me re";
            case 3 -> "I vrenjtur";
            case 45, 48 -> "Mjegull";
            case 51, 53, 55 -> "Rigë";
            case 56, 57 -> "Rigë me ngricë";
            case 61, 63, 65 -> "Shi";
            case 66, 67 -> "Shi me ngricë";
            case 71, 73, 75 -> "Reshje dëbore";
            case 77 -> "Kokrriza dëbore";
            case 80, 81, 82 -> "Rrebeshe shiu";
            case 85, 86 -> "Rrebeshe dëbore";
            case 95 -> "Stuhi me vetëtima";
            case 96, 99 -> "Stuhi me breshër";
            default -> "E panjohur";
        };
    }

    public static String icon(int code) {
        return switch (code) {
            case 0 -> "\u2600\uFE0F";
            case 1 -> "\uD83C\uDF24\uFE0F";
            case 2 -> "\u26C5";
            case 3 -> "\u2601\uFE0F";
            case 45, 48 -> "\uD83C\uDF2B\uFE0F";
            case 51, 53, 55, 56, 57 -> "\uD83C\uDF26\uFE0F";
            case 61, 63, 65, 66, 67, 80, 81, 82 -> "\uD83C\uDF27\uFE0F";
            case 71, 73, 75, 77, 85, 86 -> "\u2744\uFE0F";
            case 95, 96, 99 -> "\u26C8\uFE0F";
            default -> "\uD83C\uDF21\uFE0F";
        };
    }

    public static String describeWithIcon(int code) {
        return icon(code) + " " + describe(code);
    }
}
