package org.example.bookaroo.dto.mapper;

import org.example.bookaroo.entity.Book;
import org.example.bookaroo.dto.BookDTO;
import java.util.UUID;

public class BookMapper {

    public static BookDTO toDto(Book book) {
        String authorFullName = (book.getAuthor() != null)
                ? book.getAuthor().getName() + " " + book.getAuthor().getSurname()
                : "Nieznany";

        UUID authorId = (book.getAuthor() != null) ? book.getAuthor().getId() : null;

        return new BookDTO(
                book.getId(),
                book.getTitle(),
                book.getIsbn(),
                book.getDescription(),
                book.getPublicationYear(),
                authorId,
                authorFullName,
                book.getAverageRating()
        );
    }
}