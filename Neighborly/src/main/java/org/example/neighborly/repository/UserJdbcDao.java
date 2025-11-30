package org.example.neighborly.repository;

import org.example.neighborly. entity.User;
import org. springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java. sql.SQLException;
import java. util.List;
import java. util.Map;
import java.util.UUID;

@Repository
public class UserJdbcDao {

    private final JdbcTemplate jdbcTemplate;

    public UserJdbcDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper - mapuje wynik sql na obiekt User
    private final RowMapper<User> userRowMapper = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(UUID.fromString(rs.getString("id")));
            user.setUsername(rs.getString("username"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setRole(rs.getString("role"));
            user.setFirstName(rs.getString("first_name"));
            user.setLastName(rs.getString("last_name"));
            user.setReputationScore(rs.getInt("reputation_score"));
            return user;
        }
    };

    // SELECT - pobierz wszystkich użytkowników
    public List<User> findAllUsers() {
        String sql = "SELECT * FROM users ORDER BY username DESC";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    // SELECT z param
    public List<User> findUsersByRole(String role) {
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY reputation_score DESC";
        return jdbcTemplate.query(sql, userRowMapper, role);
    }

    // SELECT - ranking użytkowników (top x)
    public List<User> getTopUsersByReputation(int limit) {
        String sql = "SELECT * FROM users ORDER BY reputation_score DESC LIMIT ?";
        return jdbcTemplate.query(sql, userRowMapper, limit);
    }

    // SELECT - statystyki
    public Map<String, Object> getUserStatistics(UUID userId) {
        String sql = """
            SELECT 
                COUNT(*) as total_count,
                AVG(reputation_score) as avg_reputation,
                MAX(reputation_score) as max_reputation,
                MIN(reputation_score) as min_reputation
            FROM users
            WHERE id = ?
        """;
        return jdbcTemplate.queryForMap(sql, userId. toString());
    }

    // SELECT - agregacja (count)
    public Integer getTotalUserCount() {
        String sql = "SELECT COUNT(*) FROM users";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    // INSERT - dodaj użytkownika
    public int insertUser(User user) {
        String sql = """
            INSERT INTO users (id, username, email, password, role, first_name, last_name, reputation_score)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        return jdbcTemplate.update(sql,
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user. getPassword(),
                user.getRole(),
                user.getFirstName(),
                user.getLastName(),
                user.getReputationScore()
        );
    }

    // UPDATE - aktualizuj reputację
    public int updateUserReputation(UUID userId, Integer newReputation) {
        String sql = "UPDATE users SET reputation_score = ?  WHERE id = ?";
        return jdbcTemplate.update(sql, newReputation, userId. toString());
    }

    // DELETE - usuń użytkownika
    public int deleteUser(UUID userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        return jdbcTemplate.update(sql, userId.toString());
    }

    // Złożone zapytanie - użytkownicy z filtrem
    public List<User> findUsersWithFilters(String role, Integer minReputation) {
        String sql = """
            SELECT * FROM users 
            WHERE role = ? AND reputation_score >= ?
            ORDER BY reputation_score DESC
        """;
        return jdbcTemplate.query(sql, userRowMapper, role, minReputation);
    }
}