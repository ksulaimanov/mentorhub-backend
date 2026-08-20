package kg.kut.os.mentorhub.auth.controller;

import jakarta.validation.Valid;
import kg.kut.os.mentorhub.auth.dto.*;
import kg.kut.os.mentorhub.auth.entity.User;
import kg.kut.os.mentorhub.auth.service.AuthService;
import kg.kut.os.mentorhub.auth.service.UserService;
import kg.kut.os.mentorhub.auth.util.CookieUtils;
import kg.kut.os.mentorhub.common.exception.AuthException;
import kg.kut.os.mentorhub.common.dto.MessageResponse;
import kg.kut.os.mentorhub.common.security.CurrentUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String ACCESS_TOKEN_COOKIE = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private final AuthService authService;
    private final UserService userService;
    private final CookieUtils cookieUtils;

    /** Cookie живут ровно столько же, сколько сами токены — иначе сессия рвётся раньше времени. */
    private final Duration accessCookieTtl;
    private final Duration refreshCookieTtl;

    public AuthController(
            AuthService authService,
            UserService userService,
            CookieUtils cookieUtils,
            @Value("${app.jwt.access-token-expiration-minutes}") long accessTokenExpirationMinutes,
            @Value("${app.jwt.refresh-token-expiration-days}") long refreshTokenExpirationDays
    ) {
        this.authService = authService;
        this.userService = userService;
        this.cookieUtils = cookieUtils;
        this.accessCookieTtl = Duration.ofMinutes(accessTokenExpirationMinutes);
        this.refreshCookieTtl = Duration.ofDays(refreshTokenExpirationDays);
    }

    @PostMapping("/register/student")
    public ResponseEntity<MessageResponse> registerStudent(@Valid @RequestBody RegisterStudentRequest request) {
        authService.registerStudent(request);
        return ResponseEntity.ok(new MessageResponse("Код подтверждения отправлен на email"));
    }

    @PostMapping("/register/mentor")
    public ResponseEntity<MessageResponse> registerMentor() {
        throw new ResponseStatusException(
                HttpStatus.GONE,
                "Регистрация менторов закрыта. Пожалуйста, зарегистрируйтесь как студент и подайте заявку на менторство через /api/student/mentor-application"
        );
    }

    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(new MessageResponse("Email успешно подтверждён"));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerification(request);
        return ResponseEntity.ok(new MessageResponse("Новый код подтверждения отправлен"));
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
        return respondWithTokenCookies(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(@CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshTokenCookie) {
        if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
            throw AuthException.invalidRefreshToken();
        }
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(refreshTokenCookie);
        return respondWithTokenCookies(authService.refresh(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> getCurrentUser(@CurrentUser User user) {
        return ResponseEntity.ok(userService.getUserMeInfo(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshTokenCookie) {
        if (refreshTokenCookie != null) {
            LogoutRequest request = new LogoutRequest();
            request.setRefreshToken(refreshTokenCookie);
            authService.logout(request);
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieUtils.cleanCookie(ACCESS_TOKEN_COOKIE).toString())
                .header(HttpHeaders.SET_COOKIE, cookieUtils.cleanCookie(REFRESH_TOKEN_COOKIE).toString())
                .build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(new MessageResponse("Если email зарегистрирован, код для сброса отправлен"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new MessageResponse("Пароль успешно обновлён"));
    }

    /** Отдаёт пустой 200 с обновлённой парой cookie — общий хвост для login и refresh. */
    private ResponseEntity<Void> respondWithTokenCookies(AuthResponse tokens) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        cookieUtils.createTokenCookie(ACCESS_TOKEN_COOKIE, tokens.getAccessToken(), accessCookieTtl).toString())
                .header(HttpHeaders.SET_COOKIE,
                        cookieUtils.createTokenCookie(REFRESH_TOKEN_COOKIE, tokens.getRefreshToken(), refreshCookieTtl).toString())
                .build();
    }
}