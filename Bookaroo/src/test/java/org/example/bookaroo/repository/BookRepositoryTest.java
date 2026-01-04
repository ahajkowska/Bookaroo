package org.example.bookaroo.repository;

import org.example.bookaroo.entity.Author;
import org.example.bookaroo.entity.Book;
import org.example.bookaroo.entity.Genre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Author testAuthor;
    private Genre testGenre;
    private Book testBook;

    @BeforeEach
    void setUp() {
        testAuthor = new Author();
        testAuthor.setName("John");
        testAuthor.setSurname("Tolkien");
        entityManager.persist(testAuthor);

        testGenre = new Genre();
        testGenre.setName("Fantasy");
        entityManager.persist(testGenre);

        testBook = new Book();
        testBook.setTitle("The Hobbit");
        testBook.setIsbn("123-456");
        testBook.setPublicationYear(1937);
        testBook.setDescription("A hobbit journey");
        testBook.setAuthor(testAuthor);
        testBook.getGenres().add(testGenre);

        entityManager.persist(testBook);
        entityManager.flush();
    }

    @Test
    @DisplayName("Znalezienie książki po ISBN")
    void shouldFindBook_whenIsbnExists() {
        Optional<Book> found = bookRepository.findByIsbn("123-456");

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("The Hobbit");
    }

    @Test
    @DisplayName("Zwrócenie empty gdy ISBN nie istnieje")
    void shouldReturnEmpty_whenIsbnMissing() {
        Optional<Book> found = bookRepository.findByIsbn("999-999");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Znalezienie książek po autorze z paginacją")
    void shouldFindBooksByAuthor() {
        Pageable pageable = PageRequest.of(0, 5);

        Page<Book> result = bookRepository.findByAuthor(testAuthor, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAuthor().getSurname()).isEqualTo("Tolkien");
    }

    @Test
    @DisplayName("Zwrócenie pustej strony dla autora bez książek")
    void shouldReturnEmptyPage_whenAuthorHasNoBooks() {
        Author newAuthor = new Author();
        newAuthor.setName("Jane");
        newAuthor.setSurname("Austen");
        entityManager.persist(newAuthor);

        Page<Book> result = bookRepository.findByAuthor(newAuthor, Pageable.unpaged());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Znalezienie książki po genre ID")
    void shouldFindBooksByGenreId() {
        UUID genreId = testGenre.getId();

        Page<Book> result = bookRepository.findByGenresId(genreId, Pageable.unpaged());

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("The Hobbit");
    }

    @Test
    @DisplayName("Zwrócenie empty kiedy gatunek nie ma książek")
    void shouldReturnEmpty_whenGenreUnused() {
        Genre unusedGenre = new Genre();
        unusedGenre.setName("Sci-Fi");
        entityManager.persistAndFlush(unusedGenre);

        Page<Book> result = bookRepository.findByGenresId(unusedGenre.getId(), Pageable.unpaged());

        assertThat(result).isEmpty();
    }

    // @Query

    @Test
    @DisplayName("wyszukiwanie książek po tytule")
    void shouldSearchByTitle() {
        // część słowa
        List<Book> result = bookRepository.searchBooks("hob");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("The Hobbit");
    }

    @Test
    @DisplayName("wyszukiwanie książek po imieniu autora")
    void shouldSearchByAuthorName() {
        List<Book> result = bookRepository.searchBooks("John");

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getAuthor().getName()).isEqualTo("John");
    }

    @Test
    @DisplayName("wyszukiwanie książek po nazwisku autora")
    void shouldSearchByAuthorSurname() {
        List<Book> result = bookRepository.searchBooks("olkien");

        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("wyszukiwanie książek po ISBN")
    void shouldSearchByIsbn() {
        List<Book> result = bookRepository.searchBooks("123");

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getIsbn()).isEqualTo("123-456");
    }

    @Test
    @DisplayName("Zwrócenie pustej listy, gdy nic nie pasuje")
    void shouldReturnEmpty_whenNoMatch() {
        List<Book> result = bookRepository.searchBooks("Potter");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Wspieranie paginacji w wyszukiwaniu")
    void shouldSupportPaginationInSearch() {
        Book book2 = new Book();
        book2.setTitle("The Silmarillion");
        book2.setIsbn("999-888");
        book2.setAuthor(testAuthor);
        entityManager.persistAndFlush(book2);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Book> result = bookRepository.searchBooks("Tolkien", pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
    }
}