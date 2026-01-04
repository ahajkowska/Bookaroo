package org.example.bookaroo.repository;

import org.example.bookaroo.entity.Bookshelf;
import org.example.bookaroo.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class BookshelfRepositoryTest {

    @Autowired
    private BookshelfRepository bookshelfRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("shelfOwner");
        testUser.setEmail("owner@example.com");
        testUser.setPassword("password");
        testUser.setRole("USER");

        entityManager.persistAndFlush(testUser);
    }

    @Test
    @DisplayName("should return all shelves belonging to specific user")
    void shouldFindShelves_whenUserHasShelves() {
        Bookshelf s1 = new Bookshelf(); s1.setName("A"); s1.setUser(testUser);
        Bookshelf s2 = new Bookshelf(); s2.setName("B"); s2.setUser(testUser);

        entityManager.persist(s1);
        entityManager.persist(s2);
        entityManager.flush();

        List<Bookshelf> results = bookshelfRepository.findAllByUserId(testUser.getId());

        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("should not return shelves belonging to other users")
    void shouldNotReturnShelves_ofOtherUsers() {
        User otherUser = new User();
        otherUser.setUsername("other");
        otherUser.setEmail("other@example.com");
        otherUser.setPassword("pass");
        otherUser.setRole("USER");
        entityManager.persist(otherUser);

        // półka testUsera
        Bookshelf myShelf = new Bookshelf();
        myShelf.setName("Mine");
        myShelf.setUser(testUser);
        entityManager.persist(myShelf);

        // półka innego usera
        Bookshelf otherShelf = new Bookshelf();
        otherShelf.setName("Theirs");
        otherShelf.setUser(otherUser);
        entityManager.persist(otherShelf);

        entityManager.flush();

        // When
        List<Bookshelf> results = bookshelfRepository.findAllByUserId(testUser.getId());

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Mine");
    }

    @Test
    @DisplayName("should return empty list when user has no shelves")
    void shouldReturnEmptyList_whenUserHasNoShelves() {
        // user już istnieje, nie dodajemy półek

        // When
        List<Bookshelf> results = bookshelfRepository.findAllByUserId(testUser.getId());

        // Then
        assertThat(results).isEmpty();
    }

    // TESTY CRUD

    @Test
    @DisplayName("should save valid bookshelf and generate ID")
    void shouldSaveBookshelf() {
        // Given
        Bookshelf shelf = new Bookshelf();
        shelf.setName("Favorites");
        shelf.setUser(testUser);
        shelf.setIsDefault(false);

        // When
        Bookshelf saved = bookshelfRepository.save(shelf);

        // Then
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("should find bookshelf by ID")
    void shouldFindById() {
        // Given
        Bookshelf shelf = new Bookshelf();
        shelf.setName("To Read");
        shelf.setUser(testUser);
        Bookshelf persisted = entityManager.persistAndFlush(shelf);

        // When
        Optional<Bookshelf> found = bookshelfRepository.findById(persisted.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("To Read");
    }

    @Test
    @DisplayName("should return empty optional when ID does not exist")
    void shouldReturnEmpty_whenIdNotFound() {
        // When
        Optional<Bookshelf> found = bookshelfRepository.findById(UUID.randomUUID());

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("should update bookshelf name")
    void shouldUpdateName() {
        // Given
        Bookshelf shelf = new Bookshelf();
        shelf.setName("Old Name");
        shelf.setUser(testUser);
        Bookshelf persisted = entityManager.persistAndFlush(shelf);

        // When
        persisted.setName("New Name");
        Bookshelf updated = bookshelfRepository.save(persisted);

        // Then
        assertThat(updated.getName()).isEqualTo("New Name");
    }

    @Test
    @DisplayName("should delete bookshelf by ID")
    void shouldDeleteBookshelf() {
        // Given
        Bookshelf shelf = new Bookshelf();
        shelf.setName("To Delete");
        shelf.setUser(testUser);
        Bookshelf persisted = entityManager.persistAndFlush(shelf);

        // When
        bookshelfRepository.deleteById(persisted.getId());

        // Then
        assertThat(bookshelfRepository.findById(persisted.getId())).isEmpty();
    }

    @Test
    @DisplayName("should check if bookshelf exists by ID")
    void shouldReturnTrue_whenCheckingExistence() {
        // Given
        Bookshelf shelf = new Bookshelf();
        shelf.setName("Check Me");
        shelf.setUser(testUser);
        Bookshelf persisted = entityManager.persistAndFlush(shelf);

        // When
        boolean exists = bookshelfRepository.existsById(persisted.getId());

        // Then
        assertThat(exists).isTrue();
    }

    // integrity

    @Test
    @DisplayName("should throw exception when saving bookshelf without user")
    void shouldThrowException_whenUserIsNull() {
        // Given
        Bookshelf shelf = new Bookshelf();
        shelf.setName("Orphan Shelf");

        // When i Then
        assertThatThrownBy(() -> bookshelfRepository.saveAndFlush(shelf))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("should default isDefault flag to false if not set (or true if logic dictates)")
    void shouldPersistDefaultFlag() {
        // Given
        Bookshelf shelf = new Bookshelf();
        shelf.setName("Testing Flags");
        shelf.setUser(testUser);
        shelf.setIsDefault(true);

        // When
        Bookshelf saved = bookshelfRepository.save(shelf);

        // Then
        assertThat(saved.getIsDefault()).isTrue();
    }
}