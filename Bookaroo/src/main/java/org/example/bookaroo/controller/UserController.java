package org.example.bookaroo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.bookaroo.dto.CreateUserDTO;
import org.example.bookaroo.dto.UpdateUserDTO;
import org.example.bookaroo.dto.UserDTO;
import org.example.bookaroo.exception.ErrorResponse;
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

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET ALL
    @GetMapping
    @Operation(summary = "Pobierz wszystkich użytkowników", description = "Zwraca listę użytkowników z paginacją i sortowaniem.")
    @ApiResponse(
            responseCode = "200",
            description = "Lista użytkowników (z paginacją)",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Page.class),
                    examples = @ExampleObject(
                            name = "Strona 0",
                            value = """
                                    {
                                      "content": [
                                        {"id": "uuid-1", "username": "adam", "role": "USER"},
                                        {"id": "uuid-2", "username": "ewa", "role": "ADMIN"}
                                      ],
                                      "totalElements": 2,
                                      "totalPages": 1,
                                      "size": 10
                                    }
                                    """
                    )
            )
    )
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

    // GET BY ID
    @GetMapping("/{id}")
    @Operation(summary = "Pobierz użytkownika po ID", description = "Zwraca szczegóły pojedynczego użytkownika")
    @ApiResponse(
            responseCode = "200",
            description = "Użytkownik znaleziony",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDTO.class),
                    examples = @ExampleObject(
                            name = "Szczegóły użytkownika",
                            value = """
                                    {"id": "550e8400-e29b-41d4-a716-446655440000", "username": "anna_nowak", "email": "anna@bookaroo.com", "avatar": null, "bio": "Lubię czytać kryminały", "role": "ADMIN", "locked": false}
                                    """
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Użytkownik nieznaleziony",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            name = "Błąd - brak użytkownika",
                            value = """
                                    {"status": 404, "message": "Nie znaleziono użytkownika o ID: 123e4567-e89b-12d3-a456-426614174000", "timestamp": "2026-01-07T19:30:00"}
                                    """
                    )
            )
    )
    public ResponseEntity<UserDTO> getUserById(@Parameter(description = "ID użytkownika", required = true, example = "550e8400-e29b-41d4-a716-446655440001") @PathVariable UUID id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    // CREATE
    @PostMapping
    @Operation(summary = "Utwórz nowego użytkownika", description = "Tworzy nowego użytkownika w systemie")
    @ApiResponse(
            responseCode = "201",
            description = "Utworzono",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDTO.class),
                    examples = @ExampleObject(value = "{\"id\": \"uuid-new\", \"username\": \"nowy_user\", \"role\": \"USER\"}")
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Błąd walidacji",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "409",
            description = "Konflikt danych",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            name = "Email zajęty",
                            value = "{\"status\": 409, \"message\": \"Email jest już zajęty\", \"timestamp\": \"...\"}"
                    )
            )
    )
    public ResponseEntity<UserDTO> createUser(
            @Parameter(description = "Dane nowego użytkownika", required = true) @Valid @RequestBody CreateUserDTO createUserDTO,
            UriComponentsBuilder uriBuilder
    ) {
        UserDTO created = userService.createUser(createUserDTO);
        URI location = uriBuilder.path("/api/v1/users/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    // UPDATE
    @PutMapping("/{id}")
    @Operation(summary = "Zaktualizuj użytkownika", description = "Aktualizuje dane istniejącego użytkownika (wymaga ADMINA)")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponse(responseCode = "200", description = "Zaktualizowano", content = @Content(schema = @Schema(implementation = UserDTO.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Błąd walidacji danych",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Nie znaleziono", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Email już zajęty")
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "ID użytkownika", required = true) @PathVariable UUID id,
            @Parameter(description = "Zaktualizowane dane użytkownika", required = true) @Valid @RequestBody UpdateUserDTO updateUserDTO
    ) {
        UserDTO updated = userService.updateUser(id, updateUserDTO);
        return ResponseEntity.ok(updated);
    }

    // DELETE
    @DeleteMapping("/{id}")
    @Operation(summary = "Usuń użytkownika", description = "Usuwa użytkownika (na stałe) - wymaga roli ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponse(
            responseCode = "204",
            description = "Usunięto pomyślnie",
            content = @Content(schema = @Schema(hidden = true))
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Brak uprawnień",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Nie znaleziono użytkownika",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<Void> deleteUser(@Parameter(description = "ID użytkownika", required = true) @PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ALFABETYCZNIE
    @GetMapping("/alphabetical")
    @Operation(summary = "Pobierz użytkowników alfabetycznie", description = "Zwraca listę użytkowników posortowaną według username")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json"))
    public ResponseEntity<List<UserDTO>> getUsersAlphabetically() {
        List<UserDTO> users = userService.getUsersAlphabetically();
        return ResponseEntity.ok(users);
    }
}