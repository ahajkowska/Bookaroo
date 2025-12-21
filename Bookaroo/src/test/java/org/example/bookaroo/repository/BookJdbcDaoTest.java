package org.example.bookaroo.repository;

import org.example.bookaroo.entity.Author;
import org.example.bookaroo.entity.Book;
import org.example.bookaroo.entity.Genre;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// @JdbcTest - czym sie rozni od datajpatest?
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(BookJdbcDao.class)
//@TestPropertySource(properties = {
//        "spring.sql.init.mode=never",
//        "spring.jpa.hibernate.ddl-auto=create-drop"
//})
@Tag("repository")
@Tag("jdbc")
class BookJdbcDaoTest {

    @Autowired
    private BookJdbcDao bookJdbcDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID authorId1;
    private UUID authorId2;
    private UUID genreId1;
    private UUID genreId2;
    private UUID bookId1;
    private UUID bookId2;
    private UUID bookId3;

    @BeforeEach
    void setUp() {
        // Przygotowanie danych testowych
        authorId1 = UUID.randomUUID();
        authorId2 = UUID.randomUUID();
        genreId1 = UUID.randomUUID();
        genreId2 = UUID.randomUUID();

        // Insert authors
        insertTestAuthor(authorId1, "J.K.", "Rowling");
        insertTestAuthor(authorId2, "George", "Orwell");

        // Insert genres
        insertTestGenre(genreId1, "Fantasy");
        insertTestGenre(genreId2, "Dystopian");

        // Insert books
        bookId1 = UUID.randomUUID();
        bookId2 = UUID.randomUUID();
        bookId3 = UUID.randomUUID();

        insertTestBook(bookId1, "Harry Potter and the Philosopher's Stone", "978-0-7475-3269-9",
                authorId1, genreId1, 1997, 4.5, 100);
        insertTestBook(bookId2, "1984", "978-0-452-28423-4",
                authorId2, genreId2, 1949, 4.8, 200);
        insertTestBook(bookId3, "Animal Farm", "978-0-452-28424-1",
                authorId2, genreId2, 1945, 4.3, 50);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM reviews");
        jdbcTemplate.execute("DELETE FROM bookshelf_books");
        jdbcTemplate.execute("DELETE FROM books");
        jdbcTemplate.execute("DELETE FROM authors");
        jdbcTemplate.execute("DELETE FROM genres");
    }

    // ==================== TESTY SELECT ====================

    @Test
    void shouldReturnAllBooks_whenFindingAllBooks() {
        // when
        List<Book> books = bookJdbcDao.findAllBooks();

        // then
        assertThat(books).hasSize(3);
        assertThat(books).extracting(Book::getTitle)
                .containsExactlyInAnyOrder(
                        "Harry Potter and the Philosopher's Stone",
                        "1984",
                        "Animal Farm"
                );
    }

    @Test
    void shouldReturnBooksOrderedByTitle_whenFindingAllBooks() {
        // when
        List<Book> books = bookJdbcDao.findAllBooks();

        // then
        assertThat(books).hasSize(3);
        assertThat(books.get(0).getTitle()).isEqualTo("1984");
        assertThat(books.get(1).getTitle()).isEqualTo("Animal Farm");
        assertThat(books.get(2).getTitle()).isEqualTo("Harry Potter and the Philosopher's Stone");
    }

    @Test
    void shouldReturnBooksByAuthor_whenSearchingByAuthorName() {
        // when
        List<Book> books = bookJdbcDao.findBooksByAuthorName("Orwell");

        // then
        assertThat(books).hasSize(2);
        assertThat(books).extracting(Book::getTitle)
                .containsExactlyInAnyOrder("1984", "Animal Farm");
    }

    @Test
    void shouldReturnBooksByAuthorFirstName_whenSearchingByPartialName() {
        // when
        List<Book> books = bookJdbcDao.findBooksByAuthorName("George");

        // then
        assertThat(books).hasSize(2);
        assertThat(books).allMatch(book ->
                book.getTitle().equals("1984") || book.getTitle().equals("Animal Farm")
        );
    }

    @Test
    void shouldReturnEmptyList_whenAuthorNotFound() {
        // when
        List<Book> books = bookJdbcDao.findBooksByAuthorName("Nonexistent Author");

        // then
        assertThat(books).isEmpty();
    }

    @Test
    void shouldReturnTopRatedBooks_whenGettingTopBooks() {
        // when
        List<Book> topBooks = bookJdbcDao.findTopRatedBooks(2);

        // then
        assertThat(topBooks).hasSize(2);
        assertThat(topBooks.get(0).getTitle()).isEqualTo("1984"); // 4.8
        assertThat(topBooks.get(1).getTitle()).isEqualTo("Harry Potter and the Philosopher's Stone"); // 4.5
        assertThat(topBooks.get(0).getAverageRating()).isGreaterThan(topBooks.get(1).getAverageRating());
    }

    @Test
    void shouldReturnAllBooks_whenLimitExceedsCount() {
        // when
        List<Book> topBooks = bookJdbcDao.findTopRatedBooks(10);

        // then
        assertThat(topBooks).hasSize(3);
    }

    @Test
    void shouldReturnBooksByGenre_whenSearchingByGenreName() {
        // when
        List<Book> books = bookJdbcDao.findBooksByGenre("Dystopian");

        // then
        assertThat(books).hasSize(2);
        assertThat(books).extracting(Book::getTitle)
                .containsExactlyInAnyOrder("1984", "Animal Farm");
    }

    @Test
    void shouldReturnBooksOrderedByRating_whenSearchingByGenre() {
        // when
        List<Book> books = bookJdbcDao.findBooksByGenre("Dystopian");

        // then
        assertThat(books).hasSize(2);
        assertThat(books.get(0).getTitle()).isEqualTo("1984"); // wyższa ocena
        assertThat(books.get(0).getAverageRating()).isGreaterThan(books.get(1).getAverageRating());
    }

    @Test
    void shouldReturnEmptyList_whenGenreNotFound() {
        // when
        List<Book> books = bookJdbcDao.findBooksByGenre("Romance");

        // then
        assertThat(books).isEmpty();
    }

    @Test
    void shouldFindBooksByTitle_whenSearching() {
        // when
        List<Book> books = bookJdbcDao.searchBooks("Harry Potter");

        // then
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getTitle()).contains("Harry Potter");
    }

    @Test
    void shouldFindBooksByISBN_whenSearching() {
        // when
        List<Book> books = bookJdbcDao.searchBooks("978-0-452-28423-4");

        // then
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getIsbn()).isEqualTo("978-0-452-28423-4");
    }

    @Test
    void shouldFindBooksByAuthor_whenSearching() {
        // when
        List<Book> books = bookJdbcDao.searchBooks("Rowling");

        // then
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getTitle()).contains("Harry Potter");
    }

    @Test
    void shouldReturnEmptyList_whenSearchTermNotFound() {
        // when
        List<Book> books = bookJdbcDao.searchBooks("Nonexistent");

        // then
        assertThat(books).isEmpty();
    }

    @Test
    void shouldReturnBookStatistics_whenGettingStatistics() {
        // given
        insertTestReview(bookId1, 5);
        insertTestReview(bookId1, 4);
        insertTestReview(bookId1, 5);

        // when
        Map<String, Object> stats = bookJdbcDao.getBookStatistics(bookId1);

        // then
        assertThat(stats).isNotNull();
        assertThat(stats.get("total_reviews")).isEqualTo(3L);
        assertThat((Double) stats.get("avg_rating")).isBetween(4.0, 5.0);
    }

    @Test
    void shouldReturnRatingDistribution_whenGettingDistribution() {
        // given
        insertTestReview(bookId1, 5);
        insertTestReview(bookId1, 5);
        insertTestReview(bookId1, 4);
        insertTestReview(bookId1, 3);

        // when
        Map<Integer, Integer> distribution = bookJdbcDao.getRatingDistribution(bookId1);

        // then
        assertThat(distribution).hasSize(3);
        assertThat(distribution.get(5)).isEqualTo(2);
        assertThat(distribution.get(4)).isEqualTo(1);
        assertThat(distribution.get(3)).isEqualTo(1);
    }

    @Test
    void shouldReturnTotalBookCount() {
        // when
        Integer count = bookJdbcDao.getTotalBookCount();

        // then
        assertThat(count).isEqualTo(3);
    }

    @Test
    void shouldReturnBookCountByGenre() {
        // when
        Map<String, Integer> genreCounts = bookJdbcDao.getBookCountByGenre();

        // then
        assertThat(genreCounts).hasSize(2);
        assertThat(genreCounts.get("Dystopian")).isEqualTo(2);
        assertThat(genreCounts.get("Fantasy")).isEqualTo(1);
    }

    // ==================== TESTY INSERT/UPDATE/DELETE ====================

    @Test
    void shouldInsertBook_whenCreatingNewBook() {
        // given
        Book newBook = createBook("The Hobbit", "978-0-547-92822-7", authorId1, genreId1, 1937);

        // when
        int rowsAffected = bookJdbcDao.insertBook(newBook);

        // then
        assertThat(rowsAffected).isEqualTo(1);
        List<Book> allBooks = bookJdbcDao.findAllBooks();
        assertThat(allBooks).hasSize(4);
        assertThat(allBooks).extracting(Book::getTitle).contains("The Hobbit");
    }

    @Test
    void shouldUpdateBookRating_whenRatingChanges() {
        // when
        int rowsAffected = bookJdbcDao.updateBookRating(bookId1, 4.7, 150);

        // then
        assertThat(rowsAffected).isEqualTo(1);

        Book updatedBook = jdbcTemplate.queryForObject(
                "SELECT * FROM books WHERE id = ?",
                (rs, rowNum) -> {
                    Book book = new Book();
                    book.setAverageRating(rs.getDouble("average_rating"));
                    book.setTotalReviews(rs.getInt("total_reviews"));
                    return book;
                },
                bookId1.toString()
        );

        assertThat(updatedBook.getAverageRating()).isEqualTo(4.7);
        assertThat(updatedBook.getTotalReviews()).isEqualTo(150);
    }

    @Test
    void shouldReturnZero_whenUpdatingNonexistentBook() {
        // when
        int rowsAffected = bookJdbcDao.updateBookRating(UUID.randomUUID(), 5.0, 10);

        // then
        assertThat(rowsAffected).isEqualTo(0);
    }

    @Test
    void shouldDeleteBook_whenBookExists() {
        // when
        int rowsAffected = bookJdbcDao.deleteBook(bookId1);

        // then
        assertThat(rowsAffected).isEqualTo(1);
        List<Book> remainingBooks = bookJdbcDao.findAllBooks();
        assertThat(remainingBooks).hasSize(2);
        assertThat(remainingBooks).extracting(Book::getTitle)
                .doesNotContain("Harry Potter and the Philosopher's Stone");
    }

    @Test
    void shouldReturnZero_whenDeletingNonexistentBook() {
        // when
        int rowsAffected = bookJdbcDao.deleteBook(UUID.randomUUID());

        // then
        assertThat(rowsAffected).isEqualTo(0);
    }

    // ==================== METODY POMOCNICZE ====================

    private void insertTestAuthor(UUID id, String name, String surname) {
        jdbcTemplate.update(
                "INSERT INTO authors (id, name, surname) VALUES (?, ?, ?)",
                id.toString(), name, surname
        );
    }

    private void insertTestGenre(UUID id, String name) {
        jdbcTemplate.update(
                "INSERT INTO genres (id, name) VALUES (?, ?)",
                id.toString(), name
        );
    }

    private void insertTestBook(UUID id, String title, String isbn, UUID authorId,
                                UUID genreId, int year, Double rating, int reviews) {
        jdbcTemplate.update(
                """
                INSERT INTO books (id, title, isbn, description, cover_image_url, 
                                  publication_year, language, average_rating, total_reviews, 
                                  author_id, genre_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id.toString(), title, isbn, "Test description", null,
                year, "English", rating, reviews, authorId.toString(), genreId.toString()
        );
    }

    private void insertTestReview(UUID bookId, int rating) {
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        // Insert test user first
        jdbcTemplate.update(
                "INSERT INTO users (id, username, email, password, role) VALUES (?, ?, ?, ?, ?)",
                userId.toString(), "testuser" + reviewId, "test@email.com", "pass", "USER"
        );

        // Insert review
        jdbcTemplate.update(
                "INSERT INTO reviews (id, rating, review_text, reviewer_id, book_id) VALUES (?, ?, ?, ?, ?)",
                reviewId.toString(), rating, "Test review", userId.toString(), bookId.toString()
        );
    }

    private Book createBook(String title, String isbn, UUID authorId, UUID genreId, int year) {
        Book book = new Book();
        book.setId(UUID.randomUUID());
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setDescription("Test description");
        book.setPublicationYear(year);
        book.setLanguage("English");
        book.setAverageRating(0.0);
        book.setTotalReviews(0);

        Author author = new Author();
        author.setId(authorId);
        book.setAuthor(author);

        Genre genre = new Genre();
        genre.setId(genreId);
        book.setGenre(genre);

        return book;
    }
}