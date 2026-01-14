package org.example.bookaroo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.bookaroo.dto.BookshelfDTO;
import org.example.bookaroo.dto.CreateShelfDTO;
import org.example.bookaroo.exception.ErrorResponse;
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
    @ApiResponse(
            responseCode = "201",
            description = "Created - Półka została utworzona",
            content = @Content(schema = @Schema(hidden = true))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Nieprawidłowa nazwa",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Not Found - Nie znaleziono użytkownika",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "409",
            description = "Conflict - Półka o takiej nazwie już istnieje",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            name = "Duplikat",
                            value = "{\"status\": 409, \"message\": \"Półka 'Fantastyka' już istnieje\", \"timestamp\": \"...\"}"
                    )
            )
    )
    public ResponseEntity<Void> createShelf(@PathVariable UUID userId, @Valid @RequestBody CreateShelfDTO request) {
        bookshelfService.createCustomShelf(userId, request.name());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Pobierz półki użytkownika", description = "Zwraca listę wszystkich półek danego użytkownika")
    @ApiResponse(
            responseCode = "200",
            description = "OK - Lista półek",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = BookshelfDTO.class)),
                    examples = @ExampleObject(
                            name = "Lista Półek",
                            value = """
                                    [
                                      {"id": "uuid-1", "name": "Przeczytane", "isDefault": true, "bookCount": 12},
                                      {"id": "uuid-2", "name": "Chcę przeczytać", "isDefault": true, "bookCount": 5},
                                      {"id": "uuid-3", "name": "Fantastyka", "isDefault": false, "bookCount": 3}
                                    ]
                                    """
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Not Found - Nie znaleziono użytkownika",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<List<BookshelfDTO>> getUserShelves(@PathVariable UUID userId) {
        List<BookshelfDTO> shelfDtos = bookshelfService.getUserShelvesWithDetails(userId);

        return ResponseEntity.ok(shelfDtos);
    }

    @PostMapping("/{shelfId}/books/{bookId}")
    @Operation(summary = "Dodaj książkę do półki", description = "Przypisuje istniejącą książkę do konkretnej półki")
    @ApiResponse(
            responseCode = "200",
            description = "OK - Książka dodana/przeniesiona",
            content = @Content(schema = @Schema(hidden = true))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Błąd logiczny (np. książka już tam jest)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Brak dostępu",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(value = "{\"status\": 403, \"message\": \"Nie masz uprawnień do tej półki!\"}")
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Not Found - Nie znaleziono półki, książki lub użytkownika",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<Void> addBookToShelf(
            @RequestParam UUID userId,
            @PathVariable UUID shelfId,
            @PathVariable UUID bookId) {

        bookshelfService.addOrMoveBook(userId, shelfId, bookId);
        return ResponseEntity.ok().build();
    }
}