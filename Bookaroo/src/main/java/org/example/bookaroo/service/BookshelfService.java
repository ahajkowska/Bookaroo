package org.example.bookaroo.service;

import org.example.bookaroo.entity.Book;
import org.example.bookaroo.entity.Bookshelf;
import org.example.bookaroo.entity.User;
import org.example.bookaroo.exception.ResourceNotFoundException; // Użyj swojego wyjątku
import org.example.bookaroo.repository.BookRepository;
import org.example.bookaroo.repository.BookshelfRepository;
import org.example.bookaroo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BookshelfService {

    private final BookshelfRepository bookshelfRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public BookshelfService(BookshelfRepository bookshelfRepository, BookRepository bookRepository, UserRepository userRepository) {
        this.bookshelfRepository = bookshelfRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void addBookToShelfByName(UUID userId, UUID bookId, String shelfName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        Bookshelf shelf = bookshelfRepository.findByUserAndName(user, shelfName)
                .orElseGet(() -> {
                    Bookshelf newShelf = new Bookshelf();
                    newShelf.setName(shelfName);
                    newShelf.setUser(user);
                    return bookshelfRepository.save(newShelf);
                });

        if (!shelf.getBooks().contains(book)) {
            shelf.getBooks().add(book);
            bookshelfRepository.save(shelf);
        }
    }
}