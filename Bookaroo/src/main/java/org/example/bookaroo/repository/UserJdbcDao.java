package org.example.bookaroo.repository;

import org.example.bookaroo. entity.User;
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
            user.setAvatar(rs.getString("avatar"));
            user.setBio(rs.getString("bio"));
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
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY username ASC";
        return jdbcTemplate.query(sql, userRowMapper, role);
    }



    // SELECT - agregacja (count)
    public Integer getTotalUserCount() {
        String sql = "SELECT COUNT(*) FROM users";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    // INSERT - dodaj użytkownika
    public int insertUser(User user) {
        String sql = """
            INSERT INTO users (id, username, email, password, role, avatar, bio)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        return jdbcTemplate.update(sql,
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                user.getAvatar(),
                user.getBio()
        );
    }

    // DELETE - usuń użytkownika
    public int deleteUser(UUID userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        return jdbcTemplate.update(sql, userId.toString());
    }
}