package org.example.neighborly.dto;

import jakarta.validation.constraints. Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// do aktualizacji (częściowej lub pełnej)

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDTO {

    @Email(message = "Email musi być poprawny")
    private String email;

    @Size(min = 8, message = "Hasło musi mieć minimum 8 znaków")
    private String password;

    @Size(max = 50, message = "Imię może mieć maksymalnie 50 znaków")
    private String firstName;

    @Size(max = 50, message = "Nazwisko może mieć maksymalnie 50 znaków")
    private String lastName;

}