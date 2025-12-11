package org.example.bookaroo.repository;

import org.example.bookaroo.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Tag("repository")
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager; // entity manager do testów

    @BeforeEach
    void setUp() {
        userRepository.deleteAllInBatch();
        entityManager.clear();
    }

    @Test
    @Tag("crud")
    @Tag("create")
    void shouldGenerateId_whenSavingNewUser() {
        User newUser = createUser("new_user", "newuser@example.com", "USER", 0);

        User savedUser = userRepository.save(newUser);
        entityManager.flush();

        assertThat(savedUser.getId()).isNotNull();
    }

    @Test
    @Tag("crud")
    @Tag("create")
    void shouldSaveUsername_whenSavingNewUser() {
        User newUser = createUser("new_user", "newuser@example.com", "USER", 0);

        User savedUser = userRepository.save(newUser);
        entityManager.flush();

        assertThat(savedUser.getUsername()).isEqualTo("new_user");
    }

    @Test
    @Tag("crud")
    @Tag("create")
    void shouldSaveEmail_whenSavingNewUser() {
        User newUser = createUser("new_user", "newuser@example.com", "USER", 0);

        User savedUser = userRepository.save(newUser);
        entityManager.flush();

        assertThat(savedUser.getEmail()).isEqualTo("newuser@example.com");
    }

    @Test
    @Tag("crud")
    @Tag("read")
    void shouldFindUserById_whenUserExists() {
        User user = createUser("adam_malysz", "adam@gmail.com", "USER", 100);
        User savedUser = userRepository.save(user);
        entityManager.flush();
        UUID userId = savedUser.getId();

        Optional<User> foundUser = userRepository.findById(userId);

        assertThat(foundUser). isPresent();
    }

    @Test
    @Tag("crud")
    @Tag("read")
    void shouldReturnCorrectUsername_whenFindingById() {
        User user = createUser("adam_malysz", "adam@gmail.com", "USER", 100);
        User savedUser = userRepository.save(user);
        entityManager.flush();
        UUID userId = savedUser.getId();

        Optional<User> foundUser = userRepository.findById(userId);

        assertThat(foundUser.get().getUsername()).isEqualTo("adam_malysz");
    }

    @Test
    @Tag("crud")
    @Tag("read")
    void shouldReturnEmpty_whenUserDoesNotExist() {
        UUID nonExistentId = UUID.randomUUID();

        Optional<User> foundUser = userRepository.findById(nonExistentId);

        assertThat(foundUser).isEmpty();
    }

    @Test
    @Tag("crud")
    @Tag("read")
    void shouldFindAllUsers_whenMultipleUsersExist() {
        userRepository.save(createUser("adam_malysz", "adam@gmail.com", "USER", 100));
        userRepository.save(createUser("magda_gessler", "magda@gmail.com", "ADMIN", 250));
        userRepository.save(createUser("justin_bieber", "justin@onet.com.pl", "USER", 50));
        entityManager.flush();

        List<User> allUsers = userRepository.findAll();

        assertThat(allUsers).hasSize(3);
    }

    @Test
    @Tag("crud")
    @Tag("update")
    void shouldUpdateReputationScore_whenUserExists() {
        User user = createUser("adam_malysz", "adam@gmail.com", "USER", 100);
        User savedUser = userRepository.save(user);
        entityManager.flush();
        UUID userId = savedUser. getId();

        savedUser.setReputationScore(500);
        userRepository.save(savedUser);
        entityManager.flush();

        User foundUser = userRepository.findById(userId).orElseThrow();
        assertThat(foundUser.getReputationScore()).isEqualTo(500);
    }

    @Test
    @Tag("crud")
    @Tag("update")
    void shouldUpdateRole_whenUserExists() {
        User user = createUser("adam_malysz", "adam@gmail.com", "USER", 100);
        User savedUser = userRepository.save(user);
        entityManager.flush();

        savedUser. setRole("ADMIN");
        userRepository.save(savedUser);
        entityManager.flush();

        User foundUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(foundUser.getRole()).isEqualTo("ADMIN");
    }

    @Test
    @Tag("crud")
    @Tag("delete")
    void shouldDeleteUser_whenUserExists() {
        User user = createUser("adam_malysz", "adam@gmail.com", "USER", 100);
        User savedUser = userRepository.save(user);
        entityManager.flush();
        UUID userId = savedUser.getId();

        userRepository.deleteById(userId);
        entityManager.flush();

        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
    }

    @Test
    @Tag("crud")
    @Tag("delete")
    void shouldReduceCount_whenDeletingUser() {
        User user1 = userRepository.save(createUser("adam_malysz", "adam@gmail.com", "USER", 100));
        User user2 = userRepository.save(createUser("magda_gessler", "magda@gmail.com", "ADMIN", 250));
        entityManager.flush();
        long countBefore = userRepository.count();

        userRepository. deleteById(user1.getId());
        entityManager.flush();

        assertThat(userRepository.count()). isEqualTo(countBefore - 1);
    }

    @Test
    @Tag("custom-query")
    void shouldFindUserByEmail_whenEmailExists() {
        User user = createUser("adam_malysz", "adam@gmail.com", "USER", 100);
        userRepository.save(user);
        entityManager.flush();

        Optional<User> foundUser = userRepository.findByEmail("adam@gmail.com");

        assertThat(foundUser).isPresent();
    }

    @Test
    @Tag("custom-query")
    void shouldReturnCorrectUser_whenFindingByEmail() {
        User user = createUser("adam_malysz", "adam@gmail.com", "USER", 100);
        userRepository.save(user);
        entityManager.flush();

        Optional<User> foundUser = userRepository.findByEmail("adam@gmail.com");

        assertThat(foundUser.get().getUsername()).isEqualTo("adam_malysz");
    }

    @Test
    @Tag("custom-query")
    void shouldReturnEmpty_whenEmailDoesNotExist() {
        User user = createUser("adam_malysz", "adam@gmail.com", "USER", 100);
        userRepository.save(user);
        entityManager.flush();

        Optional<User> foundUser = userRepository.findByEmail("nieistniejacy@email.com");

        assertThat(foundUser).isEmpty();
    }

    @Test
    @Tag("custom-query")
    void shouldFindUserByUsername_whenUsernameExists() {
        User user = createUser("adam_malysz", "adam@gmail.com", "USER", 150);
        userRepository.save(user);
        entityManager.flush();

        Optional<User> foundUser = userRepository.findByUsername("adam_malysz");

        assertThat(foundUser).isPresent();
    }

    @Test
    @Tag("custom-query")
    void shouldReturnTrue_whenEmailExists() {
        User user = createUser("adam_malysz", "adam@gmail.com", "USER", 150);
        userRepository.save(user);
        entityManager.flush();

        boolean exists = userRepository.existsByEmail("adam@gmail.com");

        assertThat(exists).isTrue();
    }

    @Test
    @Tag("custom-query")
    void shouldReturnFalse_whenEmailDoesNotExist() {
        User user = createUser("adam_malysz", "adam@gmail.com", "USER", 150);
        userRepository.save(user);
        entityManager.flush();

        boolean exists = userRepository.existsByEmail("nieistniejacy@email.com");

        assertThat(exists).isFalse();
    }

    @Test
    @Tag("custom-query")
    void shouldReturnTrue_whenUsernameExists() {
        User user = createUser("magda_gessler", "magda@gmail.com", "USER", 150);
        userRepository.save(user);
        entityManager.flush();

        boolean exists = userRepository.existsByUsername("magda_gessler");

        assertThat(exists).isTrue();
    }

    @Test
    @Tag("custom-query")
    void shouldReturnFalse_whenUsernameDoesNotExist() {
        User user = createUser("magda_gessler", "magda@gmail.com", "ADMIN", 200);
        userRepository.save(user);
        entityManager.flush();

        boolean exists = userRepository.existsByUsername("brak");

        assertThat(exists).isFalse();
    }

    @Test
    @Tag("custom-query")
    void shouldFindUsersByRole_whenRoleMatches() {
        userRepository.save(createUser("adam_malysz", "adam@gmailcom", "USER", 100));
        userRepository.save(createUser("magda_gessler", "magda@gmail.com", "ADMIN", 250));
        userRepository.save(createUser("justin_bieber", "justin@onet.com.pl", "USER", 50));
        entityManager.flush();

        List<User> users = userRepository.findByRole("USER");

        assertThat(users).hasSize(2);
    }

    @Test
    @Tag("custom-query")
    void shouldReturnOnlyMatchingRole_whenFindingByRole() {
        userRepository.save(createUser("adam_malysz", "adam@gmailcom", "USER", 100));
        userRepository.save(createUser("magda_gessler", "magda@gmail.com", "ADMIN", 250));
        userRepository.save(createUser("justin_bieber", "justin@onet.com.pl", "USER", 50));
        entityManager.flush();

        List<User> users = userRepository.findByRole("USER");

        assertThat(users).allMatch(user -> user.getRole().equals("USER"));
    }

    @Test
    @Tag("custom-query")
    void shouldReturnEmptyList_whenRoleNotFound() {
        User user = createUser("adam_malysz", "adam@gmail.com", "USER", 150);
        userRepository.save(user);
        entityManager.flush();

        List<User> users = userRepository.findByRole("MODERATOR");

        assertThat(users).isEmpty();
    }

    @Test
    @Tag("custom-query")
    @Tag("pagination")
    void shouldReturnPagedResults_whenFindingByRoleWithPageable() {
        for (int i = 1; i <= 15; i++) {
            userRepository.save(createUser("user" + i, "user" + i + "@gmail.com", "USER", i * 10));
        }
        entityManager.flush();
        Pageable pageable = PageRequest.of(0, 10);

        Page<User> usersPage = userRepository.findByRole("USER", pageable);

        assertThat(usersPage.getContent()).hasSize(10);
    }

    @Test
    @Tag("custom-query")
    @Tag("pagination")
    void shouldReturnCorrectTotalElements_whenFindingByRoleWithPageable() {
        for (int i = 1; i <= 15; i++) {
            userRepository.save(createUser("user" + i, "user" + i + "@gmail.com", "USER", i * 10));
        }
        entityManager.flush();
        Pageable pageable = PageRequest.of(0, 10);

        Page<User> usersPage = userRepository.findByRole("USER", pageable);

        assertThat(usersPage.getTotalElements()).isEqualTo(15);
    }

    @Test
    @Tag("custom-query")
    @Tag("pagination")
    void shouldReturnSecondPage_whenRequestingPageOne() {
        for (int i = 1; i <= 15; i++) {
            userRepository.save(createUser("user" + i, "user" + i + "@gmail.com", "USER", i * 10));
        }
        entityManager.flush();
        Pageable pageable = PageRequest.of(1, 10); // druga strona

        Page<User> usersPage = userRepository.findByRole("USER", pageable);

        assertThat(usersPage.getContent()).hasSize(5); // pozostałe elementy
    }

    @Test
    @Tag("custom-query")
    void shouldOrderByReputationDesc_whenFindingAll() {
        userRepository.save(createUser("adam_malysz", "adam@gmailcom", "USER", 100));
        userRepository.save(createUser("magda_gessler", "magda@gmail.com", "ADMIN", 250));
        userRepository.save(createUser("justin_bieber", "justin@onet.com.pl", "USER", 50));
        entityManager.flush();

        List<User> users = userRepository.findAllByOrderByReputationScoreDesc();

        assertThat(users.get(0).getReputationScore()).isEqualTo(250);
    }

    @Test
    @Tag("custom-query")
    void shouldReturnAllUsers_whenFindingOrderedByReputation() {
        userRepository.save(createUser("adam_malysz", "adam@gmailcom", "USER", 100));
        userRepository.save(createUser("magda_gessler", "magda@gmail.com", "ADMIN", 250));
        userRepository.save(createUser("justin_bieber", "justin@onet.com.pl", "USER", 50));
        entityManager.flush();

        List<User> users = userRepository. findAllByOrderByReputationScoreDesc();

        assertThat(users). hasSize(3);
    }

    @Test
    @Tag("custom-query")
    void shouldMaintainDescendingOrder_whenFindingByReputation() {
        userRepository.save(createUser("adam_malysz", "adam@gmailcom", "USER", 100));
        userRepository.save(createUser("magda_gessler", "magda@gmail.com", "ADMIN", 250));
        userRepository.save(createUser("justin_bieber", "justin@onet.com.pl", "USER", 50));
        entityManager.flush();

        List<User> users = userRepository. findAllByOrderByReputationScoreDesc();

        assertThat(users)
                .extracting(User::getReputationScore)
                .containsExactly(250, 100, 50);
    }

    @Test
    @Tag("custom-query")
    @Tag("jpql")
    void shouldFindUsersWithMinReputation_whenUsingJpqlQuery() {
        userRepository.save(createUser("adam_malysz", "adam@gmailcom", "USER", 100));
        userRepository.save(createUser("magda_gessler", "magda@gmail.com", "ADMIN", 250));
        userRepository.save(createUser("justin_bieber", "justin@onet.com.pl", "USER", 50));
        entityManager.flush();

        List<User> users = userRepository. findUsersWithMinReputation(100);

        assertThat(users).hasSize(2);
    }

    @Test
    @Tag("custom-query")
    @Tag("jpql")
    void shouldOrderByReputationDesc_whenUsingJpqlQuery() {
        userRepository.save(createUser("adam_malysz", "adam@gmailcom", "USER", 100));
        userRepository.save(createUser("magda_gessler", "magda@gmail.com", "ADMIN", 250));
        userRepository.save(createUser("justin_bieber", "justin@onet.com.pl", "USER", 50));
        entityManager.flush();

        List<User> users = userRepository.findUsersWithMinReputation(100);

        assertThat(users. get(0).getReputationScore()).isEqualTo(250);
    }

    @Test
    @Tag("custom-query")
    @Tag("jpql")
    void shouldIncludeUsersWithEqualReputation_whenUsingJpqlQuery() {
        userRepository.save(createUser("adam_malysz", "adam@gmailcom", "USER", 100));
        userRepository.save(createUser("magda_gessler", "magda@gmail.com", "ADMIN", 250));
        userRepository.save(createUser("justin_bieber", "justin@onet.com.pl", "USER", 50));
        entityManager.flush();

        List<User> users = userRepository.findUsersWithMinReputation(100);

        assertThat(users).anyMatch(user -> user.getReputationScore(). equals(100));
    }

    @Test
    @Tag("custom-query")
    @Tag("jpql")
    void shouldReturnEmptyList_whenNoUsersMatchMinReputation() {
        userRepository.save(createUser("adam_malysz", "adam@gmailcom", "USER", 100));
        userRepository.save(createUser("magda_gessler", "magda@gmail.com", "ADMIN", 250));
        userRepository.save(createUser("justin_bieber", "justin@onet.com.pl", "USER", 50));
        entityManager.flush();

        List<User> users = userRepository.findUsersWithMinReputation(500);

        assertThat(users).isEmpty();
    }

    @Test
    @Tag("edge-case")
    void shouldHandleZeroReputation() {
        User zeroRepUser = createUser("zero_rep", "zero@test.com", "USER", 0);

        User savedUser = userRepository.save(zeroRepUser);
        entityManager.flush();

        assertThat(savedUser. getReputationScore()).isEqualTo(0);
    }

    // pomocnicze tworzenie usera
    private User createUser(String username, String email, String role, Integer reputation) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user. setPassword("encodedPassword123");
        user.setRole(role);
        user.setFirstName("Imie");
        user.setLastName("Nazwisko");
        user.setReputationScore(reputation);
        return user;
    }
}
