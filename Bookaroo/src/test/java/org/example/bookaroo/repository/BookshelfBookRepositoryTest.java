package org.example.bookaroo.repository;

import org.example.bookaroo.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookshelfBookRepositoryTest {

    @Autowired
    private BookshelfBookRepository bookshelfBookRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Bookshelf testShelf;
    private Book testBook;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUsername("shelfOwner");
        user.setEmail("test@test.com");
        user.setPassword("pass");
        user.setRole("USER");
        entityManager.persist(user);

        testShelf = new Bookshelf();
        testShelf.setName("My Shelf");
        testShelf.setUser(user);
        entityManager.persist(testShelf);

        Author author = new Author();
        author.setName("Jane");
        author.setSurname("Doe");
        entityManager.persist(author);

        testBook = new Book();
        testBook.setTitle("Test Book");
        testBook.setIsbn("12345");
        testBook.setAuthor(author);
        entityManager.persist(testBook);

        entityManager.flush();
    }

    @Test
    @DisplayName("Usunięcie połączenia między półką a książką")
    void shouldDeleteEntry_whenExists() {
        // tworzenie połączenia (książka na półce)
        BookshelfBook link = new BookshelfBook(testShelf, testBook);
        entityManager.persistAndFlush(link);

        assertThat(bookshelfBookRepository.count()).isEqualTo(1);

        bookshelfBookRepository.deleteByBookshelfIdAndBookId(testShelf.getId(), testBook.getId());

        assertThat(bookshelfBookRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("Brak wyjątku przy usuwaniu nieistniejącego połączenia")
    void shouldDoNothing_whenEntryDoesNotExist() {
        // brak rekordu w BookshelfBook (mimo że półka i książka istnieją)

        bookshelfBookRepository.deleteByBookshelfIdAndBookId(testShelf.getId(), testBook.getId());

        assertThat(bookshelfBookRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("Usuwanie konkretnej książki z konkretnej półki")
    void shouldDeleteOnlySpecificEntry() {
        Book otherBook = new Book();
        otherBook.setTitle("Other Book");
        otherBook.setIsbn("99999");
        otherBook.setAuthor(testBook.getAuthor());
        entityManager.persist(otherBook);

        BookshelfBook link1 = new BookshelfBook(testShelf, testBook);
        BookshelfBook link2 = new BookshelfBook(testShelf, otherBook);

        entityManager.persist(link1);
        entityManager.persist(link2);
        entityManager.flush();

        bookshelfBookRepository.deleteByBookshelfIdAndBookId(testShelf.getId(), testBook.getId());

        List<BookshelfBook> remaining = bookshelfBookRepository.findAll();
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getBook().getTitle()).isEqualTo("Other Book");
    }

    @Test
    @DisplayName("Usuwamy książkę z półki nr 1, a zostawiamy na półce nr 2")
    void shouldDeleteFromOneShelfOnly() {
        Bookshelf otherShelf = new Bookshelf();
        otherShelf.setName("Other Shelf");
        otherShelf.setUser(testShelf.getUser());
        entityManager.persist(otherShelf);

        // ta sama książka jest na obu półkach
        BookshelfBook link1 = new BookshelfBook(testShelf, testBook);
        BookshelfBook link2 = new BookshelfBook(otherShelf, testBook);

        entityManager.persist(link1);
        entityManager.persist(link2);
        entityManager.flush();

        bookshelfBookRepository.deleteByBookshelfIdAndBookId(testShelf.getId(), testBook.getId());

        List<BookshelfBook> remaining = bookshelfBookRepository.findAll();
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getBookshelf().getName()).isEqualTo("Other Shelf");
    }

    @Test
    @DisplayName("Book entity nie zostaje usunięta kiedy usuwamy relacje")
    void shouldNotDeleteBookEntity() {
        BookshelfBook link = new BookshelfBook(testShelf, testBook);
        entityManager.persistAndFlush(link);

        bookshelfBookRepository.deleteByBookshelfIdAndBookId(testShelf.getId(), testBook.getId());

        // wpis w tabeli łączącej znika.
        assertThat(bookshelfBookRepository.count()).isEqualTo(0);
        // ale książka w tabeli 'books' nadal istnieje
        assertThat(entityManager.find(Book.class, testBook.getId())).isNotNull();
    }

    @Test
    @DisplayName("should NOT delete the Bookshelf entity when removing relation")
    void shouldNotDeleteShelfEntity() {
        BookshelfBook link = new BookshelfBook(testShelf, testBook);
        entityManager.persistAndFlush(link);

        bookshelfBookRepository.deleteByBookshelfIdAndBookId(testShelf.getId(), testBook.getId());

        // Półka nadal istnieje
        assertThat(entityManager.find(Bookshelf.class, testShelf.getId())).isNotNull();
    }
}