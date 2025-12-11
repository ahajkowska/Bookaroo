package org.example.bookaroo.dto;

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

    @Size(max = 500, message = "URL avatara może mieć maksymalnie 500 znaków")
    private String avatar;

    @Size(max = 1000, message = "Bio może mieć maksymalnie 1000 znaków")
    private String bio;

}