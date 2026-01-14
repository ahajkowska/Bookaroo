package org.example.bookaroo.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

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

    public Integer countBooksOnShelfInYear(UUID userId, String shelfName, int year) {
        String sql = """
            SELECT COUNT(*) 
            FROM bookshelf_books bb 
            JOIN bookshelf b ON bb.bookshelf_id = b.id 
            WHERE b.user_id = ? 
              AND LOWER(b.name) = LOWER(?) 
              AND YEAR(bb.added_at) = ?
        """;

        return jdbcTemplate.queryForObject(sql, Integer.class, userId, shelfName, year);
    }

    public Integer getReadersCount(UUID bookId) {
        String sql = "SELECT COUNT(*) FROM BOOKSHELF_BOOKS WHERE book_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, bookId);
    }

    public Double getAverageRating(UUID bookId) {
        String sql = "SELECT AVG(rating) FROM reviews WHERE book_id = ?";
        return jdbcTemplate.queryForObject(sql, Double.class, bookId);
    }

    public List<Map<String, Object>> getRawRatingDistribution(UUID bookId) {
        String sql = "SELECT rating, COUNT(*) as count FROM reviews WHERE book_id = ? GROUP BY rating";
        return jdbcTemplate.queryForList(sql, bookId);
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