package org.example.bookaroo.service;

import jakarta.transaction.Transactional;
import org.example.bookaroo.dto.ReviewDTO;
import org.example.bookaroo.dto.mapper.ReviewMapper;
import org.example.bookaroo.entity.Book;
import org.example.bookaroo.entity.Review;
import org.example.bookaroo.entity.User;
import org.example.bookaroo.repository.BookRepository;
import org.example.bookaroo.repository.ReviewRepository;
import org.example.bookaroo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BookService bookService;

    public ReviewService(ReviewRepository reviewRepository, BookRepository bookRepository, UserRepository userRepository, BookService bookService) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.bookService = bookService;
    }

    @Transactional
    public void addReview(UUID userId, ReviewDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Book book = bookRepository.findById(dto.bookId())
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));

        Review review = ReviewMapper.toEntity(dto, user, book);
        reviewRepository.saveAndFlush(review);
        updateBookAverageRating(dto.bookId());
    }


    public List<ReviewDTO> getReviewsForBook(UUID bookId) {
        return reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId)
                .stream()
                .map(ReviewMapper::toDto)
                .toList();
    }

    public List<ReviewDTO> getAllReviews() {
        return reviewRepository.findAll()
                .stream()
                .map(ReviewMapper::toDto)
                .toList();
    }

    @Transactional
    public void deleteReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Recenzja nie istnieje"));

        UUID bookId = review.getBook().getId();

        reviewRepository.deleteById(reviewId);

        updateBookAverageRating(bookId);
    }

    private void updateBookAverageRating(UUID bookId) {
        List<Review> reviews = reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId);

        double newAverage = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        bookService.updateBookRating(bookId, newAverage);
    }
}