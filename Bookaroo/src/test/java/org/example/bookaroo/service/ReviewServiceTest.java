package org.example.bookaroo.service;

import org.example.bookaroo.dto.ReviewDTO;
import org.example.bookaroo.entity.Book;
import org.example.bookaroo.entity.Review;
import org.example.bookaroo.entity.User;
import org.example.bookaroo.repository.BookRepository;
import org.example.bookaroo.repository.ReviewRepository;
import org.example.bookaroo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookService bookService;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    @DisplayName("should save review to repository when data is valid")
    void shouldSaveReview_whenDataIsValid() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        ReviewDTO dto = new ReviewDTO(null, 5, "Great!", null, null, null, null, bookId);

        User user = new User();
        user.setId(userId);

        Book book = new Book();
        book.setId(bookId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        // pusta lista recenzji, żeby średnia się policzyła
        when(reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId)).thenReturn(List.of());

        // When
        reviewService.addReview(userId, dto);

        // Then
        verify(reviewRepository).saveAndFlush(any(Review.class));
    }

    @Test
    @DisplayName("should save review with correct content and rating")
    void shouldSaveCorrectData_whenAddingReview() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        String content = "Amazing content";
        int rating = 5;

        ReviewDTO dto = new ReviewDTO(null, rating, content, null, null, null, null, bookId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(new Book()));
        when(reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId)).thenReturn(List.of());

        // When
        reviewService.addReview(userId, dto);

        // Then
        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).saveAndFlush(reviewCaptor.capture());

        Review savedReview = reviewCaptor.getValue();
        assertThat(savedReview.getContent()).isEqualTo(content);
        assertThat(savedReview.getRating()).isEqualTo(rating);
    }

    @Test
    @DisplayName("should call updateBookRating with correct average")
    void shouldCallUpdateBookRating_withCorrectAverage() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        ReviewDTO dto = new ReviewDTO(null, 5, "New", null, null, null, null, bookId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(new Book()));

        Review r1 = new Review(); r1.setRating(4);
        Review r2 = new Review(); r2.setRating(5);
        when(reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId)).thenReturn(List.of(r1, r2));

        // When
        reviewService.addReview(userId, dto);

        // Then
        // (4 + 5) / 2 = 4.5
        verify(bookService).updateBookRating(bookId, 4.5);
    }

    @Test
    @DisplayName("should update rating to 0.0 when no reviews found")
    void shouldUpdateRatingToZero_whenListIsEmpty() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        ReviewDTO dto = new ReviewDTO(null, 5, "New", null, null, null, null, bookId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(new Book()));

        when(reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId)).thenReturn(List.of());

        // When
        reviewService.addReview(userId, dto);

        // Then
        verify(bookService).updateBookRating(bookId, 0.0);
    }

    @Test
    @DisplayName("should throw exception when user not found")
    void shouldThrowException_whenUserNotFound() {
        UUID userId = UUID.randomUUID();
        ReviewDTO dto = new ReviewDTO(null, 5, "C", null, null, null, null, UUID.randomUUID());

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.addReview(userId, dto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("should throw exception when book not found")
    void shouldThrowException_whenBookNotFound() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        ReviewDTO dto = new ReviewDTO(null, 5, "content", null, null, null, null, bookId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reviewService.addReview(userId, dto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("should return list of reviews for specific book")
    void shouldReturnReviews_whenGettingForBook() {
        // Given
        UUID bookId = UUID.randomUUID();

        User user = new User();
        user.setUsername("TestUser");

        Book book = new Book();
        book.setId(bookId);

        Review review = new Review(5, "Content", user, book);

        when(reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId)).thenReturn(List.of(review));

        // When
        List<ReviewDTO> result = reviewService.getReviewsForBook(bookId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).username()).isEqualTo("TestUser");
        assertThat(result.get(0).content()).isEqualTo("Content");
    }

    @Test
    @DisplayName("should return all reviews")
    void shouldReturnAllReviews_whenCalled() {
        // Given
        User user = new User();
        user.setUsername("User1");
        Book book = new Book();
        book.setId(UUID.randomUUID());

        Review review = new Review(5, "Content", user, book);

        when(reviewRepository.findAll()).thenReturn(List.of(review));

        // When
        List<ReviewDTO> result = reviewService.getAllReviews();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0)).isInstanceOf(ReviewDTO.class);
    }

    @Test
    @DisplayName("should call deleteById on repository")
    void shouldCallDelete_whenDeletingReview() {
        // Given
        UUID reviewId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        Book book = new Book();
        book.setId(bookId);

        Review review = new Review();
        review.setId(reviewId);
        review.setBook(book);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId)).thenReturn(List.of());

        // When
        reviewService.deleteReview(reviewId);

        // Then
        verify(reviewRepository).deleteById(reviewId);
        verify(bookService).updateBookRating(bookId, 0.0);
    }
}