package org.example.bookaroo.repository;

import org.example.bookaroo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<User> findByRole(String role);

    Page<User> findByRole(String role, Pageable pageable);

    // Metody przydatne dla Book Lovers Community
    List<User> findAllByOrderByUsernameAsc();
    
    List<User> findAllByOrderByCreatedAtDesc();

    // Custom @Query - znajdź użytkowników według daty rejestracji
    @Query("SELECT u FROM User u WHERE u.createdAt >= :date ORDER BY u.createdAt DESC")
    List<User> findUsersRegisteredAfter(@Param("date") java.time.LocalDateTime date);

}
