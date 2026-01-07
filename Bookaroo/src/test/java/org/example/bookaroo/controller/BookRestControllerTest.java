package org.example.bookaroo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.bookaroo.config.SecurityConfig;
import org.example.bookaroo.dto.BookDTO;
import org.example.bookaroo.entity.Author;
import org.example.bookaroo.entity.Book;
import org.example.bookaroo.service.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookRestController.class)
@Import(SecurityConfig.class)
class BookRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    @Test
    @DisplayName("GET /api/v1/books - Zwraca listę wszystkich książek")
    @WithMockUser
    void shouldReturnAllBooks_whenAuthenticated() throws Exception {
        Book book = createTestBook("The Hobbit", "123");
        Page<Book> bookPage = new PageImpl<>(List.of(book));

        when(bookService.findAll(any(Pageable.class))).thenReturn(bookPage);

        mockMvc.perform(get("/api/v1/books")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("The Hobbit")));
    }

    @Test
    @DisplayName("GET /api/v1/books/{id} - Wyszukanie książki po ID")
    @WithMockUser
    void shouldReturnBookById_whenFound() throws Exception {
        UUID id = UUID.randomUUID();
        Book book = createTestBook("1984", "456");
        book.setId(id);

        when(bookService.findById(id)).thenReturn(Optional.of(book));

        mockMvc.perform(get("/api/v1/books/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("1984")));
    }

    @Test
    @DisplayName("GET /api/v1/books/{id} - Książka nieznaleziona po ID (404)")
    @WithMockUser
    void shouldReturn404_whenBookNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(bookService.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/books/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/books - Stworzenie książki (Admin)")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateBook_whenValid() throws Exception {
        UUID authorId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        BookDTO inputDto = new BookDTO(
                null,
                "Dune",
                "999",
                "Sci-Fi Epic",
                1965,
                authorId,
                "Frank Herbert",
                null,
                null
        );

        BookDTO savedDto = new BookDTO(
                bookId, "Dune", "999", "Sci-Fi Epic", 1965, authorId, "Frank Herbert", 0.0, List.of()
        );
        when(bookService.createBook(any(BookDTO.class))).thenReturn(savedDto);

        mockMvc.perform(post("/api/v1/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Dune")));
    }

    @Test
    @DisplayName("POST /api/v1/books - Stworzenie książki z nieprawidłowymi danymi (400)")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400_whenCreatingInvalidBook() throws Exception {
        BookDTO invalidDto = new BookDTO(
                null,
                null, // title jest null
                "999",
                "Desc",
                2020,
                null, // authorId jest null
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/v1/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).createBook(any());
    }

    @Test
    @DisplayName("POST /api/v1/books - USER nie może stworzyć książki (403)")
    @WithMockUser(roles = "USER")
    void shouldForbidCreate_whenUser() throws Exception {
        BookDTO inputDto = new BookDTO(null, "Hacker Book", "666", "Desc", 2024, UUID.randomUUID(), "Hacker", null, null);

        mockMvc.perform(post("/api/v1/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isForbidden());

        verify(bookService, never()).createBook(any());
    }

    @Test
    @DisplayName("PUT /api/v1/books/{id} - Update książki (Admin)")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateBook() throws Exception {
        UUID id = UUID.randomUUID();

        BookDTO updateDto = new BookDTO(
                id,
                "New Title",
                "111",
                "New Desc",
                2021,
                UUID.randomUUID(),
                "Author Name",
                4.5,
                List.of()
        );

        when(bookService.updateBook(eq(id), any(BookDTO.class))).thenReturn(updateDto);

        mockMvc.perform(put("/api/v1/books/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("New Title")));
    }

    @Test
    @DisplayName("PUT /api/v1/books/{id} - USER nie może zrobić update'u książki (403)")
    @WithMockUser(roles = "USER")
    void shouldForbidUpdate_whenUser() throws Exception {
        UUID id = UUID.randomUUID();
        BookDTO updateDto = new BookDTO(id, "Hacked Title", "666", "Desc", 2024, UUID.randomUUID(), "Hacker", 1.0, null);

        mockMvc.perform(put("/api/v1/books/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());

        verify(bookService, never()).updateBook(any(), any());
    }

    @Test
    @DisplayName("DELETE /api/v1/books/{id} - Usuwa książkę (Admin)")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteBook_whenAdmin() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/books/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(bookService).deleteById(id);
    }

    @Test
    @DisplayName("DELETE /api/v1/books/{id} - USER nie może usunąć książki (403)")
    @WithMockUser(roles = "USER")
    void shouldForbidDelete_whenUser() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/books/{id}", id)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(bookService, never()).deleteById(id);
    }

    @Test
    @DisplayName("GET /search - Przeszukiwanie książek")
    @WithMockUser
    void shouldSearchBooks() throws Exception {
        Book book = createTestBook("Harry Potter", "777");
        Page<Book> result = new PageImpl<>(List.of(book));

        when(bookService.searchBooks(eq("Harry"), any(Pageable.class))).thenReturn(result);

        mockMvc.perform(get("/api/v1/books/search")
                        .param("query", "Harry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Harry Potter")));
    }

    @Test
    @DisplayName("GET /api/v1/books - Niezalogowani mogą widzieć książki (200)")
    void shouldReturnBooks_whenAnonymous() throws Exception {
        Page<Book> emptyPage = new PageImpl<>(List.of());
        when(bookService.findAll(any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/books")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /author/{id} - Filtrowanie po autorze")
    @WithMockUser
    void shouldGetBooksByAuthor() throws Exception {
        UUID authorId = UUID.randomUUID();
        Page<Book> result = new PageImpl<>(List.of(createTestBook("Book A", "1")));

        when(bookService.getBooksByAuthorId(eq(authorId), any(Pageable.class))).thenReturn(result);

        mockMvc.perform(get("/api/v1/books/author/{authorId}", authorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @DisplayName("GET /top - Top ocenione książki")
    @WithMockUser
    void shouldReturnTopBooks() throws Exception {
        List<Book> topBooks = List.of(createTestBook("Best Book", "100"));

        when(bookService.getTopRatedBooksViaSql(5)).thenReturn(topBooks);

        mockMvc.perform(get("/api/v1/books/top")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // m. pomocnicza
    private Book createTestBook(String title, String isbn) {
        Book book = new Book();
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setDescription("Test Desc");
        book.setPublicationYear(2000);

        Author author = new Author();
        author.setId(UUID.randomUUID());
        author.setName("Magda");
        author.setSurname("Gessler");

        book.setAuthor(author);
        return book;
    }
}