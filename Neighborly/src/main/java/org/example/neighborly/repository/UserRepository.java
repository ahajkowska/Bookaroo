package org.example.neighborly.repository;

import org.example.neighborly.entity.User;
import org.springframework.data.domain.Page;
import java.awt.print.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    List<User> findByRole(String role);

    List<User> findByReputationScoreGreaterThan(Integer score);

    List<User> findAllByOrderByReputationScoreDesc();

    List<User> findTop10ByOrderByReputationScoreDesc();

    Page<User> findByRole(String role, Pageable pageable);

    // Custom @Query z JPQL
    @Query("SELECT u FROM User u WHERE u.reputationScore >= :minScore ORDER BY u.reputationScore DESC")
    List<User> findUsersWithMinReputation(@Param("minScore") Integer minScore);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
