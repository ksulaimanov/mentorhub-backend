package kg.kut.os.mentorhub.common.security;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Единый список эндпоинтов, доступных без аутентификации.
 * <p>
 * Раньше этот список дублировался в {@link SecurityConfig} (правила доступа) и в
 * {@link JwtAuthenticationFilter} (пропуск разбора JWT), из-за чего копии успели разойтись.
 * Обе стороны теперь читают его отсюда.
 */
public final class PublicEndpoints {

    /** Пути, совпадающие целиком. */
    private static final String[] EXACT_PATHS = {
            "/api/auth/login",
            "/api/auth/register/student",
            "/api/auth/register/mentor",
            "/api/auth/verify-email",
            "/api/auth/resend-verification",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/auth/refresh"
    };

    /** Префиксы: сам путь и всё, что под ним. */
    private static final String[] PATH_PREFIXES = {
            "/api/public",
            "/uploads",
            "/swagger-ui",
            "/api-docs",
            "/v3/api-docs",
            "/actuator/health",
            "/ws-stomp"
    };

    private PublicEndpoints() {
    }

    /** Паттерны для {@code authorizeHttpRequests(...).permitAll()}. */
    public static String[] securityPatterns() {
        return Stream.concat(
                Arrays.stream(EXACT_PATHS),
                Arrays.stream(PATH_PREFIXES).map(prefix -> prefix + "/**")
        ).toArray(String[]::new);
    }

    /** Совпадает ли путь запроса с одним из публичных эндпоинтов. */
    public static boolean matches(String path) {
        for (String exact : EXACT_PATHS) {
            if (exact.equals(path)) {
                return true;
            }
        }
        for (String prefix : PATH_PREFIXES) {
            if (path.equals(prefix) || path.startsWith(prefix + "/")) {
                return true;
            }
        }
        return false;
    }
}
