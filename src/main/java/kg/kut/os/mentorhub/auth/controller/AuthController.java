package kg.kut.os.mentorhub.auth.controller;

import jakarta.validation.Valid;
import kg.kut.os.mentorhub.auth.dto.LoginRequest;
import kg.kut.os.mentorhub.auth.dto.LogoutRequest;
import kg.kut.os.mentorhub.auth.dto.RefreshTokenRequest;
import kg.kut.os.mentorhub.auth.dto.RegisterMentorRequest;
import kg.kut.os.mentorhub.auth.dto.RegisterStudentRequest;
import kg.kut.os.mentorhub.auth.dto.ResendVerificationRequest;
import kg.kut.os.mentorhub.auth.dto.VerifyEmailRequest;
import kg.kut.os.mentorhub.auth.dto.AuthResponse;
import kg.kut.os.mentorhub.auth.service.AuthService;
import kg.kut.os.mentorhub.common.dto.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/student")
    public ResponseEntity<MessageResponse> registerStudent(@Valid @RequestBody RegisterStudentRequest request) {
        authService.registerStudent(request);
        return ResponseEntity.ok(new MessageResponse("Код подтверждения отправлен на email"));
    }

    @PostMapping("/register/mentor")
    public ResponseEntity<MessageResponse> registerMentor(@Valid @RequestBody RegisterMentorRequest request) {
        authService.registerMentor(request);
        return ResponseEntity.ok(new MessageResponse("Код подтверждения отправлен на email"));
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
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }
}