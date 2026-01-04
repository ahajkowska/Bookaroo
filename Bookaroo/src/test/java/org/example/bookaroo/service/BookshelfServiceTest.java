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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookshelfServiceTest {

    @Mock
    private BookshelfRepository bookshelfRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookshelfBookRepository bookshelfBookRepository;

    @InjectMocks
    private BookshelfService bookshelfService;


    @Test
    @DisplayName("should generate exactly 3 default shelves")
    void shouldGenerateThreeShelves() {
        // Given
        User user = new User();

        // When
        List<Bookshelf> result = bookshelfService.generateDefaultShelves(user);

        // Then
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("should contain correct default names")
    void shouldContainDefaultNames() {
        // Given
        User user = new User();

        // When
        List<Bookshelf> result = bookshelfService.generateDefaultShelves(user);

        // Then
        assertThat(result)
                .extracting(Bookshelf::getName)
                .containsExactlyInAnyOrder("Przeczytane", "Chcę przeczytać", "Teraz czytam");
    }

    @Test
    @DisplayName("should set isDefault flag to true for generated shelves")
    void shouldSetIsDefaultToTrue() {
        // Given
        User user = new User();

        // When
        List<Bookshelf> result = bookshelfService.generateDefaultShelves(user);

        // Then
        assertThat(result).allMatch(Bookshelf::getIsDefault);
    }

    // CREATE CUSTOM SHELF

    @Test
    @DisplayName("should save new shelf when name is unique")
    void shouldSaveShelf_whenNameIsUnique() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setBookshelves(new ArrayList<>());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        bookshelfService.createCustomShelf(userId, "Fantasy");

        // Then
        verify(bookshelfRepository).save(any(Bookshelf.class));
    }

    @Test
    @DisplayName("should throw exception when shelf name already exists")
    void shouldThrowException_whenNameDuplicate() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = new User();
        Bookshelf existingShelf = new Bookshelf();
        existingShelf.setName("Fantasy");
        user.setBookshelves(List.of(existingShelf));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> bookshelfService.createCustomShelf(userId, "Fantasy"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("już istnieje");
    }

    @Test
    @DisplayName("should set isDefault to false for custom shelf")
    void shouldSetIsDefaultFalse_forCustomShelf() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setBookshelves(new ArrayList<>());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        bookshelfService.createCustomShelf(userId, "Custom");

        // Then
        ArgumentCaptor<Bookshelf> captor = ArgumentCaptor.forClass(Bookshelf.class);
        verify(bookshelfRepository).save(captor.capture());
        assertThat(captor.getValue().getIsDefault()).isFalse();
    }

    // GET USER SHELVES BY USERNAME

    @Test
    @DisplayName("should return shelves when user exists")
    void shouldReturnShelves_whenUserExists() {
        // Given
        String username = "john";
        User user = new User();
        user.setId(UUID.randomUUID());

        List<Bookshelf> shelves = List.of(new Bookshelf());

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(bookshelfRepository.findAllByUserId(user.getId())).thenReturn(shelves);

        // When
        List<Bookshelf> result = bookshelfService.getUserShelvesByUsername(username);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("should return empty list when user does not exist")
    void shouldReturnEmptyList_whenUserNotFound() {
        // Given
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // When
        List<Bookshelf> result = bookshelfService.getUserShelvesByUsername("unknown");

        // Then
        assertThat(result).isEmpty();
    }

    // GET SHELF NAME FOR BOOK

    @Test
    @DisplayName("should return shelf name when book is on a shelf")
    void shouldReturnShelfName_whenBookFound() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        // Tworzymy książkę i półkę
        Book book = new Book();
        book.setId(bookId);

        Bookshelf shelf = new Bookshelf();
        shelf.setName("Ulubione");
        BookshelfBook item = new BookshelfBook(shelf, book);
        shelf.setItems(List.of(item));

        when(bookshelfRepository.findAllByUserId(userId)).thenReturn(List.of(shelf));

        // When
        String result = bookshelfService.getShelfNameForBook(userId, bookId);

        // Then
        assertThat(result).isEqualTo("Ulubione");
    }

    @Test
    @DisplayName("should return null when book is not on any shelf")
    void shouldReturnNull_whenBookNotFoundOnShelves() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        Bookshelf shelf = new Bookshelf();
        shelf.setName("Empty Shelf");
        shelf.setItems(Collections.emptyList());

        when(bookshelfRepository.findAllByUserId(userId)).thenReturn(List.of(shelf));

        // When
        String result = bookshelfService.getShelfNameForBook(userId, bookId);

        // Then
        assertThat(result).isNull();
    }

    // ADD OR MOVE BOOK

    @Test
    @DisplayName("should remove book from old shelves and add to new one")
    void shouldMoveBook_whenTargetIsValid() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID targetShelfId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        // Stara półka
        Bookshelf oldShelf = new Bookshelf();
        oldShelf.setId(UUID.randomUUID());
        user.setBookshelves(List.of(oldShelf));

        Book book = new Book();
        book.setId(bookId);

        // Nowa półka
        Bookshelf targetShelf = new Bookshelf();
        targetShelf.setId(targetShelfId);
        targetShelf.setUser(user);
        targetShelf.setItems(new ArrayList<>());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookshelfRepository.findById(targetShelfId)).thenReturn(Optional.of(targetShelf));

        // When
        bookshelfService.addOrMoveBook(userId, bookId, targetShelfId);

        // Then
        // czy usunięto ze starych
        verify(bookshelfBookRepository).deleteByBookshelfIdAndBookId(oldShelf.getId(), bookId);
        // czy dodano do nowej
        verify(bookshelfBookRepository).save(any(BookshelfBook.class));
    }

    @Test
    @DisplayName("should not do anything if book is already on target shelf")
    void shouldDoNothing_whenBookAlreadyOnTarget() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID targetShelfId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        Book book = new Book();
        book.setId(bookId);

        Bookshelf targetShelf = new Bookshelf();
        targetShelf.setId(targetShelfId);
        targetShelf.setUser(user);

        BookshelfBook item = new BookshelfBook(targetShelf, book);
        targetShelf.setItems(List.of(item));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookshelfRepository.findById(targetShelfId)).thenReturn(Optional.of(targetShelf));

        // When
        bookshelfService.addOrMoveBook(userId, bookId, targetShelfId);

        // Then
        verify(bookshelfBookRepository, never()).deleteByBookshelfIdAndBookId(any(), any());
        verify(bookshelfBookRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw exception if target shelf belongs to another user")
    void shouldThrowException_whenShelfBelongsToOtherUser() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID targetShelfId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        User otherUser = new User();
        otherUser.setId(otherUserId);

        Bookshelf targetShelf = new Bookshelf();
        targetShelf.setUser(otherUser);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findById(any())).thenReturn(Optional.of(new Book()));
        when(bookshelfRepository.findById(targetShelfId)).thenReturn(Optional.of(targetShelf));

        // When & Then
        assertThatThrownBy(() -> bookshelfService.addOrMoveBook(userId, UUID.randomUUID(), targetShelfId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nie masz uprawnień");
    }

    // REMOVE BOOK

    @Test
    @DisplayName("should call delete for all user shelves")
    void shouldDeleteFromAllShelves() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        User user = new User();
        Bookshelf s1 = new Bookshelf(); s1.setId(UUID.randomUUID());
        Bookshelf s2 = new Bookshelf(); s2.setId(UUID.randomUUID());
        user.setBookshelves(List.of(s1, s2));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(new Book()));

        // When
        bookshelfService.removeBookFromLibrary(userId, bookId);

        // Then
        verify(bookshelfBookRepository).deleteByBookshelfIdAndBookId(s1.getId(), bookId);
        verify(bookshelfBookRepository).deleteByBookshelfIdAndBookId(s2.getId(), bookId);
    }
}