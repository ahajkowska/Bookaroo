package org.example. neighborly.repository;

import org.example.neighborly.entity.User;
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
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "USER", 100);

        List<User> users = userJdbcDao.findAllUsers();

        User user = findUserById(users, userId);
        assertThat(user.getUsername()).isEqualTo("adam_malysz");
    }

    @Test
    @Tag("rowmapper")
    void shouldMapEmail_whenQueryingDatabase() {
        UUID userId = UUID.randomUUID();
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "USER", 100);

        List<User> users = userJdbcDao.findAllUsers();

        User user = findUserById(users, userId);
        assertThat(user.getEmail()).isEqualTo("adam@gmail.com");
    }

    @Test
    @Tag("rowmapper")
    void shouldMapPassword_whenQueryingDatabase() {
        UUID userId = UUID.randomUUID();
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "USER", 100);

        List<User> users = userJdbcDao.findAllUsers();

        User user = findUserById(users, userId);
        assertThat(user.getPassword()).isEqualTo("password123");
    }

    @Test
    @Tag("rowmapper")
    void shouldMapRole_whenQueryingDatabase() {
        UUID userId = UUID.randomUUID();
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "ADMIN", 100);

        List<User> users = userJdbcDao. findAllUsers();

        User user = findUserById(users, userId);
        assertThat(user.getRole()).isEqualTo("ADMIN");
    }

    @Test
    @Tag("rowmapper")
    void shouldMapFirstName_whenQueryingDatabase() {
        UUID userId = UUID.randomUUID();
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "USER", 100);

        List<User> users = userJdbcDao.findAllUsers();

        User user = findUserById(users, userId);
        assertThat(user.getFirstName()).isEqualTo("Adam");
    }

    @Test
    @Tag("rowmapper")
    void shouldMapLastName_whenQueryingDatabase() {
        UUID userId = UUID.randomUUID();
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "USER", 100);

        List<User> users = userJdbcDao.findAllUsers();

        User user = findUserById(users, userId);
        assertThat(user.getLastName()).isEqualTo("Malysz");
    }

    @Test
    @Tag("rowmapper")
    void shouldMapReputationScore_whenQueryingDatabase() {
        UUID userId = UUID.randomUUID();
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "USER", 100);

        List<User> users = userJdbcDao.findAllUsers();

        User user = findUserById(users, userId);
        assertThat(user.getReputationScore()).isEqualTo(100);
    }

    @Test
    @Tag("select")
    void shouldReturnAllUsers_whenFindingAll() {
        insertTestUser(UUID.randomUUID(), "adam_malysz", "adam@gmail.com", "USER", 100);
        insertTestUser(UUID.randomUUID(), "magda_gessler", "magdagessler@gmail.com", "ADMIN", 250);
        insertTestUser(UUID.randomUUID(), "justin_bieber", "justin@onet.com.pl", "USER", 50);

        List<User> users = userJdbcDao.findAllUsers();

        assertThat(users).hasSize(3);
    }

    @Test
    @Tag("select")
    void shouldOrderByUsernameDesc_whenFindingAllUsers() {
        insertTestUser(UUID.randomUUID(), "adam_malysz", "adam@gmail.com", "USER", 100);
        insertTestUser(UUID.randomUUID(), "magda_gessler", "magdagessler@gmail.com", "ADMIN", 250);
        insertTestUser(UUID.randomUUID(), "justin_bieber", "justin@onet.com.pl", "USER", 50);

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
        insertTestUser(UUID.randomUUID(), "adam_malysz", "adam@gmail.com", "USER", 100);
        insertTestUser(UUID.randomUUID(), "magda_gessler", "magdagessler@gmail.com", "ADMIN", 250);
        insertTestUser(UUID.randomUUID(), "justin_bieber", "justin@onet.com.pl", "USER", 50);

        List<User> users = userJdbcDao.findUsersByRole("USER");

        assertThat(users).hasSize(2);
    }

    @Test
    @Tag("select")
    void shouldOrderByReputationDesc_whenFindingByRole() {
        insertTestUser(UUID.randomUUID(), "adam_malysz", "adam@gmail.com", "USER", 100);
        insertTestUser(UUID.randomUUID(), "magda_gessler", "magdagessler@gmail.com", "ADMIN", 250);
        insertTestUser(UUID.randomUUID(), "justin_bieber", "justin@onet.com.pl", "USER", 50);

        List<User> users = userJdbcDao.findUsersByRole("USER");

        assertThat(users.get(0).getReputationScore()).isEqualTo(100);
    }

    @Test
    @Tag("select")
    void shouldReturnOnlyMatchingRole_whenFindingByRole() {
        insertTestUser(UUID.randomUUID(), "adam_malysz", "adam@gmail.com", "USER", 100);
        insertTestUser(UUID.randomUUID(), "magda_gessler", "magdagessler@gmail.com", "ADMIN", 250);
        insertTestUser(UUID.randomUUID(), "justin_bieber", "justin@onet.com.pl", "USER", 50);

        List<User> users = userJdbcDao.findUsersByRole("USER");

        assertThat(users).allMatch(user -> user.getRole().equals("USER"));
    }

    @Test
    @Tag("select")
    void shouldReturnEmptyList_whenRoleNotFound() {
        insertTestUser(UUID.randomUUID(), "adam_malysz", "adam@gmail.com", "USER", 100);
        insertTestUser(UUID.randomUUID(), "magda_gessler", "magdagessler@gmail.com", "ADMIN", 250);
        insertTestUser(UUID.randomUUID(), "justin_bieber", "justin@onet.com.pl", "USER", 50);

        List<User> users = userJdbcDao.findUsersByRole("MODERATOR");

        assertThat(users). isEmpty();
    }

    @Test
    @Tag("select")
    void shouldLimitResults_whenGettingTopUsers() {
        insertTestUser(UUID.randomUUID(), "adam_malysz", "adam@gmail.com", "USER", 100);
        insertTestUser(UUID.randomUUID(), "magda_gessler", "magdagessler@gmail.com", "ADMIN", 250);
        insertTestUser(UUID.randomUUID(), "justin_bieber", "justin@onet.com.pl", "USER", 50);

        List<User> topUsers = userJdbcDao.getTopUsersByReputation(2);

        assertThat(topUsers).hasSize(2);
    }

    @Test
    @Tag("select")
    void shouldReturnHighestReputation_whenGettingTopUsers() {
        insertTestUser(UUID.randomUUID(), "adam_malysz", "adam@gmail.com", "USER", 100);
        insertTestUser(UUID.randomUUID(), "magda_gessler", "magdagessler@gmail.com", "ADMIN", 250);
        insertTestUser(UUID.randomUUID(), "justin_bieber", "justin@onet.com.pl", "USER", 50);

        List<User> topUsers = userJdbcDao.getTopUsersByReputation(3);

        assertThat(topUsers. get(0).getReputationScore()).isEqualTo(250);
    }

    @Test
    @Tag("select")
    void shouldOrderByReputationDesc_whenGettingTopUsers() {
        insertTestUser(UUID.randomUUID(), "adam_malysz", "adam@gmail.com", "USER", 100);
        insertTestUser(UUID.randomUUID(), "magda_gessler", "magdagessler@gmail.com", "ADMIN", 250);
        insertTestUser(UUID.randomUUID(), "justin_bieber", "justin@onet.com.pl", "USER", 50);

        List<User> topUsers = userJdbcDao.getTopUsersByReputation(3);

        assertThat(topUsers)
                .extracting(User::getReputationScore)
                .containsExactly(250, 100, 50);
    }

    @Test
    @Tag("select")
    void shouldReturnAllUsers_whenLimitExceedsCount() {
        insertTestUser(UUID.randomUUID(), "adam_malysz", "adam@gmail.com", "USER", 100);
        insertTestUser(UUID.randomUUID(), "magda_gessler", "magdagessler@gmail.com", "ADMIN", 250);
        insertTestUser(UUID.randomUUID(), "justin_bieber", "justin@onet.com.pl", "USER", 50);

        List<User> topUsers = userJdbcDao.getTopUsersByReputation(100);

        assertThat(topUsers). hasSize(3);
    }

    @Test
    @Tag("select")
    @Tag("aggregation")
    void shouldReturnStatisticsMap_whenUserExists() {
        UUID userId = UUID.randomUUID();
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "USER", 100);

        Map<String, Object> stats = userJdbcDao.getUserStatistics(userId);

        assertThat(stats).isNotNull();
    }

    @Test
    @Tag("select")
    @Tag("aggregation")
    void shouldContainTotalCount_whenGettingStatistics() {
        UUID userId = UUID.randomUUID();
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "USER", 100);

        Map<String, Object> stats = userJdbcDao.getUserStatistics(userId);

        assertThat(stats). containsKey("total_count");
    }

    @Test
    @Tag("select")
    @Tag("aggregation")
    void shouldCalculateAverageReputation_whenGettingStatistics() {
        UUID userId = UUID.randomUUID();
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "USER", 100);

        Map<String, Object> stats = userJdbcDao.getUserStatistics(userId);

        assertThat(stats. get("avg_reputation")).isEqualTo(100.0);
    }

    @Test
    @Tag("select")
    @Tag("aggregation")
    void shouldCalculateMaxReputation_whenGettingStatistics() {
        UUID userId = UUID.randomUUID();
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "USER", 100);

        Map<String, Object> stats = userJdbcDao.getUserStatistics(userId);

        assertThat(stats.get("max_reputation")).isEqualTo(100);
    }

    @Test
    @Tag("select")
    @Tag("aggregation")
    void shouldCalculateMinReputation_whenGettingStatistics() {
        UUID userId = UUID.randomUUID();
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "USER", 100);

        Map<String, Object> stats = userJdbcDao. getUserStatistics(userId);

        assertThat(stats.get("min_reputation")).isEqualTo(100);
    }

    @Test
    @Tag("select")
    @Tag("aggregation")
    void shouldReturnCorrectCount_whenGettingTotalUserCount() {
        insertTestUser(UUID.randomUUID(), "adam_malysz", "adam@gmail.com", "USER", 100);
        insertTestUser(UUID.randomUUID(), "magda_gessler", "magdagessler@gmail.com", "ADMIN", 250);
        insertTestUser(UUID.randomUUID(), "justin_bieber", "justin@onet.com.pl", "USER", 50);

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
    @Tag("select")
    void shouldFindUsersWithFilters_whenCriteriaMatch() {
        insertTestUser(UUID.randomUUID(), "adam_malysz", "adam@gmail.com", "USER", 100);
        insertTestUser(UUID.randomUUID(), "magda_gessler", "magdagessler@gmail.com", "ADMIN", 250);
        insertTestUser(UUID.randomUUID(), "justin_bieber", "justin@onet.com.pl", "USER", 50);

        List<User> users = userJdbcDao.findUsersWithFilters("USER", 75);

        assertThat(users).hasSize(1);
    }

    @Test
    @Tag("select")
    void shouldReturnCorrectUser_whenFilteringByRoleAndReputation() {
        insertTestUser(UUID.randomUUID(), "adam_malysz", "adam@gmail.com", "USER", 100);
        insertTestUser(UUID.randomUUID(), "magda_gessler", "magdagessler@gmail.com", "ADMIN", 250);
        insertTestUser(UUID.randomUUID(), "justin_bieber", "justin@onet.com.pl", "USER", 50);

        List<User> users = userJdbcDao.findUsersWithFilters("USER", 75);

        assertThat(users. get(0).getUsername()).isEqualTo("adam_malysz");
    }

    @Test
    @Tag("select")
    void shouldOrderByReputationDesc_whenFilteringUsers() {
        insertTestUser(UUID.randomUUID(), "adam_malysz", "adam@gmail.com", "USER", 100);
        insertTestUser(UUID.randomUUID(), "magda_gessler", "magdagessler@gmail.com", "ADMIN", 250);
        insertTestUser(UUID.randomUUID(), "justin_bieber", "justin@onet.com.pl", "USER", 50);

        List<User> users = userJdbcDao.findUsersWithFilters("USER", 50);

        assertThat(users.get(0).getReputationScore()).isEqualTo(100);
    }

    @Test
    @Tag("select")
    void shouldReturnEmptyList_whenFiltersDoNotMatch() {
        insertTestUser(UUID.randomUUID(), "adam_malysz", "adam@gmail.com", "USER", 100);
        insertTestUser(UUID.randomUUID(), "magda_gessler", "magdagessler@gmail.com", "ADMIN", 250);
        insertTestUser(UUID.randomUUID(), "justin_bieber", "justin@onet.com.pl", "USER", 50);

        List<User> users = userJdbcDao.findUsersWithFilters("USER", 500);

        assertThat(users).isEmpty();
    }

    @Test
    @Tag("insert")
    @Tag("crud")
    void shouldReturnOne_whenInsertingUser() {
        User newUser = createUser("nowy_user", "nowy@gmail.com", "USER", 0);

        int rowsAffected = userJdbcDao.insertUser(newUser);

        assertThat(rowsAffected).isEqualTo(1);
    }

    @Test
    @Tag("insert")
    @Tag("crud")
    void shouldIncreaseCount_whenInsertingUser() {
        User newUser = createUser("nowy_user", "nowy@gmail.com", "USER", 0);
        int countBefore = userJdbcDao.getTotalUserCount();

        userJdbcDao. insertUser(newUser);

        int countAfter = userJdbcDao.getTotalUserCount();
        assertThat(countAfter).isEqualTo(countBefore + 1);
    }

    @Test
    @Tag("insert")
    @Tag("crud")
    void shouldPersistUsername_whenInsertingUser() {
        User newUser = createUser("nowy_user", "nowy@gmail.com", "USER", 75);

        userJdbcDao.insertUser(newUser);

        List<User> allUsers = userJdbcDao. findAllUsers();
        User inserted = allUsers.stream()
                .filter(u -> u.getUsername().equals("nowy_user"))
                .findFirst()
                .orElseThrow();
        assertThat(inserted.getUsername()).isEqualTo("nowy_user");
    }

    @Test
    @Tag("insert")
    @Tag("crud")
    void shouldPersistReputationScore_whenInsertingUser() {
        User newUser = createUser("nowy_user", "nowy@gmail.com", "USER", 75);

        userJdbcDao.insertUser(newUser);

        List<User> allUsers = userJdbcDao.findAllUsers();
        User inserted = allUsers.stream()
                . filter(u -> u.getUsername().equals("nowy_user"))
                .findFirst()
                . orElseThrow();
        assertThat(inserted.getReputationScore()).isEqualTo(75);
    }

    @Test
    @Tag("update")
    @Tag("crud")
    void shouldReturnOne_whenUpdatingExistingUser() {
        UUID userId = UUID.randomUUID();
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "USER", 100);

        Integer newReputation = 500;

        int rowsAffected = userJdbcDao.updateUserReputation(userId, newReputation);

        assertThat(rowsAffected). isEqualTo(1);
    }

    @Test
    @Tag("update")
    @Tag("crud")
    void shouldPersistNewReputation_whenUpdating() {
        UUID userId = UUID.randomUUID();
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "USER", 100);

        Integer newReputation = 500;

        userJdbcDao.updateUserReputation(userId, newReputation);

        List<User> users = userJdbcDao.findAllUsers();
        User updated = findUserById(users, userId);
        assertThat(updated.getReputationScore()).isEqualTo(500);
    }

    @Test
    @Tag("update")
    @Tag("crud")
    void shouldReturnZero_whenUpdatingNonExistentUser() {
        UUID userId = UUID.randomUUID();
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "USER", 100);

        UUID nonExistentId = UUID.randomUUID();

        int rowsAffected = userJdbcDao.updateUserReputation(nonExistentId, 100);

        assertThat(rowsAffected).isEqualTo(0);
    }

    @Test
    @Tag("update")
    @Tag("crud")
    void shouldNotAffectOtherUsers_whenUpdatingReputation() {
        UUID userId1 = UUID.randomUUID();
        insertTestUser(userId1, "adam_malysz", "adam@gmail.com", "USER", 100);
        UUID userId2 = UUID.randomUUID();
        insertTestUser(userId2, "magda_gessler", "magda@gmail.com", "ADMIN", 250);

        Integer originalReputation = 250;

        userJdbcDao.updateUserReputation(userId1, 999);

        List<User> users = userJdbcDao.findAllUsers();
        User otherUser = findUserById(users, userId2);
        assertThat(otherUser.getReputationScore()).isEqualTo(originalReputation);
    }

    @Test
    @Tag("delete")
    @Tag("crud")
    void shouldReturnOne_whenDeletingExistingUser() {
        UUID userId = UUID.randomUUID();
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "USER", 100);

        int rowsAffected = userJdbcDao.deleteUser(userId);

        assertThat(rowsAffected).isEqualTo(1);
    }

    @Test
    @Tag("delete")
    @Tag("crud")
    void shouldDecreaseCount_whenDeletingUser() {
        UUID userId = UUID.randomUUID();
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "USER", 100);

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
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "USER", 100);

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
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "USER", 100);

        UUID nonExistentId = UUID.randomUUID();

        int rowsAffected = userJdbcDao.deleteUser(nonExistentId);

        assertThat(rowsAffected). isEqualTo(0);
    }

    @Test
    @Tag("edge-case")
    void shouldHandleZeroReputation_whenInserting() {
        User zeroRepUser = createUser("zero_rep", "zero@test.com", "USER", 0);

        userJdbcDao.insertUser(zeroRepUser);

        List<User> users = userJdbcDao.findAllUsers();
        User inserted = users.stream()
                .filter(u -> u.getUsername().equals("zero_rep"))
                .findFirst()
                .orElseThrow();
        assertThat(inserted.getReputationScore()).isEqualTo(0);
    }

    @Test
    @Tag("edge-case")
    void shouldHandleNegativeReputation_whenUpdating() {
        UUID userId = UUID.randomUUID();
        insertTestUser(userId, "adam_malysz", "adam@gmail.com", "USER", 100);

        Integer negativeReputation = -50;

        userJdbcDao.updateUserReputation(userId, negativeReputation);

        List<User> users = userJdbcDao.findAllUsers();
        User updated = findUserById(users, userId);
        assertThat(updated.getReputationScore()).isEqualTo(-50);
    }

    // pomocnicze metody
    private void insertTestUser(UUID id, String username, String email, String role, Integer reputation) {
        String[] parts = username.split("_");
        String firstName = capitalize(parts[0]);
        String lastName = parts. length > 1 ? capitalize(parts[1]) : "User";

        jdbcTemplate.update(
                "INSERT INTO users (id, username, email, password, role, first_name, last_name, reputation_score) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                id. toString(), username, email, "password123",
                role, firstName, lastName, reputation
        );
    }

    private User createUser(String username, String email, String role, Integer reputation) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user. setEmail(email);
        user.setPassword("password123");
        user.setRole(role);
        user.setFirstName("Imie");
        user.setLastName("Nazwisko");
        user. setReputationScore(reputation);
        return user;
    }

    private User findUserById(List<User> users, UUID id) {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1). toLowerCase();
    }
}