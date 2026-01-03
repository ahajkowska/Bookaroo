package org.example.bookaroo.dto;

import java.util.List;
import java.util.UUID;

public record BookshelfDTO(
        UUID id,
        String name,
        List<BookDTO> books
) {}