package org.example.bookaroo.controller.view;

import org.example.bookaroo.dto.BookDTO;
import org.example.bookaroo.dto.BookshelfDTO;
import org.example.bookaroo.service.BookService;
import org.example.bookaroo.service.BookshelfService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class ViewController {

    private final BookService bookService;
    private final BookshelfService bookshelfService;

    public ViewController(BookService bookService,
                          BookshelfService bookshelfService) {
        this.bookService = bookService;
        this.bookshelfService = bookshelfService;
    }

    // strona główna z listą książek
    @GetMapping("/")
    public String index(@RequestParam(required = false) String search,
                        Model model,
                        @AuthenticationPrincipal UserDetails currentUser) {

        // logika wyszukiwania książek
        List<BookDTO> books;
        if (search != null && !search.isBlank()) {
            books = bookService.searchBooksList(search);
            model.addAttribute("searchQuery", search);
        } else {
            books = bookService.findAllList();
        }

        // statystyki ocen
        Map<UUID, Double> ratings = bookService.getAllBookAverageRatings();
        model.addAttribute("ratings", ratings);
        model.addAttribute("books", books);

        // przekazanie listy półek zalogowanego użytkownika
        if (currentUser != null) {
            List<BookshelfDTO> shelves = bookshelfService.getUserShelvesByUsername(currentUser.getUsername());
            model.addAttribute("userShelves", shelves);
        }

        return "index";
    }
}