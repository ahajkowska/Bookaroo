package org.example.bookaroo.repository;

import org.example.bookaroo.entity.Book;
import java.util.List;
import java.util.UUID;

public interface BookDAO {
    List<Book> findTopRatedBooks(int limit);
    List<Book> findBooksByPublicationYear(int year);
    int updateBookRating(UUID bookId, Double newRating);
    int deleteBook(UUID bookId);
    int insertBook(Book book);
}