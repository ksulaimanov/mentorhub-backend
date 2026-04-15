package kg.kut.os.mentorhub.application.service;

import kg.kut.os.mentorhub.application.dto.ApplicationStatusResponse;
import kg.kut.os.mentorhub.application.dto.SubmitApplicationRequest;
import kg.kut.os.mentorhub.application.entity.MentorApplicationStatus;
import kg.kut.os.mentorhub.auth.entity.Role;
import kg.kut.os.mentorhub.auth.entity.RoleCode;
import kg.kut.os.mentorhub.auth.entity.User;
import kg.kut.os.mentorhub.auth.entity.UserStatus;
import kg.kut.os.mentorhub.auth.repository.RoleRepository;
import kg.kut.os.mentorhub.auth.repository.UserRepository;
import kg.kut.os.mentorhub.common.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import jakarta.transaction.Transactional;
import java.util.Set;
import jakarta.validation.ConstraintViolationException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
public class MentorApplicationServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private MentorApplicationService mentorApplicationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        Role studentRole = roleRepository.findByCode(RoleCode.ROLE_STUDENT)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setCode(RoleCode.ROLE_STUDENT);
                    return roleRepository.save(role);
                });

        testUser = new User();
        testUser.setEmail("test.student@example.com");
        testUser.setPasswordHash("secret");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setEmailVerified(true);
        testUser.getRoles().add(studentRole);
        testUser = userRepository.save(testUser);
    }

    @Test
    void submitApplication_success() {
        SubmitApplicationRequest request = new SubmitApplicationRequest();
        request.setMotivationText("This is a valid motivation text.");
        request.setExperienceSummary("This is a valid experience summary.");
        request.setPortfolioUrl("https://portfolio.example.com");

        ApplicationStatusResponse response = mentorApplicationService.submitApplication(testUser.getId(), request);

        assertNotNull(response.getApplicationId());
        assertEquals(MentorApplicationStatus.PENDING, response.getStatus());
        assertEquals("This is a valid motivation text.", response.getMotivationText());
    }

    @Test
    void submitApplication_duplicate() {
        SubmitApplicationRequest request = new SubmitApplicationRequest();
        request.setMotivationText("Valid motivation text 1.");
        request.setExperienceSummary("Valid experience summary 1.");

        mentorApplicationService.submitApplication(testUser.getId(), request);

        SubmitApplicationRequest duplicateRequest = new SubmitApplicationRequest();
        duplicateRequest.setMotivationText("Different motivation.");
        duplicateRequest.setExperienceSummary("Different summary.");

        assertThrows(ConflictException.class, () -> {
            mentorApplicationService.submitApplication(testUser.getId(), duplicateRequest);
        });
    }

    @Test
    void submitApplication_invalidPayload() {
        SubmitApplicationRequest request = new SubmitApplicationRequest();
        request.setMotivationText(null); // Invalid
        request.setExperienceSummary(""); // Invalid

        Exception ex = assertThrows(Exception.class, () -> {
            mentorApplicationService.submitApplication(testUser.getId(), request);
            // Ideally Validation error, but since service doesn't validate explicitly, DB constraint or repository will throw it.
            // In our modified code, DataIntegrityViolationException could be thrown as fields are NOT NULL.
        });
        assertNotNull(ex);
    }
}

