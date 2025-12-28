package org.example.bookaroo.repository;

import jakarta.transaction.Transactional;
import org.example.bookaroo.entity.Book;
import org.example.bookaroo.entity.Genre;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
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

    // SELECT - wszystkie książki
    public List<Book> findAllBooks() {
        String sql = "SELECT * FROM books ORDER BY title ASC";
        return jdbcTemplate.query(sql, bookRowMapper);
    }

    // SELECT - książki danego autora
    public List<Book> findBooksByAuthorName(String authorName) {
        String sql = """
            SELECT b.* FROM books b
            JOIN authors a ON b.author_id = a.id
            WHERE LOWER(a.name) LIKE LOWER(?) OR LOWER(a.surname) LIKE LOWER(?)
            ORDER BY b.title ASC
        """;
        String searchPattern = "%" + authorName + "%";
        return jdbcTemplate.query(sql, bookRowMapper, searchPattern, searchPattern);
    }

    // SELECT - top książki według oceny
    public List<Book> findTopRatedBooks(int limit) {
        String sql = """
            SELECT * FROM books
            WHERE average_rating IS NOT NULL
            ORDER BY average_rating DESC, total_reviews DESC
            LIMIT ?
        """;
        return jdbcTemplate.query(sql, bookRowMapper, limit);
    }

    // SELECT - książki według gatunku
    public List<Book> findBooksByGenre(String genreName) {
        String sql = """
            SELECT b.* FROM books b
            JOIN book_genres bg ON b.id = bg.book_id
            JOIN genres g ON bg.genre_id = g.id
            WHERE LOWER(g.name) = LOWER(?)
            ORDER BY b.average_rating DESC
        """;
        return jdbcTemplate.query(sql, bookRowMapper, genreName);
    }

    // SELECT - wyszukiwanie pełnotekstowe (tytuł, ISBN)
    public List<Book> searchBooks(String searchTerm) {
        String sql = """
            SELECT b.* FROM books b
            LEFT JOIN authors a ON b.author_id = a.id
            WHERE LOWER(b.title) LIKE LOWER(?)
               OR LOWER(b.isbn) LIKE LOWER(?)
               OR LOWER(a.name) LIKE LOWER(?)
               OR LOWER(a.surname) LIKE LOWER(?)
            ORDER BY b.average_rating DESC NULLS LAST
        """;
        String searchPattern = "%" + searchTerm + "%";
        return jdbcTemplate.query(sql, bookRowMapper, searchPattern, searchPattern, searchPattern, searchPattern);
    }

    // SELECT - statystyki książki (liczba czytelników, rozkład ocen)
    public Map<String, Object> getBookStatistics(UUID bookId) {
        String sql = """
            SELECT 
                COUNT(r.id) as total_reviews,
                AVG(r.rating) as avg_rating,
                MAX(r.rating) as max_rating,
                MIN(r.rating) as min_rating,
                COUNT(DISTINCT bs.user_id) as total_readers
            FROM books b
            LEFT JOIN reviews r ON b.id = r.book_id
            LEFT JOIN bookshelf_books bb ON b.id = bb.book_id
            LEFT JOIN bookshelf bs ON bb.bookshelf_id = bs.id
            WHERE b.id = ?
            GROUP BY b.id
        """;
        return jdbcTemplate.queryForMap(sql, bookId.toString());
    }

    // SELECT - rozkład ocen dla książki
    public Map<Integer, Integer> getRatingDistribution(UUID bookId) {
        String sql = """
            SELECT rating, COUNT(*) as count
            FROM reviews
            WHERE book_id = ?
            GROUP BY rating
            ORDER BY rating DESC
        """;
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, bookId.toString());
        
        Map<Integer, Integer> distribution = new java.util.HashMap<>();
        for (Map<String, Object> row : results) {
            distribution.put((Integer) row.get("rating"), ((Number) row.get("count")).intValue());
        }
        return distribution;
    }

    // SELECT - liczba książek według gatunku
    public Map<String, Integer> getBookCountByGenre() {
        String sql = """
            SELECT g.name, COUNT(bg.book_id) as count
            FROM genres g
            LEFT JOIN book_genres bg ON g.id = bg.genre_id
            GROUP BY g.name
            ORDER BY count DESC
        """;
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

        Map<String, Integer> genreCounts = new java.util.HashMap<>();
        for (Map<String, Object> row : results) {
            genreCounts.put((String) row.get("name"), ((Number) row.get("count")).intValue());
        }
        return genreCounts;
    }

    // INSERT - dodaj książkę
    @Transactional
    public int insertBook(Book book) {
        // 1. Insert do tabeli books (bez genre_id)
        String sqlBook = """
            INSERT INTO books (id, title, isbn, description, cover_image_url, 
                               publication_year, language, average_rating, total_reviews, 
                               author_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        int result = jdbcTemplate.update(sqlBook,
                book.getId().toString(),
                book.getTitle(),
                book.getIsbn(),
                book.getDescription(),
                book.getPublicationYear(),
                book.getAverageRating(),
                book.getTotalReviews(),
                book.getAuthor() != null ? book.getAuthor().getId().toString() : null
        );

        if (book.getGenres() != null && !book.getGenres().isEmpty()) {
            String sqlGenre = "INSERT INTO book_genres (book_id, genre_id) VALUES (?, ?)";
            for (Genre genre : book.getGenres()) {
                jdbcTemplate.update(sqlGenre, book.getId().toString(), genre.getId().toString());
            }
        }

        return result;
    }

    @Transactional
    public void updateBookGenres(UUID bookId, List<Genre> newGenres) {
        // Najpierw usuwamy stare powiązania
        String deleteSql = "DELETE FROM book_genres WHERE book_id = ?";
        jdbcTemplate.update(deleteSql, bookId.toString());

        // Dodajemy nowe
        if (newGenres != null && !newGenres.isEmpty()) {
            String insertSql = "INSERT INTO book_genres (book_id, genre_id) VALUES (?, ?)";
            for (Genre genre : newGenres) {
                jdbcTemplate.update(insertSql, bookId.toString(), genre.getId().toString());
            }
        }
    }

    // UPDATE - aktualizowanie średniej oceny książki
    public int updateBookRating(UUID bookId, Double newAverageRating, Integer totalReviews) {
        String sql = "UPDATE books SET average_rating = ?, total_reviews = ? WHERE id = ?";
        return jdbcTemplate.update(sql, newAverageRating, totalReviews, bookId.toString());
    }

    // DELETE - usuń książkę
    public int deleteBook(UUID bookId) {
        String sql = "DELETE FROM books WHERE id = ?";
        return jdbcTemplate.update(sql, bookId.toString());
    }
}
