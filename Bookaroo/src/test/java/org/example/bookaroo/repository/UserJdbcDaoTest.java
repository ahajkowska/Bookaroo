package org.example.bookaroo.repository;

import org.example.bookaroo.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(UserJdbcDao.class)
@Tag("repository")
class UserJdbcDaoTest {

    @Autowired
    private UserJdbcDao userJdbcDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final UUID TEST_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final String TEST_USERNAME = "adam_malysz";
    private final String TEST_EMAIL = "adam@wisla.pl";
    private final String TEST_PASS = "narty123";
    private final String TEST_ROLE = "USER";
    private final String TEST_AVATAR = "ski.png";
    private final String TEST_BIO = "Orzeł z Wisły";

    // ROW MAPPER
    @Test
    @DisplayName("RowMapper: Powinien poprawnie zmapować wszystkie pola")
    @Tag("rowmapper")
    void shouldMapAllFields_whenQueryingDatabase() {
        insertFullTestUser();

        List<User> users = userJdbcDao.findAllUsers();

        User user = users.stream()
                .filter(u -> u.getId().equals(TEST_ID))
                .findFirst()
                .orElseThrow();

        assertThat(user.getId()).isEqualTo(TEST_ID);
        assertThat(user.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(user.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(user.getPassword()).isEqualTo(TEST_PASS);
        assertThat(user.getRole()).isEqualTo(TEST_ROLE);
        assertThat(user.getAvatar()).isEqualTo(TEST_AVATAR);
        assertThat(user.getBio()).isEqualTo(TEST_BIO);
        assertThat(user.isLocked()).isFalse();
    }

    // READ

    @Test
    @DisplayName("FindAll: Powinien zwrócić poprawną liczbę użytkowników")
    @Tag("select")
    void shouldReturnCorrectNumberOfUsers_whenFindingAll() {
        insertUserWithUsername("user1");
        insertUserWithUsername("user2");
        insertUserWithUsername("user3");

        List<User> users = userJdbcDao.findAllUsers();

        assertThat(users).hasSize(3);
    }

    @Test
    @DisplayName("FindAll: Powinien sortować użytkowników malejąco po nazwie (Z-A)")
    @Tag("select")
    void shouldSortUsersByUsernameDesc_whenFindingAll() {
        insertUserWithUsername("Adam");
        insertUserWithUsername("Bartek");
        insertUserWithUsername("Celina");

        List<User> result = userJdbcDao.findAllUsers();

        assertThat(result).extracting(User::getUsername)
                .containsExactly("Celina", "Bartek", "Adam");
    }

    @Test
    @DisplayName("FindAll: Powinien zwrócić pustą listę, gdy brak użytkowników")
    @Tag("select")
    void shouldReturnEmptyList_whenNoUsersInDb() {
        List<User> result = userJdbcDao.findAllUsers();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("FindByRole: Powinien zwrócić tylko użytkowników o zadanej roli")
    @Tag("select")
    void shouldReturnOnlyMatchingRole_whenFilteringByRole() {
        insertUserWithRole("USER");
        insertUserWithRole("ADMIN");
        insertUserWithRole("USER");

        List<User> result = userJdbcDao.findUsersByRole("USER");

        assertThat(result).hasSize(2)
                .allMatch(u -> u.getRole().equals("USER"));
    }

    @Test
    @DisplayName("FindByRole: Powinien sortować po nazwie rosnąco (A-Z)")
    @Tag("select")
    void shouldSortByUsernameAsc_whenFilteringByRole() {
        insertUserWithUsername("Zenon");
        insertUserWithUsername("Anna");

        List<User> result = userJdbcDao.findUsersByRole("USER");

        assertThat(result).extracting(User::getUsername)
                .containsExactly("Anna", "Zenon");
    }

    // AGGREGATION

    @Test
    @DisplayName("Count: Powinien zwrócić zero dla pustej tabeli")
    @Tag("aggregation")
    void shouldReturnZeroCount_whenTableIsEmpty() {
        Integer count = userJdbcDao.getTotalUserCount();
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Count: Powinien zwrócić dokładną liczbę rekordów")
    @Tag("aggregation")
    void shouldReturnCorrectCount_whenUsersExist() {
        insertUserWithUsername("u1");
        insertUserWithUsername("u2");
        Integer count = userJdbcDao.getTotalUserCount();
        assertThat(count).isEqualTo(2);
    }

    // CREATE

    @Test
    @DisplayName("Insert: Powinien zwrócić 1 (liczba zmienionych wierszy)")
    @Tag("insert")
    void shouldReturnOneRowAffected_whenInsertingUser() {
        User newUser = createUserObject();
        int rowsAffected = userJdbcDao.insertUser(newUser);
        assertThat(rowsAffected).isEqualTo(1);
    }

    @Test
    @DisplayName("Insert: Powinien faktycznie zapisać dane w bazie")
    @Tag("insert")
    void shouldPersistDataInDatabase_whenInsertingUser() {
        User newUser = createUserObject();
        userJdbcDao.insertUser(newUser);

        // Niezależna weryfikacja
        Integer dbCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ?",
                Integer.class,
                newUser.getId().toString()
        );
        assertThat(dbCount).isEqualTo(1);
    }

    // DELETE

    @Test
    @DisplayName("Delete: Powinien zwrócić 1 po usunięciu istniejącego użytkownika")
    @Tag("delete")
    void shouldReturnOneRowAffected_whenDeletingExistingUser() {
        insertFullTestUser();
        int rowsAffected = userJdbcDao.deleteUser(TEST_ID);
        assertThat(rowsAffected).isEqualTo(1);
    }

    @Test
    @DisplayName("Delete: Powinien faktycznie usunąć rekord z bazy")
    @Tag("delete")
    void shouldRemoveRecordFromDatabase_whenDeleting() {
        insertFullTestUser();

        userJdbcDao.deleteUser(TEST_ID);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ?",
                Integer.class,
                TEST_ID.toString()
        );
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Delete: Powinien zwrócić 0 przy próbie usunięcia nieistniejącego ID")
    @Tag("delete")
    void shouldReturnZero_whenDeletingNonExistentUser() {
        int rowsAffected = userJdbcDao.deleteUser(UUID.randomUUID());
        assertThat(rowsAffected).isZero();
    }

    // m. pomocnicze

    private void insertFullTestUser() {
        jdbcTemplate.update(
                """
                INSERT INTO users (id, username, email, password, role, avatar, bio, is_locked) 
                VALUES (?, ?, ?, ?, ?, ?, ?, false)
                """,
                TEST_ID.toString(), TEST_USERNAME, TEST_EMAIL, TEST_PASS, TEST_ROLE, TEST_AVATAR, TEST_BIO
        );
    }

    private void insertUserWithUsername(String username) {
        jdbcTemplate.update(
                """
                INSERT INTO users (id, username, email, password, role, avatar, bio, is_locked) 
                VALUES (?, ?, ?, 'pass', 'USER', 'img', 'bio', false)
                """,
                UUID.randomUUID().toString(), username, username + "@test.com"
        );
    }

    private void insertUserWithRole(String role) {
        jdbcTemplate.update(
                """
                INSERT INTO users (id, username, email, password, role, avatar, bio, is_locked) 
                VALUES (?, ?, ?, 'pass', ?, 'img', 'bio', false)
                """,
                UUID.randomUUID().toString(), "user_" + UUID.randomUUID(), UUID.randomUUID() + "@test.com", role
        );
    }

    private User createUserObject() {
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setUsername("new_user");
        u.setEmail("new@test.com");
        u.setPassword("pass");
        u.setRole("USER");
        u.setAvatar("default.png");
        u.setBio("bio");
        u.setLocked(false);
        return u;
    }
}