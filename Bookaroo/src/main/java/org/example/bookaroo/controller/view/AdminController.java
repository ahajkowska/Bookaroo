package org.example.bookaroo.controller.view;

import org.example.bookaroo.entity.Author;
import org.example.bookaroo.entity.Book;
import org.example.bookaroo.repository.AuthorRepository;
import org.example.bookaroo.repository.BookRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    public AdminController(BookRepository bookRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("books", bookRepository.findAll());
        model.addAttribute("authors", authorRepository.findAll());
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
}