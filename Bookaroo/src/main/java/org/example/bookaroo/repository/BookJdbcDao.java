package org.example.bookaroo.repository;

import org.example.bookaroo.entity.Book;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Repository
public class BookJdbcDao {

    private final JdbcTemplate jdbcTemplate;

    public BookJdbcDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper - mapuje wynik SQL na obiekt Book
    private final RowMapper<Book> bookRowMapper = new RowMapper<Book>() {
        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            Book book = new Book();
            book.setId(UUID.fromString(rs.getString("id")));
            book.setTitle(rs.getString("title"));
            book.setIsbn(rs.getString("isbn"));
            book.setDescription(rs.getString("description"));
            book.setPublicationYear(rs.getInt("publication_year"));
            book.setAverageRating(rs.getDouble("average_rating"));
            book.setTotalReviews(rs.getInt("total_reviews"));
            return book;
        }
    };

    // SELECT z query() i RowMapper
    public List<Book> findTopRatedBooks(int limit) {
        String sql = """
            SELECT * FROM books 
            WHERE average_rating IS NOT NULL 
            ORDER BY average_rating DESC 
            LIMIT ?
        """;
        return jdbcTemplate.query(sql, bookRowMapper, limit);
    }

    public List<Book> findBooksByPublicationYear(int year) {
        String sql = "SELECT * FROM books WHERE publication_year = ? ORDER BY title ASC";
        return jdbcTemplate.query(sql, bookRowMapper, year);
    }

    // UPDATE - aktualizowanie oceny książki
    public int updateBookRating(UUID bookId, Double newRating) {
        String sql = "UPDATE books SET average_rating = ? WHERE id = ?";
        return jdbcTemplate.update(sql, newRating, bookId.toString());
    }

}
