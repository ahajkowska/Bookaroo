package org.example.bookaroo.service;

import org.example.bookaroo.entity.Book;
import org.example.bookaroo.entity.Bookshelf;
import org.example.bookaroo.entity.BookshelfBook;
import org.example.bookaroo.entity.User;
import org.example.bookaroo.exception.ResourceNotFoundException; // Użyj swojego wyjątku
import org.example.bookaroo.repository.BookRepository;
import org.example.bookaroo.repository.BookshelfBookRepository;
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
    private final BookshelfBookRepository bookshelfBookRepository;

    public BookshelfService(BookshelfRepository bookshelfRepository, BookRepository bookRepository, UserRepository userRepository, BookshelfBookRepository bookshelfBookRepository) {
        this.bookshelfRepository = bookshelfRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.bookshelfBookRepository = bookshelfBookRepository;
    }

    @Transactional
    public void addOrMoveBook(UUID userId, UUID bookId, String targetShelfName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        // znalezienie docelowej półki
        Bookshelf targetShelf = user.getBookshelves().stream()
                .filter(s -> s.getName().equalsIgnoreCase(targetShelfName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono półki: " + targetShelfName));

        // czy książka jest już na tej samej półce
        if (targetShelf.getBooks().contains(book)) {
            return;
        }

        // Usuwanie książki ze wszystkich innych półek tego użytkownika
        for (Bookshelf shelf : user.getBookshelves()) {
            bookshelfBookRepository.deleteByBookshelfIdAndBookId(shelf.getId(), book.getId());
        }

        BookshelfBook newItem = new BookshelfBook(targetShelf, book);
        bookshelfBookRepository.save(newItem);
    }

    @Transactional
    public void removeBookFromLibrary(UUID userId, UUID bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        for (Bookshelf shelf : user.getBookshelves()) {
            bookshelfBookRepository.deleteByBookshelfIdAndBookId(shelf.getId(), bookId);
        }
    }
}