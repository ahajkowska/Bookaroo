package org.example.bookaroo.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.bookaroo.dto.BookDTO;
import org.example.bookaroo.dto.mapper.BookMapper;
import org.example.bookaroo.entity.Author;
import org.example.bookaroo.entity.Book;
import org.example.bookaroo.repository.AuthorRepository;
import org.example.bookaroo.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Book Management", description = "Endpointy do zarządzania książkami")
public class BookRestController {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    public BookRestController(BookRepository bookRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
    }

    @GetMapping
    @Operation(summary = "Pobierz listę książek z paginacją", description = "Zwraca listę książek zgodnie z paginacją")
    public ResponseEntity<Page<BookDTO>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookRepository.findAll(pageable);

        Page<BookDTO> dtoPage = bookPage.map(BookMapper::toDto);

        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Pobierz książkę po ID", description = "Zwraca szczegóły pojedynczej książki")
    public ResponseEntity<BookDTO> getBookById(@PathVariable UUID id) {
        return bookRepository.findById(id)
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

        Book savedBook = bookRepository.save(book);

        return ResponseEntity.status(HttpStatus.CREATED).body(BookMapper.toDto(savedBook));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Zaktualizuj książkę", description = "Aktualizuje dane istniejącej książki")
    public ResponseEntity<BookDTO> updateBook(@PathVariable UUID id, @RequestBody BookDTO bookDto) {
        return bookRepository.findById(id)
                .map(existingBook -> {
                    existingBook.setTitle(bookDto.title());
                    existingBook.setIsbn(bookDto.isbn());
                    existingBook.setDescription(bookDto.description());
                    existingBook.setPublicationYear(bookDto.publicationYear());

                    if (bookDto.authorId() != null) {
                        authorRepository.findById(bookDto.authorId())
                                .ifPresent(existingBook::setAuthor);
                    }

                    Book updatedBook = bookRepository.save(existingBook);
                    // ZMIANA: Używamy Mappera
                    return ResponseEntity.ok(BookMapper.toDto(updatedBook));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Usuń książkę", description = "Usuwa książkę (na stałe)")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID id) {
        if (!bookRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        bookRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}