package org.example.bookaroo.repository;

import org.example.bookaroo.entity.Author;
import org.example.bookaroo.entity.Book;
import org.example.bookaroo.entity.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {
    
    List<Book> findByTitleIgnoreCase(String title);
    
    Optional<Book> findByIsbn(String isbn);
    
    boolean existsByIsbn(String isbn);
    
    List<Book> findByAuthor(Author author);
    
    // z paginacją
    Page<Book> findByAuthor(Author author, Pageable pageable);
    
    List<Book> findByGenre(Genre genre);
    
    // z paginacją
    Page<Book> findByGenre(Genre genre, Pageable pageable);
    
    List<Book> findTop10ByOrderByAverageRatingDesc();
    
    List<Book> findByPublicationYearOrderByTitleAsc(Integer year);
    
    // wyszukiwanie po autorze
    @Query("SELECT b FROM Book b JOIN b.author a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :authorName, '%')) OR LOWER(a.surname) LIKE LOWER(CONCAT('%', :authorName, '%'))")
    List<Book> findByAuthorNameContaining(@Param("authorName") String authorName);
    
    // książki z oceną powyżej określonej wartości
    @Query("SELECT b FROM Book b WHERE b.averageRating >= :minRating ORDER BY b.averageRating DESC")
    List<Book> findBooksWithMinRating(@Param("minRating") Double minRating);
    
    // statystyki książek (liczba książek, średnia ocena)
    @Query("SELECT COUNT(b), AVG(b.averageRating) FROM Book b")
    Object[] getBookStatistics();
    
    // tytuł LUB autor LUB ISBN
    @Query("SELECT b FROM Book b JOIN b.author a WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.surname) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Book> searchBooks(@Param("searchTerm") String searchTerm);
    
    // Wyszukiwanie z paginacją
    @Query("SELECT b FROM Book b JOIN b.author a WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.surname) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Book> searchBooks(@Param("searchTerm") String searchTerm, Pageable pageable);
}
