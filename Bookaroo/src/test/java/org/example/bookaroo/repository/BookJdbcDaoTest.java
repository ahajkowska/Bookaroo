package org.example.bookaroo.repository;

import org.example.bookaroo.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(BookJdbcDao.class)
class BookJdbcDaoTest {

    @Autowired
    private BookJdbcDao bookJdbcDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS books CASCADE");

        jdbcTemplate.execute("""
            CREATE TABLE books (
                id VARCHAR(36) PRIMARY KEY,
                title VARCHAR(255),
                isbn VARCHAR(255),
                description VARCHAR(255),
                publication_year INT,
                average_rating DOUBLE,
                total_reviews INT
            )
        """);
    }

    //ROW MAPPER & SELECT ALL FIELDS

    @Test
    @DisplayName("should map all columns correctly from ResultSet to Book object")
    void shouldMapAllFieldsCorrectly() {
        UUID id = UUID.randomUUID();
        insertBook(id, "Title", "ISBN", "Desc", 2020, 4.5, 10);

        List<Book> result = bookJdbcDao.findTopRatedBooks(1);

        // Then
        Book book = result.get(0);
        assertThat(book.getId()).isEqualTo(id);
        assertThat(book.getTitle()).isEqualTo("Title");
        assertThat(book.getIsbn()).isEqualTo("ISBN");
        assertThat(book.getDescription()).isEqualTo("Desc");
        assertThat(book.getPublicationYear()).isEqualTo(2020);
        assertThat(book.getAverageRating()).isEqualTo(4.5);
        assertThat(book.getTotalReviews()).isEqualTo(10);
    }
    
    @Test
    @DisplayName("Powinno zwrócić książki uporządkowane według średniej oceny DESC")
    void shouldReturnBooksOrderedByRating() {
        insertBook(UUID.randomUUID(), "Weak", "1", "d", 2000, 2.0, 1);
        insertBook(UUID.randomUUID(), "Best", "2", "d", 2000, 5.0, 1);
        insertBook(UUID.randomUUID(), "Good", "3", "d", 2000, 4.0, 1);

        List<Book> result = bookJdbcDao.findTopRatedBooks(3);

        assertThat(result).extracting(Book::getAverageRating)
                .containsExactly(5.0, 4.0, 2.0);
    }

    @Test
    @DisplayName("Ograniczenie liczby wyników")
    void shouldLimitResults() {
        insertBook(UUID.randomUUID(), "A", "1", "d", 2000, 5.0, 1);
        insertBook(UUID.randomUUID(), "B", "2", "d", 2000, 4.0, 1);
        insertBook(UUID.randomUUID(), "C", "3", "d", 2000, 3.0, 1);

        List<Book> result = bookJdbcDao.findTopRatedBooks(2);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Zignorowanie książek z NULL rating w top ocenach")
    void shouldIgnoreNullRatings() {
        insertBook(UUID.randomUUID(), "Rated", "1", "d", 2000, 5.0, 1);
        insertBook(UUID.randomUUID(), "Unrated", "2", "d", 2000, null, 0);

        List<Book> result = bookJdbcDao.findTopRatedBooks(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Rated");
    }

    @Test
    @DisplayName("Zwrócenie pustej listy jeśli nie ma książek")
    void shouldReturnEmpty_whenNoBooks() {
        List<Book> result = bookJdbcDao.findTopRatedBooks(5);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Wyszukanie książek po roku")
    void shouldFindBooksByYear() {
        insertBook(UUID.randomUUID(), "Stare", "1", "d", 1990, 4.0, 0);
        insertBook(UUID.randomUUID(), "Cel", "2", "d", 2000, 4.0, 0);

        List<Book> result = bookJdbcDao.findBooksByPublicationYear(2000);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Cel");
    }

    @Test
    @DisplayName("Książki powinny być uporządkowane według tytułu ASC gdy są wyszukiwane po roku")
    void shouldOrderBooksByTitleAsc() {
        insertBook(UUID.randomUUID(), "Zebra", "1", "d", 2024, 4.0, 0);
        insertBook(UUID.randomUUID(), "Alfa", "2", "d", 2024, 4.0, 0);

        List<Book> result = bookJdbcDao.findBooksByPublicationYear(2024);

        assertThat(result).extracting(Book::getTitle)
                .containsExactly("Alfa", "Zebra");
    }

    @Test
    @DisplayName("Zwrócenie pustej listy gdy nie pasuje do żadnej książki wybrany rok")
    void shouldReturnEmpty_whenYearNotFound() {
        insertBook(UUID.randomUUID(), "A", "1", "d", 2020, 4.0, 0);

        List<Book> result = bookJdbcDao.findBooksByPublicationYear(1900);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Aktualizacja oceny dla istniejącej książki")
    void shouldUpdateRating() {
        UUID id = UUID.randomUUID();
        insertBook(id, "Title", "ISBN", "Desc", 2020, 3.0, 5);

        int rowsAffected = bookJdbcDao.updateBookRating(id, 4.8);

        assertThat(rowsAffected).isEqualTo(1);

        Double newRating = jdbcTemplate.queryForObject(
                "SELECT average_rating FROM books WHERE id = ?", Double.class, id.toString());
        assertThat(newRating).isEqualTo(4.8);
    }

    @Test
    @DisplayName("Zwrócenie 0 rows affected podczas aktualizacji non-existent book")
    void shouldReturnZero_whenBookNotFoundForUpdate() {
        int rowsAffected = bookJdbcDao.updateBookRating(UUID.randomUUID(), 5.0);

        assertThat(rowsAffected).isZero();
    }

    // m. pomocnicza

    private void insertBook(UUID id, String title, String isbn, String desc, int year, Double rating, int reviews) {
        jdbcTemplate.update("""
            INSERT INTO books (id, title, isbn, description, publication_year, average_rating, total_reviews)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """, id.toString(), title, isbn, desc, year, rating, reviews);
    }
}