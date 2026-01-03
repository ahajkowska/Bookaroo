package org.example.bookaroo.controller.view;

import org.example.bookaroo.config.SecurityConfig;
import org.example.bookaroo.entity.Book;
import org.example.bookaroo.service.BookService;
import org.example.bookaroo.service.ReviewService;
import org.example.bookaroo.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ReviewService reviewService;

    @Test
    @DisplayName("GET /admin/dashboard - USER nie powinien mieć dostępu (403)")
    @WithMockUser(roles = "USER")
    void shouldDenyAccessToUser() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /admin/dashboard - ADMIN powinien mieć dostęp (200)")
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAccessToAdmin() throws Exception {
        when(bookService.findAllList()).thenReturn(Collections.emptyList());
        when(bookService.getAllAuthors()).thenReturn(Collections.emptyList());
        when(userService.getUsersAlphabetically()).thenReturn(Collections.emptyList());
        when(reviewService.getAllReviews()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("books", "authors", "users", "reviews"));
    }

    @Test
    @DisplayName("GET /admin/book/add - Wyświetlenie formularza dodawania książki")
    @WithMockUser(roles = "ADMIN")
    void shouldShowAddBookForm() throws Exception {
        when(bookService.getAllAuthors()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/book/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/book-form"))
                .andExpect(model().attributeExists("book", "authors"));
    }

    @Test
    @DisplayName("POST /admin/book/save - Should save and redirect")
    @WithMockUser(roles = "ADMIN")
    void shouldSaveBook() throws Exception {
        mockMvc.perform(post("/admin/book/save")
                        .with(csrf())
                        .flashAttr("book", new Book()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));

        verify(bookService).save(any(Book.class));
    }

    @Test
    @DisplayName("GET /admin/book/edit/{id} - Should show edit form with data")
    @WithMockUser(roles = "ADMIN")
    void shouldShowEditBookForm() throws Exception {
        UUID id = UUID.randomUUID();
        Book book = new Book();
        book.setId(id);

        when(bookService.findById(id)).thenReturn(Optional.of(book));
        when(bookService.getAllAuthors()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/book/edit/{id}", id))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/book-form"))
                .andExpect(model().attribute("book", hasProperty("id", is(id))));
    }

    @Test
    @DisplayName("GET /admin/book/delete/{id} - Should delete and redirect")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteBook() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/admin/book/delete/{id}", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));

        verify(bookService).deleteById(id);
    }


    @Test
    @DisplayName("GET /admin/author/delete/{id} - Obsługa wyjątków przy usuwaniu autora")
    @WithMockUser(roles = "ADMIN")
    void shouldRedirectWithError_WhenDeleteAuthorFails() throws Exception {
        // próba usunięcia autora, który ma książki
        UUID id = UUID.randomUUID();
        doThrow(new RuntimeException("Cannot delete")).when(bookService).deleteAuthor(id);

        mockMvc.perform(get("/admin/author/delete/{id}", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard?error=cannot_delete_author"));
    }

    @Test
    @DisplayName("GET /admin/user/toggle-lock/{id} - Pomyślne zablokowanie użytkownika")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldToggleUserLock() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(get("/admin/user/toggle-lock/{id}", userId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));

        verify(userService).toggleUserLock(userId, "admin");
    }

    @Test
    @DisplayName("GET /admin/user/toggle-lock/{id} - Powinno obsłużyć próbę SELF_BANa")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldPreventSelfBan() throws Exception {
        UUID myId = UUID.randomUUID();

        doThrow(new IllegalArgumentException("SELF_BAN"))
                .when(userService).toggleUserLock(myId, "admin");

        mockMvc.perform(get("/admin/user/toggle-lock/{id}", myId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard?error=self_ban"));
    }

    @Test
    @DisplayName("GET /admin/review/delete/{id} - Usunięcie recenzji")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteReview() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/admin/review/delete/{id}", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));

        verify(reviewService).deleteReview(id);
    }
}