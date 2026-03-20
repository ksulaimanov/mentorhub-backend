package kg.kut.os.mentorhub.availability.repository;

import kg.kut.os.mentorhub.availability.entity.MentorAvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MentorAvailabilitySlotRepository extends JpaRepository<MentorAvailabilitySlot, Long> {
    List<MentorAvailabilitySlot> findAllByMentorUserIdOrderByStartAtAsc(Long userId);
    List<MentorAvailabilitySlot> findAllByMentorIdAndIsActiveTrueOrderByStartAtAsc(Long mentorId);
    Optional<MentorAvailabilitySlot> findByIdAndMentorUserId(Long slotId, Long userId);
}