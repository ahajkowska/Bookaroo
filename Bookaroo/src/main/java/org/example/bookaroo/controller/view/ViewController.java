package org.example.bookaroo.controller.view;

import org.example.bookaroo.entity.Book;
import org.example.bookaroo.entity.Bookshelf;
import org.example.bookaroo.entity.User;
import org.example.bookaroo.exception.ResourceNotFoundException;
import org.example.bookaroo.repository.BookRepository;
import org.example.bookaroo.repository.UserRepository;
import org.example.bookaroo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@Controller
public class ViewController {

    private final BookRepository bookRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    public ViewController(BookRepository bookRepository, UserService userService, UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    // strona główna z listą książek
    @GetMapping("/")
    public String index(Model model) {
        List<Book> books = bookRepository.findAll();
        model.addAttribute("books", books);
        return "index"; // templates/index.html
    }

    // profil użytkownika
    @GetMapping("/profile/{userId}")
    public String userProfile(@PathVariable UUID userId, Model model) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<Bookshelf> shelves = userService.getUserShelves(userId);

        model.addAttribute("user", user);
        model.addAttribute("shelves", shelves);

        return "profile"; // templates/profile.html
    }
}