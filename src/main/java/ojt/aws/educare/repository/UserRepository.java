package ojt.aws.educare.repository;

import ojt.aws.educare.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // ── NEW: Cognito identity lookup ──────────────────────────────────────────
    // Used by CognitoUserSyncService to find an existing account by the Cognito
    // subject identifier (UUID assigned by Cognito, stable across email changes).
    Optional<User> findByCognitoSub(String cognitoSub);

    @Query("SELECT u FROM User u WHERE " +
            "(:roleName = '' OR UPPER(u.role.roleName) = :roleName) AND " +
            "(:keyword = '' OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "u.phone LIKE CONCAT('%', :keyword, '%'))")
    Page<User> searchUsersAndFilterByRole(
            @Param("keyword") String keyword,
            @Param("roleName") String roleName,
            Pageable pageable);
}
