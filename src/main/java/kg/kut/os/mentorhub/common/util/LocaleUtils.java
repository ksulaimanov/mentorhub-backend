package kg.kut.os.mentorhub.common.util;

/**
 * Locale normalization for MentorHub.
 * Supported locales: "ky" (Kyrgyz, default), "ru" (Russian).
 */
public final class LocaleUtils {

    public static final String KYRGYZ = "ky";
    public static final String RUSSIAN = "ru";
    public static final String DEFAULT_LOCALE = KYRGYZ;

    private LocaleUtils() {
    }

    /**
     * Normalizes raw locale input to one of the supported values.
     * <ul>
     *   <li>"ky", "KY", "ky-KG", "ky_KG" → "ky"</li>
     *   <li>"ru", "RU", "ru-RU", "ru_RU" → "ru"</li>
     *   <li>null, blank, or anything else → "ky" (default)</li>
     * </ul>
     */
    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEFAULT_LOCALE;
        }

        String lower = raw.trim().toLowerCase();

        if (lower.equals(RUSSIAN) || lower.startsWith("ru-") || lower.startsWith("ru_")) {
            return RUSSIAN;
        }

        if (lower.equals(KYRGYZ) || lower.startsWith("ky-") || lower.startsWith("ky_")) {
            return KYRGYZ;
        }

        return DEFAULT_LOCALE;
    }
}

