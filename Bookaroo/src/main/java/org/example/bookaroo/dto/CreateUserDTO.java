package org.example.bookaroo.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// do tworzenia - request - co klient wysyla przy rejestracji

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDTO {

    @NotBlank(message = "Username jest wymagany")
    @Size(min = 3, max = 50, message = "Username musi mieć od 3 do 50 znaków")
    private String username;

    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Email musi być poprawny")
    private String email;

    @NotBlank(message = "Hasło jest wymagane")
    @Size(min = 8, message = "Hasło musi mieć minimum 8 znaków")
    private String password;

    @NotBlank(message = "Rola jest wymagana")
    @Pattern(regexp = "USER|ADMIN", message = "Rola musi być USER lub ADMIN")
    private String role;

    @Size(max = 500, message = "Avatar URL może mieć maksymalnie 500 znaków")
    private String avatar;

    @Size(max = 500, message = "Bio może mieć maksymalnie 500 znaków")
    private String bio;
}