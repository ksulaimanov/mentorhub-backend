package kg.kut.os.mentorhub.auth.repository;

import kg.kut.os.mentorhub.auth.entity.PasswordResetCode;
import kg.kut.os.mentorhub.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {
    Optional<PasswordResetCode> findTopByUserOrderByCreatedAtDesc(User user);
}