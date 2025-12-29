package org.example.bookaroo.service;

import jakarta.transaction.Transactional;
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
    public void addReview(UUID userId, UUID bookId, int rating, String content) {
        User user = userRepository.findById(userId).orElseThrow();
        Book book = bookRepository.findById(bookId).orElseThrow();

        Review review = new Review(rating, content, user, book);
        reviewRepository.saveAndFlush(review);

        List<Review> reviews = reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId);

        double newAverage = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        bookService.updateBookRating(bookId, newAverage);
    }

    public List<Review> getReviewsForBook(UUID bookId) {
        return reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId);
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    @Transactional
    public void deleteReview(UUID id) {
        reviewRepository.deleteById(id);
    }
}