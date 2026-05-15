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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@mentorhub.kg}")
    private String adminEmail;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    public AdminUserInitializer(UserRepository userRepository,
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
            log.info("[AdminUserInitializer] Admin account '{}' already exists — skipped", adminEmail);
            return;
        }

        Role adminRole = roleRepository.findByCode(RoleCode.ROLE_ADMIN)
                .orElseThrow(() -> new IllegalStateException(
                        "ROLE_ADMIN not found in DB. Check migrations."));

        User admin = new User();
        admin.setEmail(adminEmail.trim().toLowerCase());
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        // Status renamed to PENDING_EMAIL_VERIFICATION or ACTIVE in newer migration, let's use ACTIVE since it's admin
        admin.setStatus(UserStatus.ACTIVE);
        admin.setEmailVerified(true);
        admin.setPreferredLocale("ky");
        admin.setRoles(Set.of(adminRole));

        userRepository.save(admin);

        log.info("[AdminUserInitializer] ✔ Admin account created: {}", adminEmail);
    }
}

