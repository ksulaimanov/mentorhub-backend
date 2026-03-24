package kg.kut.os.mentorhub.mentor.repository;

import kg.kut.os.mentorhub.mentor.entity.MentorProfile;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long> {
    Optional<MentorProfile> findByUserId(Long userId);
    Optional<MentorProfile> findByIdAndIsPublicTrue(Long id);
    List<MentorProfile> findAllByIsPublicTrue(Sort sort);
    Optional<MentorProfile> findByUserEmail(String email);
}