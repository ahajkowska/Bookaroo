package org.example.bookaroo.controller.view;

import org.example.bookaroo.config.SecurityConfig;
import org.example.bookaroo.service.BookshelfService;
import org.example.bookaroo.service.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookshelfViewController.class)
@Import(SecurityConfig.class)
class BookshelfViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookshelfService bookshelfService;

    @Test
    @DisplayName("POST /bookshelf/add - Dodawanie książki")
    void shouldAddBook_WhenLoggedIn() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID shelfId = UUID.randomUUID();

        CustomUserDetailsService.BookarooUserDetails mockPrincipal = mock(CustomUserDetailsService.BookarooUserDetails.class);
        when(mockPrincipal.getId()).thenReturn(userId);
        when(mockPrincipal.getUsername()).thenReturn("user");
        when(mockPrincipal.getPassword()).thenReturn("pass");
        when(mockPrincipal.getAuthorities()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/bookshelf/add")
                        .param("bookId", bookId.toString())
                        .param("shelfId", shelfId.toString())
                        .with(user(mockPrincipal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book/" + bookId));

        verify(bookshelfService).addOrMoveBook(userId, bookId, shelfId);
    }


    @Test
    @DisplayName("POST /bookshelf/add - Dodawanie książki (niezalogowany)")
    void shouldNotAddBook_WhenNotLoggedIn() throws Exception {
        // Given
        UUID bookId = UUID.randomUUID();
        UUID shelfId = UUID.randomUUID();

        UserDetails genericUser = org.springframework.security.core.userdetails.User
                .withUsername("generic").password("pass").roles("USER").build();

        mockMvc.perform(post("/bookshelf/add")
                        .param("bookId", bookId.toString())
                        .param("shelfId", shelfId.toString())
                        .with(user(genericUser))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book/" + bookId));

        // serwis NIE powinien zostać wywołany (bo if instanceof zwrócił false)
        verifyNoInteractions(bookshelfService);
    }

    @Test
    @DisplayName("POST /bookshelf/remove - Usuwanie książki (zalogowany)")
    void shouldRemoveBook_WhenLoggedIn() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        CustomUserDetailsService.BookarooUserDetails mockPrincipal = mock(CustomUserDetailsService.BookarooUserDetails.class);
        when(mockPrincipal.getId()).thenReturn(userId);
        when(mockPrincipal.getUsername()).thenReturn("user");
        when(mockPrincipal.getPassword()).thenReturn("pass");
        when(mockPrincipal.getAuthorities()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/bookshelf/remove")
                        .param("bookId", bookId.toString())
                        .with(user(mockPrincipal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile/" + userId))
                .andExpect(flash().attribute("message", "Książka została usunięta z Twojej biblioteczki."));

        verify(bookshelfService).removeBookFromLibrary(userId, bookId);
    }

    @Test
    @DisplayName("POST /bookshelf/remove - Przekierowanie na home, gdy zły typ użytkownika")
    void shouldRedirectHome_WhenUserTypeMismatch() throws Exception {
        UUID bookId = UUID.randomUUID();

        // standardowy UserDetails, który nie jest BookarooUserDetails
        UserDetails genericUser = org.springframework.security.core.userdetails.User
                .withUsername("generic").password("pass").roles("USER").build();

        mockMvc.perform(post("/bookshelf/remove")
                        .param("bookId", bookId.toString())
                        .with(user(genericUser))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verifyNoInteractions(bookshelfService);
    }
}