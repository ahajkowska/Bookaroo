package org.example.bookaroo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.bookaroo.config.SecurityConfig;
import org.example.bookaroo.dto.BookshelfDTO;
import org.example.bookaroo.dto.CreateShelfDTO;
import org.example.bookaroo.service.BookshelfService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookshelfController.class)
@Import(SecurityConfig.class)
class BookshelfControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookshelfService bookshelfService;

    @Test
    @DisplayName("POST /api/v1/shelves/{userId} - Tworzenie półki (zalogowany user)")
    @WithMockUser
    void shouldCreateShelf_whenAuthenticated() throws Exception {
        UUID userId = UUID.randomUUID();
        CreateShelfDTO requestDto = new CreateShelfDTO("Ulubione");

        mockMvc.perform(post("/api/v1/shelves/{userId}", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        // czy serwis dostał wyciągniętą nazwę z DTO
        verify(bookshelfService).createCustomShelf(userId, "Ulubione");
    }

    @Test
    @DisplayName("GET /api/v1/shelves/{userId} - Lista półek użytkownika")
    @WithMockUser
    void shouldReturnUserShelves() throws Exception {
        UUID userId = UUID.randomUUID();

        BookshelfDTO shelf1 = new BookshelfDTO(UUID.randomUUID(), "Przeczytane", true, List.of());
        BookshelfDTO shelf2 = new BookshelfDTO(UUID.randomUUID(), "Do przeczytania", false, List.of());

        when(bookshelfService.getUserShelvesWithDetails(userId)).thenReturn(List.of(shelf1, shelf2));

        mockMvc.perform(get("/api/v1/shelves/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Przeczytane")));

        verify(bookshelfService).getUserShelvesWithDetails(userId);
    }

    @Test
    @DisplayName("POST /shelves/{shelfId}/books/{bookId} - Dodawanie książki do półki")
    @WithMockUser
    void shouldAddBookToShelf() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID shelfId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/shelves/{shelfId}/books/{bookId}", shelfId, bookId)
                        .param("userId", userId.toString())
                        .with(csrf()))
                .andExpect(status().isOk());

        // czy serwis został zawołany z poprawnymi ID
        verify(bookshelfService).addOrMoveBook(userId, shelfId, bookId);
    }

    @Test
    @DisplayName("POST /api/v1/shelves - Brak autoryzacji (401)")
    void shouldReturn401_whenNotAuthenticated() throws Exception {
        UUID userId = UUID.randomUUID();
        CreateShelfDTO requestDto = new CreateShelfDTO("Hacker Shelf");

        mockMvc.perform(post("/api/v1/shelves/{userId}", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());

        verify(bookshelfService, never()).createCustomShelf(any(), any());
    }

    @Test
    @DisplayName("POST /api/v1/shelves - Obsługa błędu")
    @WithMockUser
    void shouldReturnError_whenServiceThrowsException() throws Exception {
        UUID userId = UUID.randomUUID();
        String duplicateName = "Istniejąca Półka";
        CreateShelfDTO requestDto = new CreateShelfDTO(duplicateName);

        doThrow(new IllegalArgumentException("Półka już istnieje"))
                .when(bookshelfService).createCustomShelf(userId, duplicateName);

        mockMvc.perform(post("/api/v1/shelves/{userId}", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().is4xxClientError());
    }
}