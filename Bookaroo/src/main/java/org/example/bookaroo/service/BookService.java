package org.example.bookaroo.service;

import org.example.bookaroo.dto.BookDTO;
import org.example.bookaroo.entity.Book;
import org.example.bookaroo.entity.Author;
import org.example.bookaroo.exception.ResourceNotFoundException;
import org.example.bookaroo.repository.AuthorRepository;
import org.example.bookaroo.repository.BookJdbcDao;
import org.example.bookaroo.repository.BookRepository;
import org.example.bookaroo.repository.StatisticsRepository;
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
    private final BookJdbcDao bookJdbcDao;
    private final StatisticsRepository statisticsRepository;
    private final AuthorRepository authorRepository;

    public BookService(BookRepository bookRepository,
                       BookJdbcDao bookJdbcDao,
                       StatisticsRepository statisticsRepository,
                       AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.bookJdbcDao = bookJdbcDao;
        this.statisticsRepository = statisticsRepository;
        this.authorRepository = authorRepository;
    }

    @Transactional
    public Book createBook(BookDTO bookDto) {
        Author author = authorRepository.findById(bookDto.authorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author", "id", bookDto.authorId()));

        Book book = new Book();
        book.setTitle(bookDto.title());
        book.setIsbn(bookDto.isbn());
        book.setDescription(bookDto.description());
        book.setPublicationYear(bookDto.publicationYear());
        book.setAuthor(author);

        return bookRepository.save(book);
    }

    @Transactional
    public Book updateBook(UUID id, BookDTO bookDto) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));

        book.setTitle(bookDto.title());
        book.setIsbn(bookDto.isbn());
        book.setDescription(bookDto.description());
        book.setPublicationYear(bookDto.publicationYear());

        // logika zmiany autora
        if (bookDto.authorId() != null && !bookDto.authorId().equals(book.getAuthor().getId())) {
            Author newAuthor = authorRepository.findById(bookDto.authorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Author", "id", bookDto.authorId()));
            book.setAuthor(newAuthor);
        }

        return bookRepository.save(book);
    }

    public List<Book> findAllList() {
        return bookRepository.findAll();
    }

    public Page<Book> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    public Optional<Book> findById(UUID id) {
        return bookRepository.findById(id);
    }

    public boolean existsById(UUID id) {
        return bookRepository.existsById(id);
    }

    @Transactional
    public Book save(Book book) {
        return bookRepository.save(book);
    }

    @Transactional
    public void deleteById(UUID id) {
        bookRepository.deleteById(id);
    }

    public List<Book> searchBooksList(String query) {
        return bookRepository.searchBooks(query);
    }

    public Page<Book> searchBooks(String query, Pageable pageable) {
        return bookRepository.searchBooks(query, pageable);
    }

    public Page<Book> findByAuthor(Author author, Pageable pageable) {
        return bookRepository.findByAuthor(author, pageable);
    }

    public Page<Book> findByGenresId(UUID genreId, Pageable pageable) {
        return bookRepository.findByGenresId(genreId, pageable);
    }

    public List<Book> getTopRatedBooksViaSql(int limit) {
        return bookJdbcDao.findTopRatedBooks(limit);
    }

    public List<Book> getBooksByYearViaSql(int year) {
        return bookJdbcDao.findBooksByPublicationYear(year);
    }

    public Page<Book> getBooksByAuthorId(UUID authorId, Pageable pageable) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author", "id", authorId));

        return bookRepository.findByAuthor(author, pageable);
    }

    public void updateBookRating(UUID bookId, Double newRating) {
        bookJdbcDao.updateBookRating(bookId, newRating);
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