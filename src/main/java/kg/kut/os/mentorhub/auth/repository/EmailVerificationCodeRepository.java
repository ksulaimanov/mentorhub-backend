package kg.kut.os.mentorhub.auth.repository;

import kg.kut.os.mentorhub.auth.entity.EmailVerificationCode;
import kg.kut.os.mentorhub.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long> {
    Optional<EmailVerificationCode> findTopByUserOrderByCreatedAtDesc(User user);
}