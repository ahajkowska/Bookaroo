package org.example.bookaroo.controller.view;

import org.example.bookaroo.entity.Book;
import org.example.bookaroo.repository.BookRepository;
import org.example.bookaroo.repository.StatisticsRepository;
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

    public ViewController(BookRepository bookRepository, StatisticsRepository statisticsRepository) {
        this.bookRepository = bookRepository;
        this.statisticsRepository = statisticsRepository;
    }

    // strona główna z listą książek
    @GetMapping("/")
    public String index(@RequestParam(required = false) String search, Model model) {
        List<Book> books;

        if (search != null && !search.isBlank()) {
            books = bookRepository.searchBooks(search);
            model.addAttribute("searchQuery", search);
        } else {
            books = bookRepository.findAll();
        }

        Map<UUID, Double> ratings = statisticsRepository.getAllBookAverageRatings();

        model.addAttribute("ratings", ratings);
        model.addAttribute("books", books);
        return "index"; // index.html
    }
}