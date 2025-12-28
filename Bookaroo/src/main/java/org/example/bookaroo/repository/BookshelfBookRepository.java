package org.example.bookaroo.repository;

import org.example.bookaroo.entity.BookshelfBook;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface BookshelfBookRepository extends JpaRepository<BookshelfBook, Long> {
    // do usuwania konkretnej książki z konkretnej półki
    void deleteByBookshelfIdAndBookId(UUID bookshelfId, UUID bookId);
}