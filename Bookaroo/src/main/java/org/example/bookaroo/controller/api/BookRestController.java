package org.example.bookaroo.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.bookaroo.dto.BookDTO;
import org.example.bookaroo.dto.mapper.BookMapper;
import org.example.bookaroo.entity.Author;
import org.example.bookaroo.entity.Book;
import org.example.bookaroo.repository.AuthorRepository;
import org.example.bookaroo.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Book Management", description = "Endpointy do zarządzania książkami")
public class BookRestController {

    private final BookService bookService;
    private final AuthorRepository authorRepository;

    public BookRestController(BookService bookService, AuthorRepository authorRepository) {
        this.bookService = bookService;
        this.authorRepository = authorRepository;
    }

    @GetMapping
    @Operation(summary = "Pobierz listę książek z paginacją", description = "Zwraca listę książek zgodnie z paginacją")
    public ResponseEntity<Page<BookDTO>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookService.findAll(pageable);

        return ResponseEntity.ok(bookPage.map(BookMapper::toDto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Pobierz książkę po ID", description = "Zwraca szczegóły pojedynczej książki")
    public ResponseEntity<BookDTO> getBookById(@PathVariable UUID id) {
        return bookService.findById(id)
                .map(BookMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    @Operation(summary = "Utwórz nową książkę", description = "Tworzy nową książkę w systemie")
    public ResponseEntity<BookDTO> createBook(@RequestBody BookDTO bookDto) {
        if (bookDto.title() == null || bookDto.authorId() == null) {
            return ResponseEntity.badRequest().build();
        }

        Author author = authorRepository.findById(bookDto.authorId())
                .orElse(null);

        if (author == null) {
            return ResponseEntity.badRequest().build();
        }

        Book book = new Book();
        book.setTitle(bookDto.title());
        book.setIsbn(bookDto.isbn());
        book.setDescription(bookDto.description());
        book.setPublicationYear(bookDto.publicationYear());
        book.setAuthor(author);

        Book savedBook = bookService.save(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(BookMapper.toDto(savedBook));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Zaktualizuj książkę", description = "Aktualizuje dane istniejącej książki")
    public ResponseEntity<BookDTO> updateBook(@PathVariable UUID id, @RequestBody BookDTO bookDto) {
        return bookService.findById(id)
                .map(existingBook -> {
                    existingBook.setTitle(bookDto.title());
                    existingBook.setIsbn(bookDto.isbn());
                    existingBook.setDescription(bookDto.description());
                    existingBook.setPublicationYear(bookDto.publicationYear());

                    if (bookDto.authorId() != null) {
                        authorRepository.findById(bookDto.authorId())
                                .ifPresent(existingBook::setAuthor);
                    }

                    Book updatedBook = bookService.save(existingBook);
                    return ResponseEntity.ok(BookMapper.toDto(updatedBook));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Usuń książkę", description = "Usuwa książkę (na stałe)")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID id) {
        if (!bookService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        bookService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // wyszukiwanie (custom query + paginacja)
    @GetMapping("/search")
    @Operation(summary = "Wyszukaj książki", description = "Szuka po tytule, ISBN lub autorze")
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
    public ResponseEntity<Page<BookDTO>> getBooksByAuthor(
            @PathVariable UUID authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return authorRepository.findById(authorId)
                .map(author -> {
                    Pageable pageable = PageRequest.of(page, size);
                    Page<Book> bookPage = bookService.findByAuthor(author, pageable);
                    return ResponseEntity.ok(bookPage.map(BookMapper::toDto));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // filtrowanie po Gatunku
    @GetMapping("/genre/{genreId}")
    @Operation(summary = "Książki z gatunku", description = "Pobiera książki z danego gatunku")
    public ResponseEntity<Page<BookDTO>> getBooksByGenre(
            @PathVariable UUID genreId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Book> bookPage = bookService.findByGenresId(genreId, pageable);

        return ResponseEntity.ok(bookPage.map(BookMapper::toDto));
    }

    @GetMapping("/top")
    @Operation(summary = "Najlepiej oceniane książki", description = "Pobiera najlepiej oceniane książki")
    public ResponseEntity<List<BookDTO>> getTopBooks(@RequestParam(defaultValue = "5") int limit) {
        List<Book> books = bookService.getTopRatedBooksViaSql(limit);

        List<BookDTO> dtos = books.stream()
                .map(BookMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/year/{year}")
    @Operation(summary = "Książki z danego roku", description = "Pobiera książki z danego roku")
    public ResponseEntity<List<BookDTO>> getBooksByYear(@PathVariable int year) {
        List<Book> books = bookService.getBooksByYearViaSql(year);

        List<BookDTO> dtos = books.stream()
                .map(BookMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}