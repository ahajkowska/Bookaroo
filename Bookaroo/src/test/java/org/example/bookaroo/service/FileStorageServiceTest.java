package org.example.bookaroo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    @Mock
    private MultipartFile multipartFile;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(tempDir.toString());
    }

    @Test
    @DisplayName("should return path starting with /uploads/ when file is valid")
    void shouldReturnCorrectPathPrefix_whenFileIsValid() throws IOException {
        // Given
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("image.jpg");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test content".getBytes()));

        // When
        String result = fileStorageService.saveFile(multipartFile);

        // Then
        assertThat(result).startsWith("/uploads/");
    }

    @Test
    @DisplayName("should preserve file extension when saving")
    void shouldPreserveExtension_whenSavingFile() throws IOException {
        // Given
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("document.pdf");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        // When
        String result = fileStorageService.saveFile(multipartFile);

        // Then
        assertThat(result).endsWith(".pdf");
    }

    @Test
    @DisplayName("should actually create file in the directory")
    void shouldCreateFileOnDisk_whenSaving() throws IOException {
        // Given
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("test.txt");
        byte[] content = "Hello World".getBytes();
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(content));

        // When
        String resultPath = fileStorageService.saveFile(multipartFile);

        // Then
        String filename = resultPath.replace("/uploads/", "");

        // czy plik fizycznie istnieje w katalogu tymczasowym
        Path savedFile = tempDir.resolve(filename);
        assertThat(Files.exists(savedFile)).isTrue();
    }

    @Test
    @DisplayName("should return null when file is empty")
    void shouldReturnNull_whenFileIsEmpty() {
        // Given
        when(multipartFile.isEmpty()).thenReturn(true);

        // When
        String result = fileStorageService.saveFile(multipartFile);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("should handle null original filename")
    void shouldHandleNullFilename() throws IOException {
        // Given
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn(null);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        // When
        String result = fileStorageService.saveFile(multipartFile);

        // Then
        assertThat(result).startsWith("/uploads/");
    }

    @Test
    @DisplayName("should throw RuntimeException when IOException occurs during stream reading")
    void shouldThrowException_whenIoExceptionOccurs() throws IOException {
        // Given
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");
        // błąd wejścia/wyjścia (np. uszkodzony plik)
        when(multipartFile.getInputStream()).thenThrow(new IOException("Stream error"));

        // When & Then
        assertThatThrownBy(() -> fileStorageService.saveFile(multipartFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Błąd podczas zapisu pliku");
    }
}