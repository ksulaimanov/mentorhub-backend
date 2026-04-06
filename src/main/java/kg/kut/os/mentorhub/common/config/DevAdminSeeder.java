package kg.kut.os.mentorhub.common.config;

import kg.kut.os.mentorhub.auth.entity.Role;
import kg.kut.os.mentorhub.auth.entity.RoleCode;
import kg.kut.os.mentorhub.auth.entity.User;
import kg.kut.os.mentorhub.auth.entity.UserStatus;
import kg.kut.os.mentorhub.auth.repository.RoleRepository;
import kg.kut.os.mentorhub.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Seeds a local admin account on startup.
 * <p>
 * Only active under the "dev" Spring profile — never runs in production.
 * Idempotent: if the admin user already exists, it is skipped.
 * <p>
 * Configure credentials in application-dev.yml:
 * <pre>
 *   app.admin.email=admin@mentorhub.local
 *   app.admin.password=Admin123!
 * </pre>
 */
@Component
@Profile("dev")
public class DevAdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevAdminSeeder.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@mentorhub.local}")
    private String adminEmail;

    @Value("${app.admin.password:Admin123!}")
    private String adminPassword;

    public DevAdminSeeder(UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByEmailIgnoreCase(adminEmail)) {
            log.info("[DevAdminSeeder] Admin account '{}' already exists — skipped", adminEmail);
            return;
        }

        Role adminRole = roleRepository.findByCode(RoleCode.ROLE_ADMIN)
                .orElseThrow(() -> new IllegalStateException(
                        "ROLE_ADMIN not found in DB. Check V1__init_auth_schema.sql migration."));

        User admin = new User();
        admin.setEmail(adminEmail.trim().toLowerCase());
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setStatus(UserStatus.ACTIVE);
        admin.setEmailVerified(true);
        admin.setPreferredLocale("ky");
        admin.setRoles(Set.of(adminRole));

        userRepository.save(admin);

        log.info("[DevAdminSeeder] ✅ Admin account created: {}", adminEmail);
    }
}

