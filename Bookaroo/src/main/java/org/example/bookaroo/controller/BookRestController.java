package org.example.bookaroo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.bookaroo.dto.BookDTO;
import org.example.bookaroo.dto.mapper.BookMapper;
import org.example.bookaroo.entity.Book;
import org.example.bookaroo.exception.ErrorResponse;
import org.example.bookaroo.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Book Management", description = "Endpointy do zarządzania książkami")
public class BookRestController {

    private final BookService bookService;

    public BookRestController(BookService bookService) {
        this.bookService = bookService;
    }

    // GET ALL
    @GetMapping
    @Operation(summary = "Pobierz listę książek z paginacją", description = "Zwraca listę książek zgodnie z paginacją")
    @ApiResponse(
            responseCode = "200",
            description = "OK - Lista książek",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Page.class),
                    examples = @ExampleObject(
                            name = "Strona 0",
                            value = """
                                    {
                                      "content": [
                                        {"id": "uuid-1", "title": "Wiedźmin: Ostatnie Życzenie", "isbn": "978-83..."},
                                        {"id": "uuid-2", "title": "Pan Tadeusz", "isbn": "978-83..."}
                                      ],
                                      "totalPages": 5,
                                      "totalElements": 50,
                                      "size": 10,
                                      "number": 0
                                    }
                                    """
                    )
            )
    )
    public ResponseEntity<Page<BookDTO>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookService.findAll(pageable);

        return ResponseEntity.ok(bookPage.map(BookMapper::toDto));
    }

    // GET BY ID
    @GetMapping("/{id}")
    @Operation(summary = "Pobierz książkę po ID", description = "Zwraca szczegóły pojedynczej książki")
    @ApiResponse(
            responseCode = "200",
            description = "OK - Znaleziono książkę",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BookDTO.class),
                    examples = @ExampleObject(value = "{\"id\": \"uuid-1\", \"title\": \"Wiedźmin\", \"description\": \"O Geralcie z Rivii\"}")
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Not Found - Nie znaleziono książki",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<BookDTO> getBookById(@PathVariable UUID id) {
        return bookService.findById(id)
                .map(BookMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // CREATE
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Utwórz nową książkę", description = "Tworzy nową książkę w systemie")
    @ApiResponse(
            responseCode = "201",
            description = "Created - Książka utworzona",
            content = @Content(schema = @Schema(implementation = BookDTO.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Brak tytułu lub autora",
            content = @Content(schema = @Schema(hidden = true))
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Brak uprawnień adminia",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody BookDTO bookDto) {

        BookDTO savedBookDto = bookService.createBook(bookDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedBookDto);
    }

    // UPDATE
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Zaktualizuj książkę", description = "Aktualizuje dane istniejącej książki")
    @ApiResponse(
            responseCode = "200",
            description = "OK - Zaktualizowano pomyślnie",
            content = @Content(schema = @Schema(implementation = BookDTO.class))
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Brak uprawnień",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Not Found - Nie znaleziono książki",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<BookDTO> updateBook(@PathVariable UUID id, @Valid @RequestBody BookDTO bookDto) {
        BookDTO updatedBook = bookService.updateBook(id, bookDto);

        return ResponseEntity.ok(updatedBook);
    }

    // DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Usuń książkę", description = "Usuwa książkę (na stałe)")
    @ApiResponse(
            responseCode = "204",
            description = "No Content - Usunięto pomyślnie",
            content = @Content(schema = @Schema(hidden = true))
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Brak uprawnień",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Not Found - Nie znaleziono książki",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<Void> deleteBook(@PathVariable UUID id) {
        bookService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // wyszukiwanie (custom query + paginacja)
    @GetMapping("/search")
    @Operation(summary = "Wyszukaj książki", description = "Szuka po tytule, ISBN lub autorze")
    @ApiResponse(responseCode = "200", description = "OK - Wyniki wyszukiwania", content = @Content(schema = @Schema(implementation = Page.class)))
    public ResponseEntity<Page<BookDTO>> searchBooks(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> foundBooks = bookService.searchBooks(query, pageable);
        return ResponseEntity.ok(foundBooks.map(BookMapper::toDto));
    }

    // filtrowanie po autorze
    @GetMapping("/author/{authorId}")
    @Operation(summary = "Książki autora", description = "Pobiera książki autora")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Page.class)))
    public ResponseEntity<Page<BookDTO>> getBooksByAuthor(
            @PathVariable UUID authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Book> bookPage = bookService.getBooksByAuthorId(authorId, pageable);

        return ResponseEntity.ok(bookPage.map(BookMapper::toDto));
    }

    // filtrowanie po Gatunku
    @GetMapping("/genre/{genreId}")
    @Operation(summary = "Książki z gatunku", description = "Pobiera książki z danego gatunku")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Page.class)))
    public ResponseEntity<Page<BookDTO>> getBooksByGenre(
            @PathVariable UUID genreId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Book> bookPage = bookService.findByGenresId(genreId, pageable);

        return ResponseEntity.ok(bookPage.map(BookMapper::toDto));
    }

    // TOP
    @GetMapping("/top")
    @Operation(summary = "Najlepiej oceniane książki", description = "Pobiera najlepiej oceniane książki")
    @ApiResponse(
            responseCode = "200",
            description = "OK - Lista TOP",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = BookDTO.class))
            )
    )
    public ResponseEntity<List<BookDTO>> getTopBooks(@RequestParam(defaultValue = "5") int limit) {
        List<Book> books = bookService.getTopRatedBooksViaSql(limit);

        List<BookDTO> dtos = books.stream()
                .map(BookMapper::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    // BY YEAR
    @GetMapping("/year/{year}")
    @Operation(summary = "Książki z danego roku", description = "Pobiera książki z danego roku")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = BookDTO.class))))
    public ResponseEntity<List<BookDTO>> getBooksByYear(@PathVariable int year) {
        List<Book> books = bookService.getBooksByYearViaSql(year);

        List<BookDTO> dtos = books.stream()
                .map(BookMapper::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    // CREATE JDBC
    @PostMapping("/jdbc")
    @Operation(summary = "Dodaj książkę przez JDBC", description = "Użycie JdbcTemplate INSERT")
    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true)))
    public ResponseEntity<Void> createBookViaJdbc(@Valid @RequestBody BookDTO bookDto) {
        Book book = BookMapper.toEntity(bookDto);

        if (book.getId() == null) {
            book.setId(UUID.randomUUID());
        }

        bookService.createBookViaSql(book);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/jdbc/{id}")
    @Operation(summary = "Usuń książkę przez JDBC", description = "Użycie JdbcTemplate DELETE")
    @ApiResponse(responseCode = "204", description = "No Content", content = @Content(schema = @Schema(hidden = true)))
    public ResponseEntity<Void> deleteBookViaJdbc(@PathVariable UUID id) {
        bookService.deleteBookViaSql(id);
        return ResponseEntity.noContent().build();
    }
}