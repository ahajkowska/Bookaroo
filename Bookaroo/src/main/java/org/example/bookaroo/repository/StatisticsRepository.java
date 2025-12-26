package org.example.bookaroo.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
public class StatisticsRepository {

    private final JdbcTemplate jdbcTemplate;

    public StatisticsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> getUserStats(UUID userId) {
        Map<String, Object> stats = new HashMap<>();

        // liczba półek
        try {
            String sqlShelves = "SELECT COUNT(*) FROM bookshelf WHERE user_id = ?";
            Integer shelvesCount = jdbcTemplate.queryForObject(sqlShelves, Integer.class, userId);
            stats.put("shelvesCount", shelvesCount != null ? shelvesCount : 0);
        } catch (Exception e) {
            System.err.println("Błąd SQL (półki): " + e.getMessage());
            stats.put("shelvesCount", 0);
        }

        // łączna liczba książek
        try {
            String sqlBooks = """
                SELECT COUNT(*) 
                FROM bookshelf_books bb 
                JOIN bookshelf b ON bb.bookshelf_id = b.id 
                WHERE b.user_id = ?
            """;
            Integer booksCount = jdbcTemplate.queryForObject(sqlBooks, Integer.class, userId);
            stats.put("booksCount", booksCount != null ? booksCount : 0);
        } catch (Exception e) {
            System.err.println("Błąd SQL (książki): " + e.getMessage());
            stats.put("booksCount", 0);
        }

        return stats;
    }
}