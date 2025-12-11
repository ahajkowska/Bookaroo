package org.example.bookaroo.dto;

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
    private UUID id;
    private String username;
    private String email;
    private String avatar;
    private String bio;
    private String role;
    private LocalDateTime createdAt;
}