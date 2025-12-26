package org.example.bookaroo.controller.view;

import org.example.bookaroo.entity.Book;
import org.example.bookaroo.entity.Review;
import org.example.bookaroo.repository.BookRepository;
import org.example.bookaroo.service.CustomUserDetailsService;
import org.example.bookaroo.service.ReviewService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Controller
public class BookDetailsController {

    private final BookRepository bookRepository;
    private final ReviewService reviewService;
    private final org.example.bookaroo.repository.StatisticsRepository statisticsRepository;

    public BookDetailsController(BookRepository bookRepository, ReviewService reviewService, org.example.bookaroo.repository.StatisticsRepository statisticsRepository) {
        this.bookRepository = bookRepository;
        this.reviewService = reviewService;
        this.statisticsRepository = statisticsRepository;
    }

    // szczegóły książki + recenzje
    @GetMapping("/book/{id}")
    public String showBookDetails(@PathVariable UUID id, Model model) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        List<Review> reviews = reviewService.getReviewsForBook(id);

        var stats = statisticsRepository.getBookStats(id);
        model.addAttribute("stats", stats);
        
        model.addAttribute("book", book);
        model.addAttribute("reviews", reviews);
        return "book-details";
    }

    // dodawanie recenzji
    @PostMapping("/review/add")
    public String addReview(
            @RequestParam UUID bookId,
            @RequestParam int rating,
            @RequestParam String content,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        if (currentUser instanceof CustomUserDetailsService.BookarooUserDetails) {
            UUID userId = ((CustomUserDetailsService.BookarooUserDetails) currentUser).getId();
            reviewService.addReview(userId, bookId, rating, content);
        }
        return "redirect:/book/" + bookId;
    }
}