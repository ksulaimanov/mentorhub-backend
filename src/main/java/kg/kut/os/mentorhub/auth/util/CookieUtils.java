package kg.kut.os.mentorhub.auth.util;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieUtils {

    /** Cookie с токеном: живёт столько, сколько передал вызывающий. */
    public ResponseCookie createTokenCookie(String name, String token, Duration ttl) {
        return baseCookie(name, token).maxAge(ttl).build();
    }

    /** Стирающая cookie: то же имя и атрибуты, пустое значение и maxAge=0. */
    public ResponseCookie cleanCookie(String name) {
        return baseCookie(name, "").maxAge(Duration.ZERO).build();
    }

    /**
     * Атрибуты обязаны совпадать у выдающей и стирающей cookie, иначе браузер
     * не сопоставит их и logout не удалит токен.
     */
    private ResponseCookie.ResponseCookieBuilder baseCookie(String name, String value) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true) // Set to false in local dev if not using HTTPS
                .sameSite("None")
                .path("/");
    }
}
