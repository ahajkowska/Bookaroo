package org.example.bookaroo.controller.view;

import org.example.bookaroo.entity.Book;
import org.example.bookaroo.entity.Bookshelf;
import org.example.bookaroo.entity.User;
import org.example.bookaroo.repository.BookRepository;
import org.example.bookaroo.repository.StatisticsRepository;
import org.example.bookaroo.repository.UserRepository;
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

    private final BookRepository bookRepository;
    private final StatisticsRepository statisticsRepository;
    private final UserRepository userRepository;

    public ViewController(BookRepository bookRepository,
                          StatisticsRepository statisticsRepository,
                          UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.statisticsRepository = statisticsRepository;
        this.userRepository = userRepository;
    }

    // strona główna z listą książek
    @GetMapping("/")
    public String index(@RequestParam(required = false) String search,
                        Model model,
                        @AuthenticationPrincipal UserDetails currentUser) {

        // logika wyszukiwania książek
        List<Book> books;
        if (search != null && !search.isBlank()) {
            books = bookRepository.searchBooks(search);
            model.addAttribute("searchQuery", search);
        } else {
            books = bookRepository.findAll();
        }

        // statystyki ocen
        Map<UUID, Double> ratings = statisticsRepository.getAllBookAverageRatings();
        model.addAttribute("ratings", ratings);
        model.addAttribute("books", books);

        // przekazanie listy półek zalogowanego użytkownika
        if (currentUser != null) {
            User user = userRepository.findByUsername(currentUser.getUsername()).orElse(null);
            if (user != null) {
                model.addAttribute("userShelves", user.getBookshelves());
            }
        }

        return "index";
    }
}