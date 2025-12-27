package org.example.bookaroo.controller.view;

import org.example.bookaroo.service.BookshelfService;
import org.example.bookaroo.service.CustomUserDetailsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
public class BookshelfViewController {

    private final BookshelfService bookshelfService;

    public BookshelfViewController(BookshelfService bookshelfService) {
        this.bookshelfService = bookshelfService;
    }

    // dodawanie / przenoszenie
    @PostMapping("/bookshelf/add")
    public String addOrMoveBook(
            @RequestParam UUID bookId,
            @RequestParam String shelfName,
            @AuthenticationPrincipal UserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        if (currentUser instanceof CustomUserDetailsService.BookarooUserDetails) {
            UUID userId = ((CustomUserDetailsService.BookarooUserDetails) currentUser).getId();

            bookshelfService.addOrMoveBook(userId, bookId, shelfName);

            redirectAttributes.addFlashAttribute("message", "Książka przeniesiona na półkę: " + shelfName);
        }
        return "redirect:/book/" + bookId;
    }

    @PostMapping("/bookshelf/remove")
    public String removeBook(
            @RequestParam UUID bookId,
            @AuthenticationPrincipal UserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        if (currentUser instanceof CustomUserDetailsService.BookarooUserDetails) {
            UUID userId = ((CustomUserDetailsService.BookarooUserDetails) currentUser).getId();

            bookshelfService.removeBookFromLibrary(userId, bookId);

            redirectAttributes.addFlashAttribute("message", "Książka została usunięta z Twojej biblioteczki.");
        }
        // i do profilu
        if (currentUser instanceof CustomUserDetailsService.BookarooUserDetails) {
            return "redirect:/profile/" + ((CustomUserDetailsService.BookarooUserDetails) currentUser).getId();
        }
        return "redirect:/";
    }
}