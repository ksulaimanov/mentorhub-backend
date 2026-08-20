package kg.kut.os.mentorhub.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.SignatureException;
import kg.kut.os.mentorhub.auth.entity.Role;
import kg.kut.os.mentorhub.auth.entity.RoleCode;
import kg.kut.os.mentorhub.auth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET =
            "c3VwZXItc2VjdXJlLXRlc3Qta2V5LXN1cGVyLXNlY3VyZS10ZXN0LWtleQ==";
    private static final String OTHER_SECRET =
            "YW5vdGhlci1zZWNyZXQta2V5LWFub3RoZXItc2VjcmV0LWtleS0xMjM0NQ==";

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 15);
        user = buildUser(1L, "student@example.com", RoleCode.ROLE_STUDENT);
    }

    private User buildUser(Long id, String email, RoleCode... roleCodes) {
        Set<Role> roles = Arrays.stream(roleCodes)
                .map(code -> {
                    Role role = new Role();
                    role.setCode(code);
                    return role;
                })
                .collect(Collectors.toSet());

        User u = new User();
        // id проставляется базой, сеттера у сущности нет
        ReflectionTestUtils.setField(u, "id", id);
        u.setEmail(email);
        u.setRoles(roles);
        return u;
    }

    @Test
    void токенСодержитEmailИдентификаторИРоли() {
        String token = jwtService.generateAccessToken(user);
        Claims claims = jwtService.extractAllClaims(token);

        assertEquals("student@example.com", claims.getSubject());
        assertEquals(1, ((Number) claims.get("userId")).intValue());

        Collection<?> roles = claims.get("roles", Collection.class);
        assertEquals(Set.of("ROLE_STUDENT"), Set.copyOf(roles));
    }

    @Test
    void extractEmailВозвращаетSubject() {
        String token = jwtService.generateAccessToken(user);

        assertEquals("student@example.com", jwtService.extractEmail(token));
    }

    @Test
    void срокЖизниСоответствуетНастройке() {
        String token = jwtService.generateAccessToken(user);
        Claims claims = jwtService.extractAllClaims(token);

        long lifetimeMinutes =
                (claims.getExpiration().getTime() - claims.getIssuedAt().getTime()) / 60_000;

        assertEquals(15, lifetimeMinutes);
    }

    /** Токен, подписанный чужим ключом, принимать нельзя. */
    @Test
    void токенСЧужойПодписьюОтвергается() {
        String foreignToken = new JwtService(OTHER_SECRET, 15).generateAccessToken(user);

        assertThrows(SignatureException.class, () -> jwtService.extractAllClaims(foreignToken));
    }

    /** Подмена полезной нагрузки ломает подпись. */
    @Test
    void подделанныйТокенОтвергается() {
        String token = jwtService.generateAccessToken(user);
        String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "cG9kZGVsa2E";

        assertThrows(Exception.class, () -> jwtService.extractAllClaims(tampered));
    }

    @Test
    void несколькоРолейПопадаютВТокен() {
        User multiRole = buildUser(7L, "both@example.com",
                RoleCode.ROLE_STUDENT, RoleCode.ROLE_MENTOR);

        Claims claims = jwtService.extractAllClaims(jwtService.generateAccessToken(multiRole));
        Collection<?> roles = claims.get("roles", Collection.class);

        assertEquals(Set.of("ROLE_STUDENT", "ROLE_MENTOR"), Set.copyOf(roles));
    }
}
