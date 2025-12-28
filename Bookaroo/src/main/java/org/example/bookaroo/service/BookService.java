package org.example.bookaroo.service;

import org.example.bookaroo.entity.Book;
import org.example.bookaroo.entity.Author;
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

    public BookService(BookRepository bookRepository,
                       BookJdbcDao bookJdbcDao,
                       StatisticsRepository statisticsRepository) {
        this.bookRepository = bookRepository;
        this.bookJdbcDao = bookJdbcDao;
        this.statisticsRepository = statisticsRepository;
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

    public void updateBookRating(UUID bookId, Double newRating) {
        bookJdbcDao.updateBookRating(bookId, newRating);
    }

    public Map<String, Object> getBookStatistics(UUID bookId) {
        return statisticsRepository.getBookStats(bookId);
    }
}