package org.example.bookaroo.service;

import org.example.bookaroo.dto.BookDTO;
import org.example.bookaroo.entity.Author;
import org.example.bookaroo.entity.Book;
import org.example.bookaroo.exception.ResourceNotFoundException;
import org.example.bookaroo.repository.AuthorRepository;
import org.example.bookaroo.repository.BookJdbcDao;
import org.example.bookaroo.repository.BookRepository;
import org.example.bookaroo.repository.StatisticsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookJdbcDao bookJdbcDao;
    @Mock
    private StatisticsRepository statisticsRepository;
    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private BookService bookService;

    // CREATE BOOK

    @Test
    @DisplayName("should save book when valid author provided")
    void shouldSaveBook_whenAuthorExists() {
        // Given
        UUID authorId = UUID.randomUUID();
        Author author = new Author();
        author.setId(authorId);

        BookDTO dto = new BookDTO(null, "Title", "ISBN", "Desc", 2024, authorId, null, null);

        when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(bookRepository.save(any(Book.class))).thenAnswer(i -> i.getArgument(0));

        // When
        Book result = bookService.createBook(dto);

        // Then
        assertThat(result.getTitle()).isEqualTo("Title");
    }

    @Test
    @DisplayName("should associate correct author when creating book")
    void shouldAssociateAuthor_whenCreatingBook() {
        // Given
        UUID authorId = UUID.randomUUID();
        Author author = new Author();
        author.setId(authorId);

        BookDTO dto = new BookDTO(null, "Title", "ISBN", "Desc", 2024, authorId, null, null);

        when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(bookRepository.save(any(Book.class))).thenAnswer(i -> i.getArgument(0));

        // When
        Book result = bookService.createBook(dto);

        // Then
        assertThat(result.getAuthor()).isEqualTo(author);
    }

    @Test
    @DisplayName("should throw exception when creating book with non-existent author")
    void shouldThrowException_whenAuthorNotFoundForCreate() {
        // Given
        UUID authorId = UUID.randomUUID();

        BookDTO dto = new BookDTO(null, "Title", "ISBN", "Desc", 2024, authorId, null, null);

        when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookService.createBook(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Author");
    }

    // UPDATE BOOK

    @Test
    @DisplayName("should update book fields when book exists")
    void shouldUpdateFields_whenBookExists() {
        // Given
        UUID bookId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        Author author = new Author();
        author.setId(authorId);

        Book existingBook = new Book();
        existingBook.setId(bookId);
        existingBook.setTitle("Old Title");
        existingBook.setAuthor(author);

        BookDTO dto = new BookDTO(null, "New Title", "New ISBN", "New Desc", 2025, authorId, null, null);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(i -> i.getArgument(0));

        // When
        Book result = bookService.updateBook(bookId, dto);

        // Then
        assertThat(result.getTitle()).isEqualTo("New Title");
    }

    @Test
    @DisplayName("should change author when new author ID is provided")
    void shouldUpdateAuthor_whenIdChanged() {
        // Given
        UUID bookId = UUID.randomUUID();
        UUID oldAuthorId = UUID.randomUUID();
        UUID newAuthorId = UUID.randomUUID();

        Author oldAuthor = new Author(); oldAuthor.setId(oldAuthorId);
        Author newAuthor = new Author(); newAuthor.setId(newAuthorId);

        Book existingBook = new Book();
        existingBook.setId(bookId);
        existingBook.setAuthor(oldAuthor);

        // POPRAWKA: Dodano null na początku
        BookDTO dto = new BookDTO(null, "Title", "ISBN", "Desc", 2024, newAuthorId, null, null);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(authorRepository.findById(newAuthorId)).thenReturn(Optional.of(newAuthor));
        when(bookRepository.save(any(Book.class))).thenAnswer(i -> i.getArgument(0));

        // When
        Book result = bookService.updateBook(bookId, dto);

        // Then
        assertThat(result.getAuthor().getId()).isEqualTo(newAuthorId);
    }

    @Test
    @DisplayName("should throw exception when book to update not found")
    void shouldThrowException_whenBookToUpdateNotFound() {
        // Given
        UUID bookId = UUID.randomUUID();

        BookDTO dto = new BookDTO(null, "Title", "ISBN", "Desc", 2024, UUID.randomUUID(), null, null);

        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookService.updateBook(bookId, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book");
    }

    @Test
    @DisplayName("should throw exception when new author for update not found")
    void shouldThrowException_whenNewAuthorNotFound() {
        // Given
        UUID bookId = UUID.randomUUID();
        UUID oldAuthorId = UUID.randomUUID();
        UUID newAuthorId = UUID.randomUUID();

        Author oldAuthor = new Author(); oldAuthor.setId(oldAuthorId);
        Book existingBook = new Book();
        existingBook.setAuthor(oldAuthor);

        BookDTO dto = new BookDTO(null, "Title", "ISBN", "Desc", 2024, newAuthorId, null, null);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(authorRepository.findById(newAuthorId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookService.updateBook(bookId, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Author");
    }

    // FIND & SEARCH

    @Test
    @DisplayName("should return list of all books")
    void shouldReturnAllBooks() {
        // Given
        when(bookRepository.findAll()).thenReturn(List.of(new Book(), new Book()));

        // When
        List<Book> result = bookService.findAllList();

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("should delegate search to repository")
    void shouldDelegateSearch() {
        // Given
        String query = "Harry Potter";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> page = new PageImpl<>(List.of(new Book()));

        when(bookRepository.searchBooks(query, pageable)).thenReturn(page);

        // When
        Page<Book> result = bookService.searchBooks(query, pageable);

        // Then
        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("should call findById on repository")
    void shouldDelegateFindById() {
        // Given
        UUID id = UUID.randomUUID();
        Book book = new Book();
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        // When
        Optional<Book> result = bookService.findById(id);

        // Then
        assertThat(result).isPresent();
    }

    // DELETE

    @Test
    @DisplayName("should call deleteById on repository")
    void shouldDelegateDelete() {
        // Given
        UUID id = UUID.randomUUID();

        // When
        bookService.deleteById(id);

        // Then
        verify(bookRepository).deleteById(id);
    }

    // BOOKS BY AUTHOR ID

    @Test
    @DisplayName("should return page of books when author exists")
    void shouldReturnBooksByAuthor_whenAuthorExists() {
        // Given
        UUID authorId = UUID.randomUUID();
        Author author = new Author();
        Pageable pageable = PageRequest.of(0, 5);
        Page<Book> page = new PageImpl<>(List.of(new Book()));

        when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(bookRepository.findByAuthor(author, pageable)).thenReturn(page);

        // When
        Page<Book> result = bookService.getBooksByAuthorId(authorId, pageable);

        // Then
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("should throw exception when getting books for non-existent author")
    void shouldThrowException_whenGettingBooksByInvalidAuthorId() {
        // Given
        UUID authorId = UUID.randomUUID();
        when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookService.getBooksByAuthorId(authorId, Pageable.unpaged()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // JDBC & STATISTICS

    @Test
    @DisplayName("should delegate top rated books query to JDBC DAO")
    void shouldDelegateTopRatedQuery() {
        // Given
        int limit = 5;
        when(bookJdbcDao.findTopRatedBooks(limit)).thenReturn(List.of(new Book()));

        // When
        List<Book> result = bookService.getTopRatedBooksViaSql(limit);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("should delegate rating update to JDBC DAO")
    void shouldDelegateRatingUpdate() {
        // Given
        UUID bookId = UUID.randomUUID();
        Double newRating = 4.5;

        // When
        bookService.updateBookRating(bookId, newRating);

        // Then
        verify(bookJdbcDao).updateBookRating(bookId, newRating);
    }

    @Test
    @DisplayName("should delegate stats retrieval to statistics repository")
    void shouldDelegateStatsRetrieval() {
        // Given
        UUID bookId = UUID.randomUUID();
        when(statisticsRepository.getBookStats(bookId)).thenReturn(Map.of("avg", 4.5));

        // When
        Map<String, Object> result = bookService.getBookStatistics(bookId);

        // Then
        assertThat(result).containsEntry("avg", 4.5);
    }

    // AUTHOR MANAGEMENT

    @Test
    @DisplayName("should return author by ID")
    void shouldReturnAuthorById() {
        // Given
        UUID id = UUID.randomUUID();
        when(authorRepository.findById(id)).thenReturn(Optional.of(new Author()));

        // When
        Author result = bookService.getAuthorById(id);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("should throw exception when author ID not found")
    void shouldThrowException_whenAuthorByIdMissing() {
        // Given
        UUID id = UUID.randomUUID();
        when(authorRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookService.getAuthorById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("should call save on author repository")
    void shouldSaveAuthor() {
        // Given
        Author author = new Author();

        // When
        bookService.saveAuthor(author);

        // Then
        verify(authorRepository).save(author);
    }

    @Test
    @DisplayName("should call deleteById on author repository")
    void shouldDeleteAuthor() {
        // Given
        UUID id = UUID.randomUUID();

        // When
        bookService.deleteAuthor(id);

        // Then
        verify(authorRepository).deleteById(id);
    }
}