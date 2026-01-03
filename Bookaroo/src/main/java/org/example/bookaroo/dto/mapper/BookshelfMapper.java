package org.example.bookaroo.dto.mapper;

import org.example.bookaroo.dto.BookshelfDTO;
import org.example.bookaroo.entity.Bookshelf;

public class BookshelfMapper {

    public static BookshelfDTO toDto(Bookshelf shelf) {
        if (shelf == null) return null;

        return new BookshelfDTO(
                shelf.getId(),
                shelf.getName(),
                shelf.getBooks().stream()
                        .map(BookMapper::toDto)
                        .toList()
        );
    }
}