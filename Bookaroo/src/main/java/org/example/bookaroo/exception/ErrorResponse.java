package org.example.bookaroo.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standardowa odpowiedź błędu API")
public class ErrorResponse {

    @Schema(description = "Kod statusu HTTP", example = "404")
    private int status;

    @Schema(description = "Szczegółowa wiadomość o błędzie", example = "Nie znaleziono użytkownika o ID: 123")
    private String message;

    @Schema(description = "Data i czas wystąpienia błędu")
    private LocalDateTime timestamp;
}