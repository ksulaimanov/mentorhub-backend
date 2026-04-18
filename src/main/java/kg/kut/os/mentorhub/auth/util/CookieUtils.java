package kg.kut.os.mentorhub.auth.util;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieUtils {

    public ResponseCookie createTokenCookie(String name, String token, long durationInSeconds) {
        return ResponseCookie.from(name, token)
                .httpOnly(true)
                .secure(true) // Set to false in local dev if not using HTTPS
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofSeconds(durationInSeconds))
                .build();
    }

    public ResponseCookie cleanCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();
    }
}
