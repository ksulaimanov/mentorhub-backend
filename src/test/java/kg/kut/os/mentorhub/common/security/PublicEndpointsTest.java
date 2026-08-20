package kg.kut.os.mentorhub.common.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PublicEndpoints — единственный источник правды о том, какие пути открыты без токена.
 * Его читают и SecurityConfig, и JwtAuthenticationFilter, поэтому лишний путь здесь
 * молча открывает эндпоинт наружу.
 */
class PublicEndpointsTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/auth/login",
            "/api/auth/register/student",
            "/api/auth/verify-email",
            "/api/auth/refresh",
            "/api/public/mentors",
            "/api/public/mentors/42/reviews",
            "/uploads/avatar.png",
            "/swagger-ui/index.html",
            "/api-docs",
            "/v3/api-docs/swagger-config",
            "/actuator/health",
            "/actuator/health/readiness",
            "/ws-stomp/info"
    })
    void публичныеПутиОткрыты(String path) {
        assertTrue(PublicEndpoints.matches(path), "должен быть публичным: " + path);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/auth/me",
            "/api/auth/logout",
            "/api/users/me",
            "/api/student/profile",
            "/api/mentor/bookings",
            "/api/admin/users",
            "/api/notifications",
            "/actuator/metrics",
            "/actuator/env"
    })
    void защищённыеПутиНеОткрыты(String path) {
        assertFalse(PublicEndpoints.matches(path), "не должен быть публичным: " + path);
    }

    /**
     * Точные пути обязаны совпадать целиком: иначе /api/auth/login-as-admin
     * унаследовал бы публичность от /api/auth/login.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "/api/auth/login-as-admin",
            "/api/auth/loginx",
            "/api/auth/refresh-all"
    })
    void префиксТочногоПутиНеДаётДоступа(String path) {
        assertFalse(PublicEndpoints.matches(path), "не должен быть публичным: " + path);
    }

    /**
     * Префикс срабатывает только на границе сегмента, чтобы /uploadsecret
     * не подхватился правилом для /uploads.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "/uploadsecret",
            "/api/publication",
            "/ws-stomping"
    })
    void похожийНаПрефиксПутьНеОткрыт(String path) {
        assertFalse(PublicEndpoints.matches(path), "не должен быть публичным: " + path);
    }

    @Test
    void паттерныДляSecurityConfigПокрываютТеЖеПути() {
        List<String> patterns = Arrays.asList(PublicEndpoints.securityPatterns());

        assertTrue(patterns.contains("/api/auth/login"));
        assertTrue(patterns.contains("/api/public/**"));
        assertTrue(patterns.contains("/actuator/health/**"));
        assertFalse(patterns.contains("/api/admin/**"));
    }

    @Test
    void каждыйПаттернСоответствуетРеальномуПубличномуПути() {
        for (String pattern : PublicEndpoints.securityPatterns()) {
            String samplePath = pattern.endsWith("/**")
                    ? pattern.substring(0, pattern.length() - 3) + "/что-нибудь"
                    : pattern;
            assertTrue(PublicEndpoints.matches(samplePath),
                    "паттерн " + pattern + " не согласован с matches()");
        }
    }
}
