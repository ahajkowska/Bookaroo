package org.example.bookaroo.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(StatisticsRepository.class)
class StatisticsRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StatisticsRepository statisticsRepository;

    private UUID userId;
    private UUID bookId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        bookId = UUID.randomUUID();
        createSchema();
    }

    @Test
    @DisplayName("should return 0 read count when user has no shelves")
    void shouldReturnZero_whenNoShelves() {
        // When
        Map<String, Object> stats = statisticsRepository.getUserStats(userId);

        // Then
        assertThat(stats.get("readCount")).isEqualTo(0);
    }

    @Test
    @DisplayName("should return correct year in user stats")
    void shouldReturnCorrectYear_inUserStats() {
        // When
        Map<String, Object> stats = statisticsRepository.getUserStats(userId);

        // Then
        assertThat(stats.get("currentYear")).isEqualTo(LocalDate.now().getYear());
    }

    @Test
    @DisplayName("should return correct count for books read this year")
    void shouldReturnCount_forBooksReadThisYear() {
        // Given
        UUID shelfId = UUID.randomUUID();
        insertUser(userId);
        insertShelf(shelfId, userId, "Przeczytane");
        insertBookshelfBook(shelfId, UUID.randomUUID(), LocalDate.now());
        insertBookshelfBook(shelfId, UUID.randomUUID(), LocalDate.now().minusMonths(1));

        // When
        Map<String, Object> stats = statisticsRepository.getUserStats(userId);

        // Then
        assertThat(stats.get("readCount")).isEqualTo(2);
    }

    @Test
    @DisplayName("should ignore books read in previous years")
    void shouldIgnoreBooks_fromPreviousYears() {
        // Given
        UUID shelfId = UUID.randomUUID();
        insertUser(userId);
        insertShelf(shelfId, userId, "Przeczytane");
        insertBookshelfBook(shelfId, UUID.randomUUID(), LocalDate.now().minusYears(1));

        // When
        Map<String, Object> stats = statisticsRepository.getUserStats(userId);

        // Then
        assertThat(stats.get("readCount")).isEqualTo(0);
    }

    @Test
    @DisplayName("should ignore books on different shelves")
    void shouldIgnoreBooks_onDifferentShelves() {
        // Given
        UUID shelfId = UUID.randomUUID();
        insertUser(userId);
        insertShelf(shelfId, userId, "Chcę przeczytać");
        insertBookshelfBook(shelfId, UUID.randomUUID(), LocalDate.now());

        // When
        Map<String, Object> stats = statisticsRepository.getUserStats(userId);

        // Then
        assertThat(stats.get("readCount")).isEqualTo(0);
    }

    @Test
    @DisplayName("should handle case insensitive shelf name correctly")
    void shouldHandleCaseInsensitiveShelfName() {
        // Given
        UUID shelfId = UUID.randomUUID();
        insertUser(userId);
        insertShelf(shelfId, userId, "PRZECZYTANE");
        insertBookshelfBook(shelfId, UUID.randomUUID(), LocalDate.now());

        // When
        Map<String, Object> stats = statisticsRepository.getUserStats(userId);

        // Then
        assertThat(stats.get("readCount")).isEqualTo(1);
    }

    // TESTY BOOK STATS

    @Test
    @DisplayName("should return 0 stats for non-existent book")
    void shouldReturnZeroStats_forNonExistentBook() {
        // When
        Map<String, Object> stats = statisticsRepository.getBookStats(UUID.randomUUID());

        // Then
        assertThat(stats.get("avgRating")).isEqualTo(0.0);
        assertThat(stats.get("readersCount")).isEqualTo(0);

        Map<Integer, Integer> dist = (Map<Integer, Integer>) stats.get("ratingDistribution");
        assertThat(dist).containsEntry(5, 0);
    }

    @Test
    @DisplayName("should count readers correctly")
    void shouldCountReadersCorrectly() {
        // Given
        UUID shelfId1 = UUID.randomUUID();
        UUID shelfId2 = UUID.randomUUID();
        insertBookshelfBook(shelfId1, bookId, LocalDate.now());
        insertBookshelfBook(shelfId2, bookId, LocalDate.now());

        // When
        Map<String, Object> stats = statisticsRepository.getBookStats(bookId);

        // Then
        assertThat(stats.get("readersCount")).isEqualTo(2);
    }

    @Test
    @DisplayName("should calculate average rating correctly")
    void shouldCalculateAvgRating() {
        // Given
        insertReview(bookId, 5);
        insertReview(bookId, 3); // (5+3)/2 = 4.0

        // When
        Map<String, Object> stats = statisticsRepository.getBookStats(bookId);

        // Then
        assertThat(stats.get("avgRating")).isEqualTo(4.0);
    }

    @Test
    @DisplayName("should build correct rating distribution map")
    void shouldBuildRatingDistribution() {
        // Given
        insertReview(bookId, 5);
        insertReview(bookId, 5);
        insertReview(bookId, 1);

        // When
        Map<String, Object> stats = statisticsRepository.getBookStats(bookId);

        Map<Integer, Integer> dist = (Map<Integer, Integer>) stats.get("ratingDistribution");

        // Then
        assertThat(dist).containsEntry(5, 2);
        assertThat(dist).containsEntry(1, 1);
        assertThat(dist).containsEntry(3, 0);
    }

    @Test
    @DisplayName("should ignore ratings out of range (1-10) in distribution")
    void shouldIgnoreInvalidRatingsInDistribution() {
        // Given
        insertReview(bookId, 11);
        insertReview(bookId, 5);

        // When
        Map<String, Object> stats = statisticsRepository.getBookStats(bookId);
        Map<Integer, Integer> dist = (Map<Integer, Integer>) stats.get("ratingDistribution");

        // Then
        assertThat(dist).containsEntry(5, 1);
        assertThat(dist).doesNotContainKey(11);
    }

    // TEST ALL BOOKS AVG

    @Test
    @DisplayName("should return average ratings for all books")
    void shouldReturnAvgForMultipleBooks() {
        // Given
        UUID book1 = UUID.randomUUID();
        UUID book2 = UUID.randomUUID();

        insertReview(book1, 5);
        insertReview(book1, 5); // 5.0
        insertReview(book2, 2);
        insertReview(book2, 4); // 3.0

        // When
        Map<UUID, Double> result = statisticsRepository.getAllBookAverageRatings();

        // Then
        assertThat(result).containsEntry(book1, 5.0);
        assertThat(result).containsEntry(book2, 3.0);
    }

    // m. pomocnicze

    private void createSchema() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS bookshelf_books");
        jdbcTemplate.execute("DROP TABLE IF EXISTS bookshelf");
        jdbcTemplate.execute("DROP TABLE IF EXISTS reviews");
        jdbcTemplate.execute("DROP TABLE IF EXISTS users");

        jdbcTemplate.execute("CREATE TABLE users (id UUID PRIMARY KEY, username VARCHAR(255))");
        jdbcTemplate.execute("CREATE TABLE bookshelf (id UUID PRIMARY KEY, user_id UUID, name VARCHAR(255))");
        jdbcTemplate.execute("CREATE TABLE bookshelf_books (bookshelf_id UUID, book_id UUID, added_at TIMESTAMP)");
        jdbcTemplate.execute("CREATE TABLE reviews (id UUID PRIMARY KEY, book_id UUID, rating INT)");
    }

    private void insertUser(UUID id) {
        jdbcTemplate.update("INSERT INTO users (id, username) VALUES (?, ?)", id, "testUser");
    }

    private void insertShelf(UUID id, UUID userId, String name) {
        jdbcTemplate.update("INSERT INTO bookshelf (id, user_id, name) VALUES (?, ?, ?)", id, userId, name);
    }

    private void insertBookshelfBook(UUID shelfId, UUID bId, LocalDate addedAt) {
        jdbcTemplate.update("INSERT INTO bookshelf_books (bookshelf_id, book_id, added_at) VALUES (?, ?, ?)",
                shelfId, bId, java.sql.Timestamp.valueOf(addedAt.atStartOfDay()));
    }

    private void insertReview(UUID bId, int rating) {
        jdbcTemplate.update("INSERT INTO reviews (id, book_id, rating) VALUES (?, ?, ?)",
                UUID.randomUUID(), bId, rating);
    }
}