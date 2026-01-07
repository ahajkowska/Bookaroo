package org.example.bookaroo.repository;

import org.example.bookaroo.entity.Author;
import org.example.bookaroo.entity.Book;
import org.example.bookaroo.entity.Review;
import org.example.bookaroo.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private Book testBook;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("reviewer");
        testUser.setEmail("test@email.com");
        testUser.setPassword("pass");
        testUser.setRole("USER");
        entityManager.persist(testUser);

        Author author = new Author();
        author.setName("John");
        author.setSurname("Doe");
        entityManager.persist(author);

        testBook = new Book();
        testBook.setTitle("Test Book");
        testBook.setIsbn("1234567890");
        testBook.setAuthor(author);
        entityManager.persist(testBook);

        entityManager.flush();
    }

    // TESTY CRUD

    @Test
    @DisplayName("should save review when all required fields are present")
    void shouldSaveReview_whenValid() {
        // Given
        Review review = new Review();
        review.setUser(testUser);
        review.setBook(testBook);
        review.setRating(5);
        review.setContent("Great!");

        // When
        Review savedReview = reviewRepository.save(review);

        // Then
        assertThat(savedReview.getId()).isNotNull();
    }

    @Test
    @DisplayName("should find review by ID")
    void shouldFindReviewById() {
        // Given
        Review review = new Review();
        review.setUser(testUser);
        review.setBook(testBook);
        review.setRating(5);
        Review persisted = entityManager.persistAndFlush(review);

        // When
        Optional<Review> found = reviewRepository.findById(persisted.getId());

        // Then
        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("should return all reviews")
    void shouldReturnAllReviews() {
        // Given
        Review r1 = new Review(5, "A", testUser, testBook);
        Review r2 = new Review(4, "B", testUser, testBook);
        entityManager.persist(r1);
        entityManager.persist(r2);
        entityManager.flush();

        // When
        List<Review> reviews = reviewRepository.findAll();

        // Then
        assertThat(reviews).hasSize(2);
    }

    @Test
    @DisplayName("should update review content")
    void shouldUpdateReviewContent() {
        // Given
        Review review = new Review(3, "Old content", testUser, testBook);
        Review persisted = entityManager.persistAndFlush(review);

        // When
        persisted.setContent("New content");
        Review updated = reviewRepository.save(persisted);

        // Then
        assertThat(updated.getContent()).isEqualTo("New content");
    }

    @Test
    @DisplayName("should delete review by ID")
    void shouldDeleteReview() {
        // Given
        Review review = new Review(5, "Content", testUser, testBook);
        Review persisted = entityManager.persistAndFlush(review);

        // When
        reviewRepository.deleteById(persisted.getId());

        // Then
        assertThat(reviewRepository.findById(persisted.getId())).isEmpty();
    }

    @Test
    @DisplayName("should return true when checking existence of saved review")
    void shouldReturnTrue_whenExistsById() {
        // Given
        Review review = new Review(5, "Content", testUser, testBook);
        Review persisted = entityManager.persistAndFlush(review);

        // When
        boolean exists = reviewRepository.existsById(persisted.getId());

        // Then
        assertThat(exists).isTrue();
    }

    // CONSTRAINTS TESTS

    @Test
    @DisplayName("should throw exception when saving review without user")
    void shouldThrowException_whenUserIsNull() {
        // Given
        Review review = new Review();
        review.setBook(testBook);
        review.setRating(5);

        // When i Then
        assertThatThrownBy(() -> reviewRepository.saveAndFlush(review))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("should throw exception when saving review without book")
    void shouldThrowException_whenBookIsNull() {
        // Given
        Review review = new Review();
        review.setUser(testUser);
        review.setRating(5);

        // When & Then
        assertThatThrownBy(() -> reviewRepository.saveAndFlush(review))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // CUSTOM QUERY TESTS

    @Test
    @DisplayName("should return only reviews for specific book")
    void shouldFilterReviewsByBookId() {
        // Given
        // Druga książka
        Book otherBook = new Book();
        otherBook.setTitle("Other");
        otherBook.setIsbn("00000");
        otherBook.setAuthor(testBook.getAuthor()); // ten sam autor
        entityManager.persist(otherBook);

        Review r1 = new Review(5, "For TestBook", testUser, testBook);
        Review r2 = new Review(1, "For OtherBook", testUser, otherBook);

        entityManager.persist(r1);
        entityManager.persist(r2);
        entityManager.flush();

        // When
        List<Review> result = reviewRepository.findByBookIdOrderByCreatedAtDesc(testBook.getId());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("For TestBook");
    }

    @Test
    @DisplayName("should order reviews by creation date descending (newest first)")
    void shouldReturnReviewsOrderedByDateDesc() {
        // Given
        Review oldReview = new Review(1, "Old", testUser, testBook);
        entityManager.persist(oldReview);

        Review newReview = new Review(5, "New", testUser, testBook);
        entityManager.persist(newReview);

        entityManager.flush();

        entityManager.getEntityManager()
                .createNativeQuery("UPDATE reviews SET created_at = :date WHERE id = :id")
                .setParameter("date", LocalDateTime.now().minusDays(1))
                .setParameter("id", oldReview.getId())
                .executeUpdate();

        entityManager.clear();

        // When
        List<Review> result = reviewRepository.findByBookIdOrderByCreatedAtDesc(testBook.getId());

        // Then
        assertThat(result)
                .extracting(Review::getContent)
                .containsExactly("New", "Old");
    }

    @Test
    @DisplayName("should return empty list when book has no reviews")
    void shouldReturnEmptyList_whenNoReviewsForBook() {
        // Given
        // testBook stworzony w setUp, nie dodajemy recenzji

        // When
        List<Review> result = reviewRepository.findByBookIdOrderByCreatedAtDesc(testBook.getId());

        // Then
        assertThat(result).isEmpty();
    }
}