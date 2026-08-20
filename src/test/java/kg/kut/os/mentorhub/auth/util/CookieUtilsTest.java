package kg.kut.os.mentorhub.auth.util;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class CookieUtilsTest {

    private final CookieUtils cookieUtils = new CookieUtils();

    @Test
    void токенСохраняетсяСЗащитнымиАтрибутами() {
        ResponseCookie cookie = cookieUtils.createTokenCookie("accessToken", "jwt-value", Duration.ofMinutes(15));

        assertEquals("accessToken", cookie.getName());
        assertEquals("jwt-value", cookie.getValue());
        assertTrue(cookie.isHttpOnly(), "cookie с токеном не должна читаться из JS");
        assertTrue(cookie.isSecure());
        assertEquals("None", cookie.getSameSite());
        assertEquals("/", cookie.getPath());
        assertEquals(Duration.ofMinutes(15), cookie.getMaxAge());
    }

    @Test
    void срокЖизниБерётсяИзАргумента() {
        ResponseCookie cookie = cookieUtils.createTokenCookie("refreshToken", "uuid", Duration.ofDays(30));

        assertEquals(Duration.ofDays(30), cookie.getMaxAge());
    }

    @Test
    void стирающаяCookieПустаИСрокомНоль() {
        ResponseCookie cookie = cookieUtils.cleanCookie("accessToken");

        assertEquals("", cookie.getValue());
        assertEquals(Duration.ZERO, cookie.getMaxAge());
    }

    /**
     * Браузер удалит cookie только если атрибуты стирающей совпадают с выданной.
     * Разойдутся — logout оставит рабочий токен в браузере.
     */
    @Test
    void атрибутыВыдающейИСтирающейCookieСовпадают() {
        ResponseCookie issued = cookieUtils.createTokenCookie("refreshToken", "uuid", Duration.ofDays(30));
        ResponseCookie cleared = cookieUtils.cleanCookie("refreshToken");

        assertEquals(issued.getName(), cleared.getName());
        assertEquals(issued.getPath(), cleared.getPath());
        assertEquals(issued.getDomain(), cleared.getDomain());
        assertEquals(issued.isHttpOnly(), cleared.isHttpOnly());
        assertEquals(issued.isSecure(), cleared.isSecure());
        assertEquals(issued.getSameSite(), cleared.getSameSite());
    }
}
