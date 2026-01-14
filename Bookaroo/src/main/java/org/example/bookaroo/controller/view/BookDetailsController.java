package org.example.bookaroo.controller.view;

import jakarta.validation.Valid;
import org.example.bookaroo.dto.BookDTO;
import org.example.bookaroo.dto.BookStatisticsDTO;
import org.example.bookaroo.dto.ReviewDTO;
import org.example.bookaroo.entity.Bookshelf;
import org.example.bookaroo.service.BookService;
import org.example.bookaroo.service.BookshelfService;
import org.example.bookaroo.service.CustomUserDetailsService;
import org.example.bookaroo.service.ReviewService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        BookDTO bookDto = bookService.getBookDetails(id);

        List<ReviewDTO> reviews = reviewService.getReviewsForBook(id);

        BookStatisticsDTO stats = bookService.getBookStatistics(id);

        model.addAttribute("stats", stats);
        model.addAttribute("book", bookDto);
        model.addAttribute("reviews", reviews);

        // obsługa półek użytkownika
        if (currentUser instanceof CustomUserDetailsService.BookarooUserDetails userDetails) {
            UUID userId = userDetails.getId();
            List<Bookshelf> userShelves = bookshelfService.getUserShelves(userId);
            String currentShelfName = bookshelfService.getShelfNameForBook(userId, id);

            model.addAttribute("userShelves", userShelves);
            model.addAttribute("currentShelfName", currentShelfName);
        }

        return "book-details";
    }

    // dodawanie recenzji
    @PostMapping("/review/add")
    public String addReview(
            @AuthenticationPrincipal UserDetails currentUser,
            @Valid @ModelAttribute ReviewDTO reviewDto
    ) {
        if (currentUser instanceof CustomUserDetailsService.BookarooUserDetails userDetails) {
            UUID userId = userDetails.getId();
            reviewService.addReview(userId, reviewDto);
        }
        return "redirect:/book/" + reviewDto.bookId();
    }
}