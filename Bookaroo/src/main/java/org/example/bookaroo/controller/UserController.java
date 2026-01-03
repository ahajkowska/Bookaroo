package org.example.bookaroo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.bookaroo.dto.CreateUserDTO;
import org.example.bookaroo.dto.UpdateUserDTO;
import org.example.bookaroo.dto.UserDTO;
import org.example.bookaroo.exception.ErrorResponse;
import org.example.bookaroo.service.BookshelfService;
import org.example.bookaroo.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "Endpointy do zarządzania użytkownikami")
public class UserController {

    private final UserService userService;
    private final BookshelfService bookshelfService;

    public UserController(UserService userService, BookshelfService bookshelfService) {
        this.userService = userService;
        this.bookshelfService = bookshelfService;
    }

    @GetMapping
    @Operation(summary = "Pobierz wszystkich użytkowników", description = "Zwraca listę użytkowników z paginacją i sortowaniem")
    @ApiResponse(responseCode = "200", description = "Lista użytkowników",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @Parameter(description = "Numer strony (zaczyna się od 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Rozmiar strony") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Pole do sortowania") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Kierunek sortowania (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserDTO> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Pobierz użytkownika po ID", description = "Zwraca szczegóły pojedynczego użytkownika")
    @ApiResponse(
            responseCode = "200",
            description = "Użytkownik znaleziony",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Użytkownik nie znaleziony",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<UserDTO> getUserById(@Parameter(description = "ID użytkownika", required = true) @PathVariable UUID id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @Operation(summary = "Utwórz nowego użytkownika", description = "Tworzy nowego użytkownika w systemie")
    @ApiResponse(
            responseCode = "201",
            description = "Użytkownik utworzony",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "Niepoprawne dane wejściowe")
    @ApiResponse(responseCode = "409", description = "Użytkownik już istnieje")

    public ResponseEntity<UserDTO> createUser(
            @Parameter(description = "Dane nowego użytkownika", required = true) @Valid @RequestBody CreateUserDTO createUserDTO,
            UriComponentsBuilder uriBuilder
    ) {
        UserDTO created = userService.createUser(createUserDTO);
        URI location = uriBuilder.path("/api/v1/users/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Zaktualizuj użytkownika", description = "Aktualizuje dane istniejącego użytkownika")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponse(
            responseCode = "200",
            description = "Użytkownik zaktualizowany",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "Niepoprawne dane wejściowe")
    @ApiResponse(responseCode = "404", description = "Użytkownik nie znaleziony")
    @ApiResponse(responseCode = "409", description = "Email już zajęty")
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "ID użytkownika", required = true) @PathVariable UUID id,
            @Parameter(description = "Zaktualizowane dane użytkownika", required = true) @Valid @RequestBody UpdateUserDTO updateUserDTO
    ) {
        UserDTO updated = userService.updateUser(id, updateUserDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Usuń użytkownika", description = "Usuwa użytkownika (na stałe)")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponse(responseCode = "204", description = "Użytkownik usunięty")
    @ApiResponse(responseCode = "404", description = "Użytkownik nie znaleziony")
    public ResponseEntity<Void> deleteUser(@Parameter(description = "ID użytkownika", required = true) @PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/alphabetical")
    @Operation(summary = "Pobierz użytkowników alfabetycznie", description = "Zwraca listę użytkowników posortowaną według username")
    @ApiResponse(
            responseCode = "200",
            description = "Lista użytkowników alfabetycznie",
            content = @Content(mediaType = "application/json")
    )
    public ResponseEntity<List<UserDTO>> getUsersAlphabetically() {
        List<UserDTO> users = userService.getUsersAlphabetically();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/{userId}/shelves/{shelfId}/books/{bookId}")
    public ResponseEntity<Void> addBookToShelf(@PathVariable UUID userId,
                                               @PathVariable UUID shelfId,
                                               @PathVariable UUID bookId) {
        bookshelfService.addOrMoveBook(userId, shelfId, bookId);
        return ResponseEntity.ok().build();
    }
}