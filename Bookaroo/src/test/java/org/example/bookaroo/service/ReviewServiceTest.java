package org.example.bookaroo.service;

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
        User user = new User();
        Book book = new Book();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId)).thenReturn(List.of());

        // When
        reviewService.addReview(userId, bookId, 5, "Great book!");

        // Then
        verify(reviewRepository).saveAndFlush(any(Review.class));
    }

    @Test
    @DisplayName("should save review with correct content and rating")
    void shouldSaveCorrectData_whenAddingReview() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        User user = new User();
        Book book = new Book();
        String content = "Amazing content";
        int rating = 5;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId)).thenReturn(List.of());

        // When
        reviewService.addReview(userId, bookId, rating, content);

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

        Review r1 = new Review(); r1.setRating(4);
        Review r2 = new Review(); r2.setRating(5);
        List<Review> mockReviews = List.of(r1, r2);

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(new Book()));
        when(reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId)).thenReturn(mockReviews);

        // When
        reviewService.addReview(userId, bookId, 5, "New review");

        // Then
        verify(bookService).updateBookRating(bookId, 4.5);
    }

    @Test
    @DisplayName("should update rating to 0.0 when no reviews found")
    void shouldUpdateRatingToZero_whenListIsEmpty() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(new Book()));
        when(reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId)).thenReturn(List.of());

        // When
        reviewService.addReview(userId, bookId, 5, "content");

        // Then
        verify(bookService).updateBookRating(bookId, 0.0);
    }

    @Test
    @DisplayName("should throw exception when user not found")
    void shouldThrowException_whenUserNotFound() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reviewService.addReview(userId, bookId, 5, "content"))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("should throw exception when book not found")
    void shouldThrowException_whenBookNotFound() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reviewService.addReview(userId, bookId, 5, "content"))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("should return list of reviews for specific book")
    void shouldReturnReviews_whenGettingForBook() {
        // Given
        UUID bookId = UUID.randomUUID();
        List<Review> reviews = List.of(new Review(), new Review());
        when(reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId)).thenReturn(reviews);

        // When
        List<Review> result = reviewService.getReviewsForBook(bookId);

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("should return all reviews")
    void shouldReturnAllReviews_whenCalled() {
        // Given
        List<Review> reviews = List.of(new Review());
        when(reviewRepository.findAll()).thenReturn(reviews);

        // When
        List<Review> result = reviewService.getAllReviews();

        // Then
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("should call deleteById on repository")
    void shouldCallDelete_whenDeletingReview() {
        // Given
        UUID reviewId = UUID.randomUUID();

        // When
        reviewService.deleteReview(reviewId);

        // Then
        verify(reviewRepository).deleteById(reviewId);
    }
}