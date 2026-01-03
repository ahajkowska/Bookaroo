package org.example.bookaroo.controller.view;

import org.example.bookaroo.entity.Author;
import org.example.bookaroo.entity.Book;
import org.example.bookaroo.service.BookService;
import org.example.bookaroo.service.ReviewService;
import org.example.bookaroo.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final BookService bookService;
    private final UserService userService;
    private final ReviewService reviewService;

    public AdminController(BookService bookService, UserService userService, ReviewService reviewService) {
        this.bookService = bookService;
        this.userService = userService;
        this.reviewService = reviewService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("books", bookService.findAllList());
        model.addAttribute("authors", bookService.getAllAuthors());
        model.addAttribute("users", userService.getUsersAlphabetically());
        model.addAttribute("reviews", reviewService.getAllReviews());
        return "admin/dashboard";
    }

    @GetMapping("/book/add")
    public String showAddBookForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("authors", bookService.getAllAuthors());
        return "admin/book-form";
    }

    @PostMapping("/book/save")
    public String saveBook(@ModelAttribute Book book) {
        bookService.save(book);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/book/delete/{id}")
    public String deleteBook(@PathVariable UUID id) {
        bookService.deleteById(id);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/book/edit/{id}")
    public String showEditBookForm(@PathVariable UUID id, Model model) {
        Book book = bookService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nieprawidłowe ID książki: " + id));

        model.addAttribute("book", book);
        model.addAttribute("authors", bookService.getAllAuthors());

        return "admin/book-form";
    }

    // formularz dodawania autora
    @GetMapping("/author/add")
    public String showAddAuthorForm(Model model) {
        model.addAttribute("author", new Author());
        return "admin/author-form";
    }

    @PostMapping("/author/save")
    public String saveAuthor(@ModelAttribute Author author) {
        bookService.saveAuthor(author);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/author/delete/{id}")
    public String deleteAuthor(@PathVariable UUID id) {
        try {
            bookService.deleteAuthor(id);
        } catch (Exception e) {
            return "redirect:/admin/dashboard?error=cannot_delete_author";
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/author/edit/{id}")
    public String showEditAuthorForm(@PathVariable UUID id, Model model) {
        Author author = bookService.getAuthorById(id);
        model.addAttribute("author", author);
        return "admin/author-form";
    }

    // moderacja użytkowników
    @GetMapping("/user/toggle-lock/{id}")
    public String toggleUserLock(@PathVariable UUID id, @AuthenticationPrincipal UserDetails currentUser) {
        try {
            userService.toggleUserLock(id, currentUser.getUsername());
        } catch (IllegalArgumentException e) {
            if ("SELF_BAN".equals(e.getMessage())) {
                return "redirect:/admin/dashboard?error=self_ban";
            }
            throw e;
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/review/delete/{id}")
    public String deleteReview(@PathVariable UUID id) {
        reviewService.deleteReview(id);
        return "redirect:/admin/dashboard";
    }
}