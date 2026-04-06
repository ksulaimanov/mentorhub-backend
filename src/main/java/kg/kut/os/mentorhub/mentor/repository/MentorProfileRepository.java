package kg.kut.os.mentorhub.mentor.repository;

import kg.kut.os.mentorhub.mentor.entity.MentorProfile;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long> {
    Optional<MentorProfile> findByUserId(Long userId);
    Optional<MentorProfile> findByIdAndIsPublicTrue(Long id);

    /**
     * Eagerly fetches the User association to avoid N+1 when mapping directory items.
     */
    @Query("select mp from MentorProfile mp join fetch mp.user where mp.isPublic = true")
    List<MentorProfile> findAllPublicWithUser(Sort sort);

    List<MentorProfile> findAllByIsPublicTrue(Sort sort);
    Optional<MentorProfile> findByUserEmail(String email);
}