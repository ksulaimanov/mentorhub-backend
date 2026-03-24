package kg.kut.os.mentorhub.auth.repository;

import kg.kut.os.mentorhub.auth.entity.RoleCode;
import kg.kut.os.mentorhub.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);

    @Query("select count(u) from User u join u.roles r where r.code = :roleCode")
    long countByRoleCode(@Param("roleCode") RoleCode roleCode);
}