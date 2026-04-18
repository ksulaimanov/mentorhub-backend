package kg.kut.os.mentorhub.auth.service;

import jakarta.transaction.Transactional;
import kg.kut.os.mentorhub.auth.dto.*;
import kg.kut.os.mentorhub.auth.entity.*;
import kg.kut.os.mentorhub.auth.repository.*;
import kg.kut.os.mentorhub.common.exception.AuthException;
import kg.kut.os.mentorhub.common.util.LocaleUtils;
import kg.kut.os.mentorhub.mentor.entity.MentorProfile;
import kg.kut.os.mentorhub.mentor.repository.MentorProfileRepository;
import kg.kut.os.mentorhub.notification.EmailNotificationService;
import kg.kut.os.mentorhub.student.entity.StudentProfile;
import kg.kut.os.mentorhub.student.repository.StudentProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailNotificationService emailNotificationService;
    private final PasswordResetCodeRepository passwordResetCodeRepository;

    private final long refreshTokenExpirationDays;
    private final long verificationCodeExpirationMinutes;
    private final long resendCooldownSeconds;
    private final int maxVerificationAttempts;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            RefreshTokenRepository refreshTokenRepository,
            EmailVerificationCodeRepository emailVerificationCodeRepository,
            StudentProfileRepository studentProfileRepository,
            MentorProfileRepository mentorProfileRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            EmailNotificationService emailNotificationService, PasswordResetCodeRepository passwordResetCodeRepository,
            @Value("${app.jwt.refresh-token-expiration-days}") long refreshTokenExpirationDays,
            @Value("${app.verification.code-expiration-minutes}") long verificationCodeExpirationMinutes,
            @Value("${app.verification.resend-cooldown-seconds}") long resendCooldownSeconds,
            @Value("${app.verification.max-attempts}") int maxVerificationAttempts
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.emailVerificationCodeRepository = emailVerificationCodeRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.mentorProfileRepository = mentorProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailNotificationService = emailNotificationService;
        this.passwordResetCodeRepository = passwordResetCodeRepository;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
        this.verificationCodeExpirationMinutes = verificationCodeExpirationMinutes;
        this.resendCooldownSeconds = resendCooldownSeconds;
        this.maxVerificationAttempts = maxVerificationAttempts;
    }

    public void registerStudent(RegisterStudentRequest request) {
        String locale = LocaleUtils.normalize(request.getPreferredLocale());
        register(request.getEmail(), request.getPassword(), RoleCode.ROLE_STUDENT, locale);
    }

    // registerMentor is no longer used, removed.

    public void verifyEmail(VerifyEmailRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(AuthException::userNotFound);

        if (user.isEmailVerified()) {
            return;
        }

        EmailVerificationCode verificationCode = emailVerificationCodeRepository
                .findTopByUserOrderByCreatedAtDesc(user)
                .orElseThrow(AuthException::invalidCode);

        if (verificationCode.isUsed()) {
            throw AuthException.codeAlreadyUsed();
        }

        if (verificationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw AuthException.codeExpired();
        }

        if (verificationCode.getAttempts() >= maxVerificationAttempts) {
            throw AuthException.tooManyAttempts();
        }

        if (!verificationCode.getCode().equals(request.getCode())) {
            verificationCode.setAttempts(verificationCode.getAttempts() + 1);
            emailVerificationCodeRepository.save(verificationCode);
            throw AuthException.invalidCode();
        }

        verificationCode.setUsed(true);
        emailVerificationCodeRepository.save(verificationCode);

        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    public void resendVerification(ResendVerificationRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(AuthException::userNotFound);

        if (user.isEmailVerified()) {
            throw AuthException.emailAlreadyVerified();
        }

        EmailVerificationCode latestCode = emailVerificationCodeRepository
                .findTopByUserOrderByCreatedAtDesc(user)
                .orElse(null);

        if (latestCode != null
                && latestCode.getCreatedAt().plusSeconds(resendCooldownSeconds).isAfter(LocalDateTime.now())) {
            throw AuthException.tooManyRequests();
        }

        issueAndSendVerificationCode(user);
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(AuthException::invalidCredentials);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw AuthException.invalidCredentials();
        }

        if (!user.isEmailVerified() || user.getStatus() == UserStatus.PENDING_EMAIL_VERIFICATION) {
            throw AuthException.emailNotVerified();
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw AuthException.accountDisabled();
        }

        RefreshToken refreshToken = createRefreshToken(user);

        user.setLastActiveAt(LocalDateTime.now());
        userRepository.save(user);

        return buildAuthResponse(user, refreshToken.getToken());
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(AuthException::invalidRefreshToken);

        if (refreshToken.isRevoked()) {
            throw AuthException.refreshTokenRevoked();
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw AuthException.refreshTokenExpired();
        }

        User user = refreshToken.getUser();

        if (!user.isEmailVerified() || user.getStatus() != UserStatus.ACTIVE) {
            throw AuthException.accountDisabled();
        }

        // Refresh Token Rotation (RTR)
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        RefreshToken newRefreshToken = createRefreshToken(user);

        user.setLastActiveAt(LocalDateTime.now());
        userRepository.save(user);

        return buildAuthResponse(user, newRefreshToken.getToken());
    }

    public void logout(LogoutRequest request) {
        refreshTokenRepository.findByToken(request.getRefreshToken()).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    private void register(String email, String rawPassword, RoleCode roleCode, String locale) {
        String normalizedEmail = normalizeEmail(email);

        User existingUser = userRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        if (existingUser != null) {
            if (existingUser.getStatus() == UserStatus.PENDING_EMAIL_VERIFICATION
                    && existingUser.getCreatedAt().plusMinutes(verificationCodeExpirationMinutes).isBefore(LocalDateTime.now())) {
                // Stale unverified registration — treat email as new
                userRepository.delete(existingUser);
                userRepository.flush();
            } else {
                throw AuthException.emailAlreadyRegistered();
            }
        }

        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new RuntimeException("Роль не найдена: " + roleCode));

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setStatus(UserStatus.PENDING_EMAIL_VERIFICATION);
        user.setEmailVerified(false);
        user.setPreferredLocale(locale);
        user.setRoles(Set.of(role));

        User savedUser = userRepository.save(user);

        if (roleCode == RoleCode.ROLE_STUDENT) {
            StudentProfile studentProfile = new StudentProfile();
            studentProfile.setUser(savedUser);
            studentProfileRepository.save(studentProfile);
        }

        if (roleCode == RoleCode.ROLE_MENTOR) {
            MentorProfile mentorProfile = new MentorProfile();
            mentorProfile.setUser(savedUser);
            mentorProfile.setVerified(false);
            mentorProfile.setPublic(true);
            mentorProfile.setLessonFormatOnline(false);
            mentorProfile.setLessonFormatOffline(false);
            mentorProfile.setLessonFormatHybrid(false);
            mentorProfileRepository.save(mentorProfile);
        }

        issueAndSendVerificationCode(savedUser);
    }

    private void issueAndSendVerificationCode(User user) {
        // Invalidate any previous unused codes
        emailVerificationCodeRepository.findTopByUserOrderByCreatedAtDesc(user)
                .filter(old -> !old.isUsed())
                .ifPresent(old -> {
                    old.setUsed(true);
                    emailVerificationCodeRepository.save(old);
                });

        String code = generateSixDigitCode();

        EmailVerificationCode verificationCode = new EmailVerificationCode();
        verificationCode.setUser(user);
        verificationCode.setCode(code);
        verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(verificationCodeExpirationMinutes));
        verificationCode.setUsed(false);
        verificationCode.setAttempts(0);

        emailVerificationCodeRepository.save(verificationCode);
        emailNotificationService.sendEmailVerificationCode(user.getEmail(), code, user.getPreferredLocale());
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(refreshTokenExpirationDays));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    private AuthResponse buildAuthResponse(User user, String refreshToken) {
        String accessToken = jwtService.generateAccessToken(user);

        Set<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getCode().name())
                .collect(Collectors.toSet());

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                roles,
                accessToken,
                refreshToken
        );
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String generateSixDigitCode() {
        int value = 100000 + (int) (Math.random() * 900000);
        return String.valueOf(value);
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);

        if (user == null) {
            return;
        }

        if (!user.isEmailVerified() || user.getStatus() != UserStatus.ACTIVE) {
            return;
        }

        PasswordResetCode latestCode = passwordResetCodeRepository
                .findTopByUserOrderByCreatedAtDesc(user)
                .orElse(null);

        if (latestCode != null &&
                latestCode.getCreatedAt().plusSeconds(resendCooldownSeconds).isAfter(LocalDateTime.now())) {
            throw AuthException.tooManyRequests();
        }

        issueAndSendPasswordResetCode(user);
    }

    public void resetPassword(ResetPasswordRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(AuthException::userNotFound);

        PasswordResetCode resetCode = passwordResetCodeRepository
                .findTopByUserOrderByCreatedAtDesc(user)
                .orElseThrow(AuthException::invalidResetCode);

        if (resetCode.isUsed()) {
            throw AuthException.codeAlreadyUsed();
        }

        if (resetCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw AuthException.codeExpired();
        }

        if (resetCode.getAttempts() >= maxVerificationAttempts) {
            throw AuthException.tooManyAttempts();
        }

        if (!resetCode.getCode().equals(request.getCode())) {
            resetCode.setAttempts(resetCode.getAttempts() + 1);
            passwordResetCodeRepository.save(resetCode);
            throw AuthException.invalidResetCode();
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetCode.setUsed(true);
        passwordResetCodeRepository.save(resetCode);

        revokeAllRefreshTokens(user);
    }

    private void issueAndSendPasswordResetCode(User user) {
        // Invalidate any previous unused codes
        passwordResetCodeRepository.findTopByUserOrderByCreatedAtDesc(user)
                .filter(old -> !old.isUsed())
                .ifPresent(old -> {
                    old.setUsed(true);
                    passwordResetCodeRepository.save(old);
                });

        String code = generateSixDigitCode();

        PasswordResetCode resetCode = new PasswordResetCode();
        resetCode.setUser(user);
        resetCode.setCode(code);
        resetCode.setExpiresAt(LocalDateTime.now().plusMinutes(verificationCodeExpirationMinutes));
        resetCode.setUsed(false);
        resetCode.setAttempts(0);
        passwordResetCodeRepository.save(resetCode);

        String userName = extractUserName(user);
        emailNotificationService.sendPasswordResetCode(user.getEmail(), userName, code, user.getPreferredLocale());
    }

    private String extractUserName(User user) {
        return studentProfileRepository.findByUserId(user.getId())
                .map(kg.kut.os.mentorhub.student.entity.StudentProfile::getFirstName)
                .orElseGet(() -> mentorProfileRepository.findByUserId(user.getId())
                        .map(kg.kut.os.mentorhub.mentor.entity.MentorProfile::getFirstName)
                        .orElse(null));
    }

    private void revokeAllRefreshTokens(User user) {
        refreshTokenRepository.findAllByUserId(user.getId())
                .forEach(token -> token.setRevoked(true));
    }
}