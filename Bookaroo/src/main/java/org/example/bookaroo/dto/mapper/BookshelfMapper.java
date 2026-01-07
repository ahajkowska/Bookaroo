package org.example.bookaroo.dto.mapper;

import org.example.bookaroo.dto.BookDTO;
import org.example.bookaroo.dto.BookshelfDTO;
import org.example.bookaroo.entity.Bookshelf;

import java.util.List;

public class BookshelfMapper {

    public static BookshelfDTO toDto(Bookshelf shelf) {
        if (shelf == null) return null;

        List<BookDTO> bookDtos = shelf.getBooks().stream()
                .map(BookMapper::toDto)
                .toList();

        return new BookshelfDTO(
                shelf.getId(),
                shelf.getName(),
                shelf.getIsDefault(),
                bookDtos
        );
    }
}