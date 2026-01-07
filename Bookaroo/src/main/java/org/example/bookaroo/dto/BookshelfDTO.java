package org.example.bookaroo.dto;

import java.util.List;
import java.util.UUID;

public record BookshelfDTO(
        UUID id,
        String name,
        boolean isDefault,
        List<BookDTO> books
) {}