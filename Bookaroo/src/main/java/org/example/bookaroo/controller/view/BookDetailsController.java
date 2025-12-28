package org.example.bookaroo.controller.view;

import org.example.bookaroo.entity.Book;
import org.example.bookaroo.entity.Review;
import org.example.bookaroo.service.BookService;
import org.example.bookaroo.service.BookshelfService;
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
import java.util.Map;
import java.util.UUID;

@Controller
public class BookDetailsController {

    private final BookService bookService;
    private final ReviewService reviewService;
    private final BookshelfService bookshelfService;

    public BookDetailsController(BookService bookService, ReviewService reviewService, BookshelfService bookshelfService) {
        this.bookService = bookService;
        this.reviewService = reviewService;
        this.bookshelfService = bookshelfService;
    }

    @GetMapping("/book/{id}")
    public String showBookDetails(@PathVariable UUID id, Model model,
                                  @AuthenticationPrincipal UserDetails currentUser) {
        Book book = bookService.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        List<Review> reviews = reviewService.getReviewsForBook(id);

        Map<String, Object> stats = bookService.getBookStatistics(id);

        model.addAttribute("stats", stats);
        model.addAttribute("book", book);
        model.addAttribute("reviews", reviews);

        // obsługa półek użytkownika
        if (currentUser != null) {
            if (currentUser instanceof CustomUserDetailsService.BookarooUserDetails) {
                UUID userId = ((CustomUserDetailsService.BookarooUserDetails) currentUser).getId();

                // pobranie listy półek
                var userShelves = bookshelfService.getUserShelves(userId);
                model.addAttribute("userShelves", userShelves);

                // na której półce jest książka
                String currentShelfName = bookshelfService.getShelfNameForBook(userId, id);
                model.addAttribute("currentShelfName", currentShelfName);
            }
        }

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