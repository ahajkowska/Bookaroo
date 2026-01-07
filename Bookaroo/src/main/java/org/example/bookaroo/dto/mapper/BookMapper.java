package org.example.bookaroo.dto.mapper;

import org.example.bookaroo.dto.BookDTO;
import org.example.bookaroo.entity.Book;
import org.example.bookaroo.entity.Genre;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BookMapper {

    public static BookDTO toDto(Book book) {
        if (book == null) {
            return null;
        }

        String authorFullName = null;
        UUID authorId = null;

        if (book.getAuthor() != null) {
            authorId = book.getAuthor().getId();
            authorFullName = book.getAuthor().getName() + " " + book.getAuthor().getSurname();
        }

        List<String> genreNames = new ArrayList<>();
        if (book.getGenres() != null) {
            genreNames = book.getGenres().stream()
                    .map(Genre::getName)
                    .toList();
        }

        return new BookDTO(
                book.getId(),
                book.getTitle(),
                book.getIsbn(),
                book.getDescription(),
                book.getPublicationYear(),
                authorId,
                authorFullName,
                book.getAverageRating(),
                genreNames
        );
    }

    public static Book toEntity(BookDTO dto) {
        if (dto == null) {
            return null;
        }
        Book book = new Book();
        book.setTitle(dto.title());
        book.setIsbn(dto.isbn());
        book.setDescription(dto.description());
        book.setPublicationYear(dto.publicationYear());

        return book;
    }

    public static void updateEntity(BookDTO dto, Book book) {
        if (dto == null || book == null) {
            return;
        }
        book.setTitle(dto.title());
        book.setIsbn(dto.isbn());
        book.setDescription(dto.description());
        book.setPublicationYear(dto.publicationYear());
    }
}