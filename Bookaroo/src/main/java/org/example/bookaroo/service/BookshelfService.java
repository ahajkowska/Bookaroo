package org.example.bookaroo.service;

import org.example.bookaroo.entity.Book;
import org.example.bookaroo.entity.Bookshelf;
import org.example.bookaroo.entity.BookshelfBook;
import org.example.bookaroo.entity.User;
import org.example.bookaroo.exception.ResourceNotFoundException;
import org.example.bookaroo.repository.BookRepository;
import org.example.bookaroo.repository.BookshelfBookRepository;
import org.example.bookaroo.repository.BookshelfRepository;
import org.example.bookaroo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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

    public List<Bookshelf> generateDefaultShelves(User user) {
        List<Bookshelf> shelves = new ArrayList<>();
        shelves.add(createShelfEntity(user, "Przeczytane", true));
        shelves.add(createShelfEntity(user, "Chcę przeczytać", true));
        shelves.add(createShelfEntity(user, "Teraz czytam", true));
        return shelves;
    }

    private Bookshelf createShelfEntity(User user, String name, boolean isDefault) {
        Bookshelf shelf = new Bookshelf();
        shelf.setName(name);
        shelf.setIsDefault(isDefault);
        shelf.setUser(user);
        return shelf;
    }

    @Transactional
    public void createCustomShelf(UUID userId, String shelfName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // sprawdzenie duplikatów
        boolean exists = user.getBookshelves().stream()
                .anyMatch(s -> s.getName().equalsIgnoreCase(shelfName));

        if (exists) {
            throw new IllegalArgumentException("Półka " + shelfName + " już istnieje");
        }

        Bookshelf shelf = createShelfEntity(user, shelfName, false);
        bookshelfRepository.save(shelf);
    }

    @Transactional(readOnly = true)
    public List<Bookshelf> getUserShelves(UUID userId) {
        return bookshelfRepository.findAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Bookshelf> getUserShelvesByUsername(String username) {
        // 1. Szukamy użytkownika po nazwie (żeby zdobyć jego ID)
        return userRepository.findByUsername(username)
                .map(user -> bookshelfRepository.findAllByUserId(user.getId())) // Jeśli user jest -> pobierz półki
                .orElseGet(java.util.Collections::emptyList); // Jeśli user nie istnieje -> zwróć pustą listę
    }

    // na której półce znajduje się dana książka (nazwa półki lub null)
    public String getShelfNameForBook(UUID userId, UUID bookId) {
        List<Bookshelf> userShelves = getUserShelves(userId);

        for (Bookshelf shelf : userShelves) {
            boolean containsBook = shelf.getBooks().stream()
                    .anyMatch(b -> b.getId().equals(bookId));

            if (containsBook) {
                return shelf.getName();
            }
        }
        return null;
    }

    @Transactional
    public void addOrMoveBook(UUID userId, UUID bookId, UUID targetShelfId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        Bookshelf targetShelf = bookshelfRepository.findById(targetShelfId)
                .orElseThrow(() -> new ResourceNotFoundException("Bookshelf", "id", targetShelfId));

        if (!targetShelf.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Nie masz uprawnień do tej półki!");
        }

        // czy lista książek na tej półce zawiera naszą książkę
        boolean alreadyOnShelf = targetShelf.getBooks().stream()
                .anyMatch(b -> b.getId().equals(bookId));

        if (alreadyOnShelf) {
            return;
        }
        // przenoszenie książki
        for (Bookshelf shelf : user.getBookshelves()) {
            bookshelfBookRepository.deleteByBookshelfIdAndBookId(shelf.getId(), book.getId());
        }

        // dodanie do nowej półki
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