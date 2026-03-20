package kg.kut.os.mentorhub.auth.service;

import kg.kut.os.mentorhub.auth.dto.*;
import kg.kut.os.mentorhub.auth.entity.*;
import kg.kut.os.mentorhub.auth.repository.RefreshTokenRepository;
import kg.kut.os.mentorhub.auth.repository.RoleRepository;
import kg.kut.os.mentorhub.auth.repository.UserRepository;
import kg.kut.os.mentorhub.common.exception.BadRequestException;
import kg.kut.os.mentorhub.common.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kg.kut.os.mentorhub.mentor.entity.MentorProfile;
import kg.kut.os.mentorhub.mentor.repository.MentorProfileRepository;
import kg.kut.os.mentorhub.student.entity.StudentProfile;
import kg.kut.os.mentorhub.student.repository.StudentProfileRepository;

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
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long refreshTokenExpirationDays;
    private final StudentProfileRepository studentProfileRepository;
    private final MentorProfileRepository mentorProfileRepository;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            StudentProfileRepository studentProfileRepository,
            MentorProfileRepository mentorProfileRepository,
            @Value("${app.jwt.refresh-token-expiration-days}") long refreshTokenExpirationDays
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.studentProfileRepository = studentProfileRepository;
        this.mentorProfileRepository = mentorProfileRepository;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
    }

    public AuthResponse registerStudent(RegisterStudentRequest request) {
        return register(request.getEmail(), request.getPassword(), RoleCode.ROLE_STUDENT);
    }

    public AuthResponse registerMentor(RegisterMentorRequest request) {
        return register(request.getEmail(), request.getPassword(), RoleCode.ROLE_MENTOR);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("User account is not active");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = createAndSaveRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Refresh token is invalid"));

        if (refreshToken.isRevoked()) {
            throw new UnauthorizedException("Refresh token is revoked");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token has expired");
        }

        User user = refreshToken.getUser();

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("User account is not active");
        }

        refreshToken.setRevoked(true);

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = createAndSaveRefreshToken(user);

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    public void logout(LogoutRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Refresh token is invalid"));

        refreshToken.setRevoked(true);
    }

    private AuthResponse register(String email, String rawPassword, RoleCode roleCode) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("Email is already registered");
        }

        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new BadRequestException("Role not found: " + roleCode));

        User user = new User();
        user.setEmail(email.toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(false);
        user.setRoles(Set.of(role));

        User savedUser = userRepository.save(user);

        if (roleCode == RoleCode.ROLE_STUDENT) {
            StudentProfile profile = new StudentProfile();
            profile.setUser(savedUser);
            studentProfileRepository.save(profile);
        }

        if (roleCode == RoleCode.ROLE_MENTOR) {
            MentorProfile profile = new MentorProfile();
            profile.setUser(savedUser);
            profile.setVerified(false);
            profile.setPublic(true);
            profile.setLessonFormatOnline(false);
            profile.setLessonFormatOffline(false);
            profile.setLessonFormatHybrid(false);
            mentorProfileRepository.save(profile);
        }

        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshToken = createAndSaveRefreshToken(savedUser);

        return buildAuthResponse(savedUser, accessToken, refreshToken);
    }

    private String createAndSaveRefreshToken(User user) {
        String token = UUID.randomUUID().toString() + UUID.randomUUID();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(refreshTokenExpirationDays));
        refreshToken.setRevoked(false);

        refreshTokenRepository.save(refreshToken);

        return token;
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
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
}