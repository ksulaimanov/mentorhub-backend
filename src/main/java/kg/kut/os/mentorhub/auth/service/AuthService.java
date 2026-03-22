package kg.kut.os.mentorhub.auth.service;

import jakarta.transaction.Transactional;
import kg.kut.os.mentorhub.auth.dto.AuthResponse;
import kg.kut.os.mentorhub.auth.dto.LoginRequest;
import kg.kut.os.mentorhub.auth.dto.LogoutRequest;
import kg.kut.os.mentorhub.auth.dto.RefreshTokenRequest;
import kg.kut.os.mentorhub.auth.dto.RegisterMentorRequest;
import kg.kut.os.mentorhub.auth.dto.RegisterStudentRequest;
import kg.kut.os.mentorhub.auth.dto.ResendVerificationRequest;
import kg.kut.os.mentorhub.auth.dto.VerifyEmailRequest;
import kg.kut.os.mentorhub.auth.entity.EmailVerificationCode;
import kg.kut.os.mentorhub.auth.entity.RefreshToken;
import kg.kut.os.mentorhub.auth.entity.Role;
import kg.kut.os.mentorhub.auth.entity.RoleCode;
import kg.kut.os.mentorhub.auth.entity.User;
import kg.kut.os.mentorhub.auth.entity.UserStatus;
import kg.kut.os.mentorhub.auth.repository.EmailVerificationCodeRepository;
import kg.kut.os.mentorhub.auth.repository.RefreshTokenRepository;
import kg.kut.os.mentorhub.auth.repository.RoleRepository;
import kg.kut.os.mentorhub.auth.repository.UserRepository;
import kg.kut.os.mentorhub.mentor.entity.MentorProfile;
import kg.kut.os.mentorhub.mentor.repository.MentorProfileRepository;
import kg.kut.os.mentorhub.notification.EmailNotificationService;
import kg.kut.os.mentorhub.student.entity.StudentProfile;
import kg.kut.os.mentorhub.student.repository.StudentProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
            EmailNotificationService emailNotificationService,
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
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
        this.verificationCodeExpirationMinutes = verificationCodeExpirationMinutes;
        this.resendCooldownSeconds = resendCooldownSeconds;
        this.maxVerificationAttempts = maxVerificationAttempts;
    }

    public void registerStudent(RegisterStudentRequest request) {
        register(request.getEmail(), request.getPassword(), RoleCode.ROLE_STUDENT);
    }

    public void registerMentor(RegisterMentorRequest request) {
        register(request.getEmail(), request.getPassword(), RoleCode.ROLE_MENTOR);
    }

    public void verifyEmail(VerifyEmailRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пользователь с таким email не найден"));

        if (user.isEmailVerified()) {
            return;
        }

        EmailVerificationCode verificationCode = emailVerificationCodeRepository
                .findTopByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Код подтверждения не найден"));

        if (verificationCode.isUsed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Код подтверждения уже использован");
        }

        if (verificationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Срок действия кода истёк");
        }

        if (verificationCode.getAttempts() >= maxVerificationAttempts) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Превышено количество попыток ввода кода");
        }

        if (!verificationCode.getCode().equals(request.getCode())) {
            verificationCode.setAttempts(verificationCode.getAttempts() + 1);
            emailVerificationCodeRepository.save(verificationCode);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неверный код подтверждения");
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пользователь с таким email не найден"));

        if (user.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email уже подтверждён");
        }

        EmailVerificationCode latestCode = emailVerificationCodeRepository
                .findTopByUserOrderByCreatedAtDesc(user)
                .orElse(null);

        if (latestCode != null
                && latestCode.getCreatedAt().plusSeconds(resendCooldownSeconds).isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Код был отправлен недавно. Попробуйте чуть позже"
            );
        }

        issueAndSendVerificationCode(user);
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный email или пароль"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный email или пароль");
        }

        if (!user.isEmailVerified() || user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ещё не подтверждён");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Аккаунт недоступен");
        }

        RefreshToken refreshToken = createRefreshToken(user);

        return buildAuthResponse(user, refreshToken.getToken());
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Недействительный refresh token"));

        if (refreshToken.isRevoked()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token отозван");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Срок действия refresh token истёк");
        }

        User user = refreshToken.getUser();

        if (!user.isEmailVerified() || user.getStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Аккаунт недоступен");
        }

        return buildAuthResponse(user, refreshToken.getToken());
    }

    public void logout(LogoutRequest request) {
        refreshTokenRepository.findByToken(request.getRefreshToken()).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    private void register(String email, String rawPassword, RoleCode roleCode) {
        String normalizedEmail = normalizeEmail(email);

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email уже зарегистрирован");
        }

        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Роль не найдена: " + roleCode));

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setEmailVerified(false);
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
        String code = generateSixDigitCode();

        EmailVerificationCode verificationCode = new EmailVerificationCode();
        verificationCode.setUser(user);
        verificationCode.setCode(code);
        verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(verificationCodeExpirationMinutes));
        verificationCode.setUsed(false);
        verificationCode.setAttempts(0);

        emailVerificationCodeRepository.save(verificationCode);
        emailNotificationService.sendEmailVerificationCode(user.getEmail(), code);
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
}