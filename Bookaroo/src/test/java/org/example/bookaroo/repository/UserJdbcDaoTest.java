package org.example.bookaroo.repository;

import org.example.bookaroo.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit. jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions. assertThat;

@DataJpaTest
@Import(UserJdbcDao.class)
@Tag("repository")
@Tag("jdbc")
@Tag("integration")
class UserJdbcDaoTest {

    @Autowired
    private UserJdbcDao userJdbcDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate. execute("DELETE FROM users");
    }

    // ROWMAPPER TESTS

    @Test
    @Tag("rowmapper")
    void shouldMapUsername_whenQueryingDatabase() {
        UUID userId = UUID.randomUUID();
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "USER");

        List<User> users = userJdbcDao.findAllUsers();

        User user = findUserById(users, userId);
        assertThat(user.getUsername()).isEqualTo("adam_malysz");
    }

    @Test
    @Tag("rowmapper")
    void shouldMapEmail_whenQueryingDatabase() {
        UUID userId = UUID.randomUUID();
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "USER");

        List<User> users = userJdbcDao.findAllUsers();

        User user = findUserById(users, userId);
        assertThat(user.getEmail()).isEqualTo("adam@gmail.com");
    }

    @Test
    @Tag("rowmapper")
    void shouldMapPassword_whenQueryingDatabase() {
        UUID userId = UUID.randomUUID();
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "USER");

        List<User> users = userJdbcDao.findAllUsers();

        User user = findUserById(users, userId);
        assertThat(user.getPassword()).isEqualTo("password123");
    }

    @Test
    @Tag("rowmapper")
    void shouldMapRole_whenQueryingDatabase() {
        UUID userId = UUID.randomUUID();
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "ADMIN");

        List<User> users = userJdbcDao. findAllUsers();

        User user = findUserById(users, userId);
        assertThat(user.getRole()).isEqualTo("ADMIN");
    }

    @Test
    @Tag("select")
    void shouldReturnAllUsers_whenFindingAll() {
        insertTestUser(UUID.randomUUID(), "adam_malysz", "adam@gmail.com", "USER");
        insertTestUser(UUID.randomUUID(), "magda_gessler", "magdagessler@gmail.com", "ADMIN");
        insertTestUser(UUID.randomUUID(), "justin_bieber", "justin@onet.com.pl", "USER");

        List<User> users = userJdbcDao.findAllUsers();

        assertThat(users).hasSize(3);
    }

    @Test
    @Tag("select")
    void shouldOrderByUsernameDesc_whenFindingAllUsers() {
        insertTestUser(UUID.randomUUID(), "adam_malysz", "adam@gmail.com", "USER");
        insertTestUser(UUID.randomUUID(), "magda_gessler", "magdagessler@gmail.com", "ADMIN");
        insertTestUser(UUID.randomUUID(), "justin_bieber", "justin@onet.com.pl", "USER");

        List<User> users = userJdbcDao.findAllUsers();

        assertThat(users. get(0).getUsername()).isEqualTo("magda_gessler");
    }

    @Test
    @Tag("select")
    void shouldReturnEmptyList_whenNoUsersExist() {
        jdbcTemplate.execute("DELETE FROM users");
        List<User> users = userJdbcDao.findAllUsers();

        assertThat(users).isEmpty();
    }

    @Test
    @Tag("select")
    void shouldFindUsersByRole_whenRoleMatches() {
        insertTestUser(UUID.randomUUID(), "adam_malysz", "adam@gmail.com", "USER");
        insertTestUser(UUID.randomUUID(), "magda_gessler", "magdagessler@gmail.com", "ADMIN");
        insertTestUser(UUID.randomUUID(), "justin_bieber", "justin@onet.com.pl", "USER");

        List<User> users = userJdbcDao.findUsersByRole("USER");

        assertThat(users).hasSize(2);
    }

    @Test
    @Tag("select")
    void shouldReturnOnlyMatchingRole_whenFindingByRole() {
        insertTestUser(UUID.randomUUID(), "adam_malysz", "adam@gmail.com", "USER");
        insertTestUser(UUID.randomUUID(), "magda_gessler", "magdagessler@gmail.com", "ADMIN");
        insertTestUser(UUID.randomUUID(), "justin_bieber", "justin@onet.com.pl", "USER");

        List<User> users = userJdbcDao.findUsersByRole("USER");

        assertThat(users).allMatch(user -> user.getRole().equals("USER"));
    }

    @Test
    @Tag("select")
    void shouldReturnEmptyList_whenRoleNotFound() {
        insertTestUser(UUID.randomUUID(), "adam_malysz", "adam@gmail.com", "USER");
        insertTestUser(UUID.randomUUID(), "magda_gessler", "magdagessler@gmail.com", "ADMIN");
        insertTestUser(UUID.randomUUID(), "justin_bieber", "justin@onet.com.pl", "USER");

        List<User> users = userJdbcDao.findUsersByRole("MODERATOR");

        assertThat(users). isEmpty();
    }

    @Test
    @Tag("select")
    @Tag("aggregation")
    void shouldReturnCorrectCount_whenGettingTotalUserCount() {
        insertTestUser(UUID.randomUUID(), "adam_malysz", "adam@gmail.com", "USER");
        insertTestUser(UUID.randomUUID(), "magda_gessler", "magdagessler@gmail.com", "ADMIN");
        insertTestUser(UUID.randomUUID(), "justin_bieber", "justin@onet.com.pl", "USER");

        Integer count = userJdbcDao.getTotalUserCount();

        assertThat(count).isEqualTo(3);
    }

    @Test
    @Tag("select")
    @Tag("aggregation")
    void shouldReturnZero_whenNoUsersExist() {
        jdbcTemplate.execute("DELETE FROM users");

        Integer count = userJdbcDao.getTotalUserCount();

        assertThat(count).isEqualTo(0);
    }

    @Test
    @Tag("insert")
    @Tag("crud")
    void shouldReturnOne_whenInsertingUser() {
        User newUser = createUser("nowy_user", "nowy@gmail.com", "USER");

        int rowsAffected = userJdbcDao.insertUser(newUser);

        assertThat(rowsAffected).isEqualTo(1);
    }

    @Test
    @Tag("insert")
    @Tag("crud")
    void shouldIncreaseCount_whenInsertingUser() {
        User newUser = createUser("nowy_user", "nowy@gmail.com", "USER");
        int countBefore = userJdbcDao.getTotalUserCount();

        userJdbcDao. insertUser(newUser);

        int countAfter = userJdbcDao.getTotalUserCount();
        assertThat(countAfter).isEqualTo(countBefore + 1);
    }

    @Test
    @Tag("insert")
    @Tag("crud")
    void shouldPersistUsername_whenInsertingUser() {
        User newUser = createUser("nowy_user", "nowy@gmail.com", "USER");

        userJdbcDao.insertUser(newUser);

        List<User> allUsers = userJdbcDao. findAllUsers();
        User inserted = allUsers.stream()
                .filter(u -> u.getUsername().equals("nowy_user"))
                .findFirst()
                .orElseThrow();
        assertThat(inserted.getUsername()).isEqualTo("nowy_user");
    }

    @Test
    @Tag("delete")
    @Tag("crud")
    void shouldReturnOne_whenDeletingExistingUser() {
        UUID userId = UUID.randomUUID();
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "USER");

        int rowsAffected = userJdbcDao.deleteUser(userId);

        assertThat(rowsAffected).isEqualTo(1);
    }

    @Test
    @Tag("delete")
    @Tag("crud")
    void shouldDecreaseCount_whenDeletingUser() {
        UUID userId = UUID.randomUUID();
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "USER");

        int countBefore = userJdbcDao.getTotalUserCount();

        userJdbcDao.deleteUser(userId);

        int countAfter = userJdbcDao.getTotalUserCount();
        assertThat(countAfter).isEqualTo(countBefore - 1);
    }

    @Test
    @Tag("delete")
    @Tag("crud")
    void shouldRemoveUser_whenDeleting() {
        UUID userId = UUID.randomUUID();
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "USER");

        userJdbcDao.deleteUser(userId);

        List<User> users = userJdbcDao.findAllUsers();
        boolean userExists = users.stream()
                .anyMatch(u -> u.getId().equals(userId));
        assertThat(userExists).isFalse();
    }

    @Test
    @Tag("delete")
    @Tag("crud")
    void shouldReturnZero_whenDeletingNonExistentUser() {
        UUID userId = UUID.randomUUID();
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "USER");

        UUID nonExistentId = UUID.randomUUID();

        int rowsAffected = userJdbcDao.deleteUser(nonExistentId);

        assertThat(rowsAffected). isEqualTo(0);
    }

    // pomocnicze metody
    private void insertTestUser(UUID id, String username, String email, String role) {
        jdbcTemplate.update(
                "INSERT INTO users (id, username, email, password, role) " +
                        "VALUES (?, ?, ?, ?, ?)",
                id.toString(), username, email, "password123", role
        );
    }

    private User createUser(String username, String email, String role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("encodedPassword123");
        user.setRole(role);
        return user;
    }

    private User findUserById(List<User> users, UUID id) {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }
}