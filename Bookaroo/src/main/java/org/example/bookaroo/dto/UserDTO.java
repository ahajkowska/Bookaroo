package org.example.bookaroo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

// do odczytu - response - co zwracam klientowi

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    @NotNull
    private UUID id;

    @NotBlank(message = "Nazwa użytkownika nie może być pusta")
    private String username;

    @Email(message = "Niepoprawny format adresu email")
    @NotBlank(message = "Email jest wymagany")
    private String email;

    private String avatar;
    private String bio;

    @NotBlank
    private String role;

    @NotNull
    private LocalDateTime createdAt;

    boolean locked;
}