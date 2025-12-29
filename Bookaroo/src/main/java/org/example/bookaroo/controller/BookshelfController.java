package org.example.bookaroo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.bookaroo.entity.Bookshelf;
import org.example.bookaroo.service.BookshelfService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shelves")
@Tag(name = "Bookshelf Management", description = "Endpointy do zarządzania półkami")
public class BookshelfController {

    private final BookshelfService bookshelfService;

    public BookshelfController(BookshelfService bookshelfService) {
        this.bookshelfService = bookshelfService;
    }

    @PostMapping("/{userId}")
    @Operation(summary = "Utwórz własną półkę", description = "Tworzy nową półkę dla użytkownika")
    public ResponseEntity<Void> createShelf(@PathVariable UUID userId, @RequestParam String name) {
        bookshelfService.createCustomShelf(userId, name);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Pobierz półki użytkownika", description = "Zwraca listę wszystkich półek danego użytkownika")
    public ResponseEntity<List<Bookshelf>> getUserShelves(@PathVariable UUID userId) {
        // Potem zmienić na DTO, a nie entity jak teraz
        return ResponseEntity.ok(bookshelfService.getUserShelves(userId));
    }

    @PostMapping("/{shelfId}/books/{bookId}")
    @Operation(summary = "Dodaj książkę do półki", description = "Przypisuje istniejącą książkę do konkretnej półki")
    public ResponseEntity<Void> addBookToShelf(
            @RequestParam UUID userId,
            @PathVariable UUID shelfId,
            @PathVariable UUID bookId) {

        bookshelfService.addOrMoveBook(userId, shelfId, bookId);
        return ResponseEntity.ok().build();
    }
}