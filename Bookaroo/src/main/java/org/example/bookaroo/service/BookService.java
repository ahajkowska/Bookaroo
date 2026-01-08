package org.example.bookaroo.service;

import org.example.bookaroo.dto.BookDTO;
import org.example.bookaroo.dto.mapper.BookMapper;
import org.example.bookaroo.entity.Book;
import org.example.bookaroo.entity.Author;
import org.example.bookaroo.exception.ResourceNotFoundException;
import org.example.bookaroo.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookDAO bookDAO;
    private final StatisticsRepository statisticsRepository;
    private final AuthorRepository authorRepository;

    public BookService(BookRepository bookRepository,
                       BookDAO bookDAO,
                       StatisticsRepository statisticsRepository,
                       AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.bookDAO = bookDAO;
        this.statisticsRepository = statisticsRepository;
        this.authorRepository = authorRepository;
    }

    @Transactional(readOnly = true)
    public BookDTO getBookDetails(UUID id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));

        return BookMapper.toDto(book);
    }

    @Transactional
    public BookDTO createBook(BookDTO bookDto) {
        Author author = authorRepository.findById(bookDto.authorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author", "id", bookDto.authorId()));

        Book book = BookMapper.toEntity(bookDto);
        book.setAuthor(author);

        Book savedBook = bookRepository.save(book);

        return BookMapper.toDto(savedBook);
    }

    @Transactional
    public BookDTO updateBook(UUID id, BookDTO bookDto) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));

        BookMapper.updateEntity(bookDto, book);

        // logika zmiany autora
        if (bookDto.authorId() != null && !bookDto.authorId().equals(book.getAuthor().getId())) {
            Author newAuthor = authorRepository.findById(bookDto.authorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Author", "id", bookDto.authorId()));
            book.setAuthor(newAuthor);
        }

        Book savedBook = bookRepository.save(book);
        return BookMapper.toDto(savedBook);
    }

    @Transactional
    public void deleteById(UUID id) {
        bookRepository.deleteById(id);
    }

    public Page<Book> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    public Page<Book> findByGenresId(UUID genreId, Pageable pageable) {
        return bookRepository.findByGenresId(genreId, pageable);
    }

    public Optional<Book> findById(UUID id) {
        return bookRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<BookDTO> searchBooksList(String query) {
        return bookRepository.searchBooks(query)
                .stream()
                .map(BookMapper::toDto)
                .toList();
    }

    public Page<Book> searchBooks(String query, Pageable pageable) {
        return bookRepository.searchBooks(query, pageable);
    }

    public Page<Book> getBooksByAuthorId(UUID authorId, Pageable pageable) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author", "id", authorId));

        return bookRepository.findByAuthor(author, pageable);
    }

    @Transactional
    public void createBookViaSql(Book book) {
        bookDAO.insertBook(book);
    }

    @Transactional
    public void deleteBookViaSql(UUID id) {
        bookDAO.deleteBook(id);
    }

    @Transactional(readOnly = true)
    public List<BookDTO> findAllList() {
        return bookRepository.findAll()
                .stream()
                .map(BookMapper::toDto)
                .toList();
    }

    public List<Book> getTopRatedBooksViaSql(int limit) {
        return bookDAO.findTopRatedBooks(limit);
    }

    public List<Book> getBooksByYearViaSql(int year) {
        return bookDAO.findBooksByPublicationYear(year);
    }

    public void updateBookRatingViaSql(UUID bookId, Double newRating) {
        bookDAO.updateBookRating(bookId, newRating);
    }

    @Transactional
    public void updateBookRating(UUID bookId, double newAverageRating) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Książka nie istnieje o ID: " + bookId));

        book.setAverageRating(newAverageRating);
        bookRepository.save(book);
    }

    public Map<String, Object> getBookStatistics(UUID bookId) {
        return statisticsRepository.getBookStats(bookId);
    }

    public Map<UUID, Double> getAllBookAverageRatings() {
        return statisticsRepository.getAllBookAverageRatings();
    }

    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    public Author getAuthorById(UUID id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author", "id", id));
    }

    @Transactional
    public void saveAuthor(Author author) {
        authorRepository.save(author);
    }

    @Transactional
    public void deleteAuthor(UUID id) {
        authorRepository.deleteById(id);
    }
}