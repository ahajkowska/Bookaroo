package org.example.bookaroo.repository;

import org.example.bookaroo.entity.Author;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AuthorRepositoryTest {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Pomyślne zapisanie autora i wygenerowanie jego ID")
    void shouldSaveAuthor() {
        Author author = new Author();
        author.setName("George");
        author.setSurname("Orwell");

        Author savedAuthor = authorRepository.save(author);

        assertThat(savedAuthor.getId()).isNotNull();
    }

    @Test
    @DisplayName("Poprawne zapisanie danych autora")
    void shouldPersistDataCorrectly() {
        Author author = new Author();
        author.setName("Agatha");
        author.setSurname("Christie");

        Author savedAuthor = authorRepository.save(author);

        assertThat(savedAuthor.getSurname()).isEqualTo("Christie");
    }

    @Test
    @DisplayName("Znalezienie autora po ID")
    void shouldFindAuthorById() {
        Author author = new Author();
        author.setName("J.R.R.");
        author.setSurname("Tolkien");

        Author persisted = entityManager.persistAndFlush(author);

        Optional<Author> found = authorRepository.findById(persisted.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getSurname()).isEqualTo("Tolkien");
    }

    @Test
    @DisplayName("Zwrócenie empty optional kiedy author ID nie istnieje")
    void shouldReturnEmpty_whenIdMissing() {
        Optional<Author> found = authorRepository.findById(UUID.randomUUID());

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Zwrócenie wszystkich autorów")
    void shouldReturnAllAuthors() {
        Author a1 = new Author(); a1.setName("A"); a1.setSurname("A");
        Author a2 = new Author(); a2.setName("B"); a2.setSurname("B");

        entityManager.persist(a1);
        entityManager.persist(a2);
        entityManager.flush();

        List<Author> authors = authorRepository.findAll();

        assertThat(authors).hasSize(2);
    }

    @Test
    @DisplayName("Zwrócenie pustej listy autorów")
    void shouldReturnEmptyList_whenNoAuthors() {
        List<Author> authors = authorRepository.findAll();

        assertThat(authors).isEmpty();
    }

    @Test
    @DisplayName("Zakualizowanie szczegółów autora")
    void shouldUpdateAuthor() {
        Author author = new Author();
        author.setName("Stephen");
        author.setSurname("King");
        Author persisted = entityManager.persistAndFlush(author);

        persisted.setSurname("Bachman");
        Author updated = authorRepository.save(persisted);

        assertThat(updated.getSurname()).isEqualTo("Bachman");
    }

    @Test
    @DisplayName("Usunięcie autora po ID")
    void shouldDeleteAuthor() {
        Author author = new Author();
        author.setName("Delete");
        author.setSurname("Me");
        Author persisted = entityManager.persistAndFlush(author);

        authorRepository.deleteById(persisted.getId());

        Author found = entityManager.find(Author.class, persisted.getId());
        assertThat(found).isNull();
    }
}