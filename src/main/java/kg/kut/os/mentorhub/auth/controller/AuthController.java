package kg.kut.os.mentorhub.auth.controller;

import jakarta.validation.Valid;
import kg.kut.os.mentorhub.auth.dto.*;
import kg.kut.os.mentorhub.auth.entity.User;
import kg.kut.os.mentorhub.auth.service.AuthService;
import kg.kut.os.mentorhub.auth.service.UserService;
import kg.kut.os.mentorhub.auth.util.CookieUtils;
import kg.kut.os.mentorhub.common.dto.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import kg.kut.os.mentorhub.common.security.CurrentUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final UserService userService;
    private final CookieUtils cookieUtils;

    public AuthController(AuthService authService, UserService userService, CookieUtils cookieUtils) {
        this.authService = authService;
        this.userService = userService;
        this.cookieUtils = cookieUtils;
    }

    @PostMapping("/register/student")
    public ResponseEntity<MessageResponse> registerStudent(@Valid @RequestBody RegisterStudentRequest request) {
        authService.registerStudent(request);
        return ResponseEntity.ok(new MessageResponse("Код подтверждения отправлен на email"));
    }

    @PostMapping("/register/mentor")
    public ResponseEntity<?> registerMentor(@Valid @RequestBody RegisterMentorRequest request) {
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
        AuthResponse tokens = authService.login(request);

        var accessCookie = cookieUtils.createTokenCookie("accessToken", tokens.getAccessToken(), 3600); // 1 hr
        var refreshCookie = cookieUtils.createTokenCookie("refreshToken", tokens.getRefreshToken(), 604800); // 7 days

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(@CookieValue(name = "refreshToken", required = false) String refreshTokenCookie) {
        if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
            throw AuthException.invalidRefreshToken();
        }
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(refreshTokenCookie);
        AuthResponse tokens = authService.refresh(request);

        var accessCookie = cookieUtils.createTokenCookie("accessToken", tokens.getAccessToken(), 3600); // 1 hr
        var refreshCookie = cookieUtils.createTokenCookie("refreshToken", tokens.getRefreshToken(), 604800); // 7 days

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> getCurrentUser(@CurrentUser User user) {
        log.info("AUTH_ME_RESOLVED_USER: id={}, email={}, roles={}", user.getId(), user.getEmail(), user.getRoles());
        UserMeResponse userDto = userService.getUserMeInfo(user);
        log.info("FINAL_USER_DTO: {}", userDto);
        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "refreshToken", required = false) String refreshTokenCookie) {
        if (refreshTokenCookie != null) {
            LogoutRequest request = new LogoutRequest();
            request.setRefreshToken(refreshTokenCookie);
            authService.logout(request);
        }
        var accessCookie = cookieUtils.cleanCookie("accessToken");
        var refreshCookie = cookieUtils.cleanCookie("refreshToken");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
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
}