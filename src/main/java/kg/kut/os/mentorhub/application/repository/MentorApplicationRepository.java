package kg.kut.os.mentorhub.application.repository;

import kg.kut.os.mentorhub.application.entity.MentorApplication;
import kg.kut.os.mentorhub.application.entity.MentorApplicationStatus;
import kg.kut.os.mentorhub.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MentorApplicationRepository extends JpaRepository<MentorApplication, Long> {

    /**
     * Найти последнюю заявку для пользователя (независимо от статуса)
     */
    Optional<MentorApplication> findFirstByApplicantUserOrderByCreatedAtDesc(User user);

    /**
     * Найти заявку со статусом, отличным от REJECTED (PENDING или APPROVED)
     */
    Optional<MentorApplication> findFirstByApplicantUserAndStatusIn(User user, java.util.List<MentorApplicationStatus> statuses);

    /**
     * Получить все заявки с определённым статусом (paginated)
     */
    Page<MentorApplication> findByStatus(MentorApplicationStatus status, Pageable pageable);

    /**
     * Получить все заявки (paginated)
     */
    Page<MentorApplication> findAll(Pageable pageable);
}

