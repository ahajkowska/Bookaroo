package org.example.bookaroo.controller.view;

import org.example.bookaroo.entity.Author;
import org.example.bookaroo.entity.Book;
import org.example.bookaroo.entity.User;
import org.example.bookaroo.repository.AuthorRepository;
import org.example.bookaroo.repository.BookRepository;
import org.example.bookaroo.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final UserRepository userRepository;

    public AdminController(BookRepository bookRepository, AuthorRepository authorRepository, UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("books", bookRepository.findAll());
        model.addAttribute("authors", authorRepository.findAll());
        model.addAttribute("users", userRepository.findAll());
        return "admin/dashboard";
    }

    @GetMapping("/book/add")
    public String showAddBookForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("authors", authorRepository.findAll());
        return "admin/book-form";
    }

    @PostMapping("/book/save")
    public String saveBook(@ModelAttribute Book book) {
        bookRepository.save(book);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/book/delete/{id}")
    public String deleteBook(@PathVariable UUID id) {
        bookRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }

    // formularz dodawania autora
    @GetMapping("/author/add")
    public String showAddAuthorForm(Model model) {
        model.addAttribute("author", new Author());
        return "admin/author-form";
    }

    @PostMapping("/author/save")
    public String saveAuthor(@ModelAttribute Author author) {
        authorRepository.save(author);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/author/delete/{id}")
    public String deleteAuthor(@PathVariable UUID id) {
        try {
            authorRepository.deleteById(id);
        } catch (Exception e) {
            System.err.println("Nie można usunąć autora, bo ma przypisane książki.");
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/book/edit/{id}")
    public String showEditBookForm(@PathVariable UUID id, Model model) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nieprawidłowe ID książki: " + id));

        model.addAttribute("book", book);
        model.addAttribute("authors", authorRepository.findAll());

        return "admin/book-form";
    }

    @GetMapping("/author/edit/{id}")
    public String showEditAuthorForm(@PathVariable UUID id, Model model) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nieprawidłowe ID autora: " + id));

        model.addAttribute("author", author);

        return "admin/author-form";
    }

    // moderacja użytkowników
    @GetMapping("/user/toggle-lock/{id}")
    public String toggleUserLock(@PathVariable UUID id, @AuthenticationPrincipal UserDetails currentUser) {
        User userToMod = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nieprawidłowe ID użytkownika"));

        if (currentUser.getUsername().equals(userToMod.getUsername())) {
            System.out.println("Nie możesz zablokować samego siebie!");
            return "redirect:/admin/dashboard?error=self_ban";
        }

        userToMod.setLocked(!userToMod.isLocked());
        userRepository.save(userToMod);

        return "redirect:/admin/dashboard";
    }
}