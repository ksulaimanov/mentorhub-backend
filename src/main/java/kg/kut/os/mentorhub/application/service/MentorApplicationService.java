package kg.kut.os.mentorhub.application.service;

import jakarta.transaction.Transactional;
import kg.kut.os.mentorhub.application.dto.*;
import kg.kut.os.mentorhub.application.entity.MentorApplication;
import kg.kut.os.mentorhub.application.entity.MentorApplicationStatus;
import kg.kut.os.mentorhub.application.repository.MentorApplicationRepository;
import kg.kut.os.mentorhub.auth.entity.Role;
import kg.kut.os.mentorhub.auth.entity.RoleCode;
import kg.kut.os.mentorhub.auth.entity.User;
import kg.kut.os.mentorhub.auth.repository.RoleRepository;
import kg.kut.os.mentorhub.auth.repository.UserRepository;
import kg.kut.os.mentorhub.common.exception.BadRequestException;
import kg.kut.os.mentorhub.common.exception.ConflictException;
import kg.kut.os.mentorhub.common.exception.NotFoundException;
import kg.kut.os.mentorhub.mentor.entity.MentorProfile;
import kg.kut.os.mentorhub.mentor.repository.MentorProfileRepository;
import kg.kut.os.mentorhub.notification.EmailNotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;

@Service
@Transactional
public class MentorApplicationService {

    private final MentorApplicationRepository mentorApplicationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final EmailNotificationService emailNotificationService;

    public MentorApplicationService(
            MentorApplicationRepository mentorApplicationRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            MentorProfileRepository mentorProfileRepository,
            EmailNotificationService emailNotificationService
    ) {
        this.mentorApplicationRepository = mentorApplicationRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.mentorProfileRepository = mentorProfileRepository;
        this.emailNotificationService = emailNotificationService;
    }


    public ApplicationStatusResponse submitApplication(Long userId, SubmitApplicationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Пользователь не найден"));

        // Check if user is already a mentor
        boolean alreadyMentor = user.getRoles().stream()
                .anyMatch(r -> r.getCode() == RoleCode.ROLE_MENTOR);
        if (alreadyMentor) {
            throw new ConflictException("Вы уже являетесь ментором");
        }

        var activeApplication = mentorApplicationRepository.findByApplicantUserAndStatusIn(
                user,
                Arrays.asList(MentorApplicationStatus.PENDING, MentorApplicationStatus.APPROVED)
        );

        if (activeApplication.isPresent()) {
            MentorApplicationStatus existingStatus = activeApplication.get().getStatus();
            String msg = existingStatus == MentorApplicationStatus.PENDING
                    ? "У вас уже есть заявка на рассмотрении"
                    : "Ваша заявка уже одобрена";
            throw new ConflictException(msg);
        }

        MentorApplication application = new MentorApplication();
        application.setApplicantUser(user);
        application.setStatus(MentorApplicationStatus.PENDING);
        application.setMotivationText(request.getMotivationText());
        application.setExperienceSummary(request.getExperienceSummary());
        application.setPortfolioUrl(request.getPortfolioUrl());

        MentorApplication savedApplication = mentorApplicationRepository.save(application);

        // Отправить email администратору (опционально, можно добавить позже)
        // emailNotificationService.sendApplicationReceived(user.getEmail(), user.getEmail());

        return mapToApplicationStatusResponse(savedApplication);
    }

    /**
     * Получить статус заявки текущего пользователя
     * @param userId ID пользователя
     * @return Статус заявки или 404, если заявки нет
     */
    public ApplicationStatusResponse getApplicationStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Пользователь не найден"));

        MentorApplication application = mentorApplicationRepository.findFirstByApplicantUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new NotFoundException("Заявка на менторство не найдена"));

        return mapToApplicationStatusResponse(application);
    }

    /**
     * Получить все заявки с фильтром по статусу (для админа)
     * @param status Статус фильтра (опционально)
     * @param pageable Пагинация
     * @return Список заявок
     */
    public Page<AdminApplicationView> listApplications(MentorApplicationStatus status, Pageable pageable) {
        Page<MentorApplication> applications;

        if (status != null) {
            applications = mentorApplicationRepository.findByStatus(status, pageable);
        } else {
            applications = mentorApplicationRepository.findAll(pageable);
        }

        return applications.map(this::mapToAdminApplicationView);
    }

    /**
     * Получить детали одной заявки (для админа)
     * @param applicationId ID заявки
     * @return Детали заявки
     */
    public AdminApplicationDetailView getApplicationDetail(Long applicationId) {
        MentorApplication application = mentorApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Заявка не найдена"));

        return mapToAdminApplicationDetailView(application);
    }

    /**
     * Одобрить заявку на менторство (ATOMICALLY):
     * 1. Обновить статус заявки на APPROVED
     * 2. Добавить роль ROLE_MENTOR пользователю
     * 3. Создать MentorProfile с verified=true, isPublic=false (или обновить существующий)
     *
     * @param applicationId ID заявки
     * @param adminUserId ID администратора, одобряющего заявку
     * @param adminComment Необязательный комментарий администратора
     * @return true если заявка была одобрена, false если уже была одобрена ранее (идемпотентность)
     */
    public boolean approveApplication(Long applicationId, Long adminUserId, String adminComment) {
        MentorApplication application = mentorApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Заявка не найдена"));

        // Если уже одобрена, это OK (идемпотентность)
        if (application.getStatus() == MentorApplicationStatus.APPROVED) {
            return false;
        }

        if (application.getStatus() != MentorApplicationStatus.PENDING) {
            throw new ConflictException(
                    "Заявка не может быть одобрена. Текущий статус: " + application.getStatus()
            );
        }

        User applicantUser = application.getApplicantUser();
        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new BadRequestException("Admin пользователь не найден"));

        // Проверка: пользователь ещё не должен иметь ROLE_MENTOR
        boolean hasRoleMentor = applicantUser.getRoles().stream()
                .anyMatch(r -> r.getCode() == RoleCode.ROLE_MENTOR);

        if (hasRoleMentor) {
            throw new ConflictException(
                    "Пользователь уже имеет роль ROLE_MENTOR"
            );
        }

        // 1. Обновить заявку
        application.setStatus(MentorApplicationStatus.APPROVED);
        application.setReviewedByUser(adminUser);
        application.setReviewedAt(LocalDateTime.now());
        application.setAdminComment(adminComment);
        mentorApplicationRepository.save(application);

        // 2. Добавить роль ROLE_MENTOR
        Role mentorRole = roleRepository.findByCode(RoleCode.ROLE_MENTOR)
                .orElseThrow(() -> new BadRequestException("Роль ROLE_MENTOR не найдена"));

        applicantUser.getRoles().add(mentorRole);
        userRepository.save(applicantUser);

        // 3. Создать MentorProfile (или обновить существующий)
        MentorProfile existingProfile = mentorProfileRepository.findByUserId(applicantUser.getId())
                .orElse(null);

        if (existingProfile != null) {
            existingProfile.setVerified(true);
            mentorProfileRepository.save(existingProfile);
        } else {
            MentorProfile mentorProfile = new MentorProfile();
            mentorProfile.setUser(applicantUser);
            mentorProfile.setVerified(true);
            mentorProfile.setPublic(false); // По умолчанию не публичный, ментор может включить позже
            mentorProfile.setLessonFormatOnline(false);
            mentorProfile.setLessonFormatOffline(false);
            mentorProfile.setLessonFormatHybrid(false);
            mentorProfileRepository.save(mentorProfile);
        }

        // 4. Отправить email ментору
        emailNotificationService.sendApplicationApproved(applicantUser.getEmail(), applicantUser.getEmail(), applicantUser.getPreferredLocale());

        return true;
    }

    /**
     * Отклонить заявку на менторство
     *
     * @param applicationId ID заявки
     * @param rejectionReason Причина отклонения
     * @param adminUserId ID администратора, отклоняющего заявку
     * @return true если заявка была отклонена, false если уже была отклонена ранее (идемпотентность)
     */
    public boolean rejectApplication(Long applicationId, String rejectionReason, Long adminUserId) {
        MentorApplication application = mentorApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Заявка не найдена"));

        // Если уже отклонена, это OK (идемпотентность)
        if (application.getStatus() == MentorApplicationStatus.REJECTED) {
            return false;
        }

        if (application.getStatus() != MentorApplicationStatus.PENDING) {
            throw new ConflictException(
                    "Заявка не может быть отклонена. Текущий статус: " + application.getStatus()
            );
        }

        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new BadRequestException("Admin пользователь не найден"));

        application.setStatus(MentorApplicationStatus.REJECTED);
        application.setRejectionReason(rejectionReason);
        application.setReviewedByUser(adminUser);
        application.setReviewedAt(LocalDateTime.now());
        mentorApplicationRepository.save(application);

        // Отправить email заявителю
        User applicantUser = application.getApplicantUser();
        emailNotificationService.sendApplicationRejected(
                applicantUser.getEmail(),
                applicantUser.getEmail(),
                rejectionReason,
                applicantUser.getPreferredLocale()
        );

        return true;
    }


    private ApplicationStatusResponse mapToApplicationStatusResponse(MentorApplication app) {
        return new ApplicationStatusResponse(
                app.getId(),
                app.getStatus(),
                app.getMotivationText(),
                app.getExperienceSummary(),
                app.getPortfolioUrl(),
                app.getRejectionReason(),
                app.getCreatedAt(),
                app.getUpdatedAt()
        );
    }

    private AdminApplicationView mapToAdminApplicationView(MentorApplication app) {
        return new AdminApplicationView(
                app.getId(),
                app.getApplicantUser().getId(),
                app.getApplicantUser().getEmail(),
                app.getApplicantUser().getEmail(),
                app.getStatus(),
                app.getCreatedAt(),
                app.getUpdatedAt(),
                app.getReviewedAt()
        );
    }

    private AdminApplicationDetailView mapToAdminApplicationDetailView(MentorApplication app) {
        AdminApplicationDetailView view = new AdminApplicationDetailView();
        view.setApplicationId(app.getId());
        view.setApplicantUserId(app.getApplicantUser().getId());
        view.setApplicantEmail(app.getApplicantUser().getEmail());
        view.setApplicantName(app.getApplicantUser().getEmail());
        view.setStatus(app.getStatus());
        view.setMotivationText(app.getMotivationText());
        view.setExperienceSummary(app.getExperienceSummary());
        view.setPortfolioUrl(app.getPortfolioUrl());
        view.setRejectionReason(app.getRejectionReason());
        view.setAdminComment(app.getAdminComment());

        if (app.getReviewedByUser() != null) {
            view.setReviewedByUserId(app.getReviewedByUser().getId());
            view.setReviewedByEmail(app.getReviewedByUser().getEmail());
        }

        view.setReviewedAt(app.getReviewedAt());
        view.setCreatedAt(app.getCreatedAt());
        view.setUpdatedAt(app.getUpdatedAt());
        return view;
    }
}

