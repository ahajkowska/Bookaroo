package org.example.bookaroo.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
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

        // reading challenge
        try {
            int currentYear = java.time.LocalDate.now().getYear();

            String sqlRead = """
                SELECT COUNT(*) 
                FROM bookshelf_books bb 
                JOIN bookshelf b ON bb.bookshelf_id = b.id 
                WHERE b.user_id = ? 
                  AND LOWER(b.name) = 'przeczytane'
                  AND YEAR(bb.added_at) = ?
            """;

            Integer readCount = jdbcTemplate.queryForObject(sqlRead, Integer.class, userId, currentYear);

            stats.put("readCount", readCount != null ? readCount : 0);
            stats.put("currentYear", currentYear); // rok do widoku

        } catch (Exception e) {
            System.err.println("Błąd SQL (Reading Challenge): " + e.getMessage());
            stats.put("readCount", 0);
            stats.put("currentYear", java.time.LocalDate.now().getYear());
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

        RowMapper<Map.Entry<UUID, Double>> mapper = (rs, rowNum) -> {
            UUID id = UUID.fromString(rs.getString("book_id"));
            Double avg = rs.getDouble("avg_rating");
            return Map.entry(id, avg);
        };

        List<Map.Entry<UUID, Double>> list = jdbcTemplate.query(sql, mapper);

        Map<UUID, Double> result = new HashMap<>();
        for (var entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}