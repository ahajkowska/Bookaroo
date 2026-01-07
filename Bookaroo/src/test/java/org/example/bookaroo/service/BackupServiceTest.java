package org.example.bookaroo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.bookaroo.dto.ReviewBackupDTO;
import org.example.bookaroo.dto.ShelfBackupDTO;
import org.example.bookaroo.dto.UserBackupDTO;
import org.example.bookaroo.entity.*;
import org.example.bookaroo.exception.ResourceNotFoundException;
import org.example.bookaroo.repository.BookRepository;
import org.example.bookaroo.repository.BookshelfRepository;
import org.example.bookaroo.repository.ReviewRepository;
import org.example.bookaroo.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BackupServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private BookRepository bookRepository;
    @Mock private BookshelfRepository bookshelfRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private BackupService backupService;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("janusz");

        Author author = new Author();
        author.setName("Andrzej");
        author.setSurname("Sapkowski");

        book = new Book();
        book.setId(UUID.randomUUID());
        book.setTitle("Wiedźmin");
        book.setIsbn("1234567890");
        book.setAuthor(author);
    }

    // TESTY EKSPORTU

    @Test
    void exportUserDataToJson_ShouldReturnBytes_WhenUserExists() throws IOException {
        // Given
        when(userRepository.findByUsername("janusz")).thenReturn(Optional.of(user));
        byte[] expectedJson = "{json}".getBytes();
        when(objectMapper.writeValueAsBytes(any(UserBackupDTO.class))).thenReturn(expectedJson);

        // When
        byte[] result = backupService.exportUserDataToJson("janusz");

        // Then
        assertNotNull(result);
        assertArrayEquals(expectedJson, result);
        verify(userRepository).findByUsername("janusz");
    }

    @Test
    void exportUserReviewsToCsv_ShouldGenerateCsvContent() {
        // Given
        Review review = new Review();
        review.setBook(book);
        review.setRating(5);
        review.setContent("Super ksiazka");
        user.setGivenReviews(List.of(review));

        when(userRepository.findByUsername("janusz")).thenReturn(Optional.of(user));

        // When
        byte[] result = backupService.exportUserReviewsToCsv("janusz");
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Then
        assertNotNull(result);

        assertTrue(csvContent.contains("\"Tytuł książki\";\"Autor\";\"ISBN\""));

        assertTrue(csvContent.contains("\"Wiedźmin\""));
        assertTrue(csvContent.contains("\"Super ksiazka\""));
        assertTrue(csvContent.contains("\"5\""));
    }

    @Test
    void exportUserReviewsToPdf_ShouldGeneratePdfBytes() {
        // Given
        Review review = new Review();
        review.setBook(book);
        review.setRating(5);
        review.setContent("Super");
        user.setGivenReviews(List.of(review));

        when(userRepository.findByUsername("janusz")).thenReturn(Optional.of(user));

        // When
        byte[] result = backupService.exportUserReviewsToPdf("janusz");

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0); // Sprawdzamy czy wygenerowano cokolwiek
        // Nie parsujemy PDF w teście jednostkowym, sprawdzamy tylko czy proces przeszedł bez błędów
    }

    @Test
    void shouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findByUsername("nieznany")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                backupService.exportUserDataToJson("nieznany")
        );
    }

    // TESTY IMPORTU

    @Test
    void importUserData_ShouldSaveNewReview_WhenReviewDoesNotExist() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", "backup.json", "application/json", "{}".getBytes()
        );

        // DTO, które 'zwróci' ObjectMapper
        List<ReviewBackupDTO> reviews = List.of(
                new ReviewBackupDTO("1234567890", "Nowa recenzja", 5)
        );
        UserBackupDTO backupDTO = new UserBackupDTO(new ArrayList<>(), reviews);

        when(objectMapper.readValue(any(InputStream.class), eq(UserBackupDTO.class))).thenReturn(backupDTO);
        when(userRepository.findByUsername("janusz")).thenReturn(Optional.of(user));
        when(bookRepository.findByIsbn("1234567890")).thenReturn(Optional.of(book));

        // When
        backupService.importUserData("janusz", file);

        // Then
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void importUserData_ShouldAddBookToShelf_WhenShelfExists() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "test.json", "application/json", "{}".getBytes());

        // Istniejąca półka usera
        Bookshelf existingShelf = new Bookshelf();
        existingShelf.setName("Do przeczytania");
        existingShelf.setUser(user);
        existingShelf.setItems(new ArrayList<>()); // Pusta półka
        user.setBookshelves(List.of(existingShelf));

        // DTO z tą samą półką i jedną książką
        List<ShelfBackupDTO> shelves = List.of(
                new ShelfBackupDTO("Do przeczytania", List.of("1234567890"))
        );
        UserBackupDTO backupDTO = new UserBackupDTO(shelves, null);

        when(objectMapper.readValue(any(InputStream.class), eq(UserBackupDTO.class))).thenReturn(backupDTO);
        when(userRepository.findByUsername("janusz")).thenReturn(Optional.of(user));
        when(bookRepository.findByIsbn("1234567890")).thenReturn(Optional.of(book));

        // When
        backupService.importUserData("janusz", file);

        // Then
        // Sprawdzamy czy książka została dodana do istniejącej półki
        assertEquals(1, existingShelf.getBooks().size());
        assertEquals("Wiedźmin", existingShelf.getBooks().get(0).getTitle());
        verify(bookshelfRepository, times(1)).save(existingShelf);
    }

    // Sprzątanie po testach (usuwanie plików tymczasowych stworzonych przez import)
    @AfterEach
    void cleanUp() {
        File dir = new File("backups/");
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) f.delete();
            }
            dir.delete();
        }
    }
}