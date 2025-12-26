package org.example.bookaroo.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
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

    public Map<String, Object> getBookStats(UUID bookId) {
        Map<String, Object> stats = new HashMap<>();

        Map<Integer, Integer> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 10; i++) {
            ratingDistribution.put(i, 0);
        }
        stats.put("ratingDistribution", ratingDistribution);
        stats.put("avgRating", 0.0);
        stats.put("readersCount", 0);

        // liczba czytelników
        try {
            String sqlReaders = "SELECT COUNT(*) FROM BOOKSHELF_BOOKS WHERE book_id = ?";
            Integer count = jdbcTemplate.queryForObject(sqlReaders, Integer.class, bookId);
            if (count != null) stats.put("readersCount", count);
        } catch (Exception e) {
            System.err.println("Błąd SQL (readers): " + e.getMessage());
        }

        // średnia ocena
        try {
            String sqlAvg = "SELECT AVG(rating) FROM reviews WHERE book_id = ?";
            Double avg = jdbcTemplate.queryForObject(sqlAvg, Double.class, bookId);
            if (avg != null) stats.put("avgRating", avg);
        } catch (Exception e) {
        }

        // rozkład ocen
        try {
            String sqlDist = "SELECT rating, COUNT(*) as count FROM reviews WHERE book_id = ? GROUP BY rating";
            jdbcTemplate.query(sqlDist, (rs) -> {
                int r = rs.getInt("rating");
                int c = rs.getInt("count");
                if (r >= 1 && r <= 10) ratingDistribution.put(r, c);
            }, bookId);
            stats.put("ratingDistribution", ratingDistribution);
        } catch (Exception e) {
            System.err.println("Błąd SQL (dist): " + e.getMessage());
        }

        return stats;
    }

    public Map<UUID, Double> getAllBookAverageRatings() {
        String sql = "SELECT book_id, AVG(CAST(rating AS FLOAT)) as avg_rating FROM reviews GROUP BY book_id";

        return jdbcTemplate.query(sql, (ResultSet rs) -> {
            Map<UUID, Double> map = new HashMap<>();
            while (rs.next()) {
                try {
                    UUID bookId = UUID.fromString(rs.getString("book_id"));
                    Double avg = rs.getDouble("avg_rating");
                    map.put(bookId, avg);
                } catch (Exception e) {
                }
            }
            return map;
        });
    }
}