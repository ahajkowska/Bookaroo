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

    @PostMapping("/bookshelf/add")
    public String addBookToShelf(
            @RequestParam UUID bookId,
            @RequestParam String shelfName,
            @AuthenticationPrincipal UserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        if (currentUser instanceof CustomUserDetailsService.BookarooUserDetails) {
            UUID userId = ((CustomUserDetailsService.BookarooUserDetails) currentUser).getId();

            bookshelfService.addBookToShelfByName(userId, bookId, shelfName);

            redirectAttributes.addFlashAttribute("message", "Dodano do półki: " + shelfName);
        }
        return "redirect:/";
    }
}