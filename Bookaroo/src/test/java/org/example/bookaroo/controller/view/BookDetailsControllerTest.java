package org.example.bookaroo.controller.view;

import org.example.bookaroo.config.SecurityConfig;
import org.example.bookaroo.dto.BookDTO;
import org.example.bookaroo.dto.ReviewDTO;
import org.example.bookaroo.entity.Bookshelf;
import org.example.bookaroo.exception.ResourceNotFoundException;
import org.example.bookaroo.service.BookService;
import org.example.bookaroo.service.BookshelfService;
import org.example.bookaroo.service.CustomUserDetailsService;
import org.example.bookaroo.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.Mockito.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.hasKey;

@WebMvcTest(BookDetailsController.class)
@Import(SecurityConfig.class)
class BookDetailsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private BookshelfService bookshelfService;

    // Wyświetlanie szczegółów (Anonim)

    @Test
    @DisplayName("GET /book/{id} - Niezalogowani nie widzą statystyk książki")
    void shouldShowBookDetails_WhenAnonymous() throws Exception {
        UUID bookId = UUID.randomUUID();

        BookDTO bookDto = new BookDTO(
                bookId, "Test Book", "1234567890", "Opis", 2024,
                UUID.randomUUID(), "Jan Kowalski", 4.5, List.of("Fantasy")
        );

        when(bookService.getBookDetails(bookId)).thenReturn(bookDto);
        when(reviewService.getReviewsForBook(bookId)).thenReturn(Collections.emptyList());
        when(bookService.getBookStatistics(bookId)).thenReturn(createMockStats());

        mockMvc.perform(get("/book/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(view().name("book-details"))
                .andExpect(model().attribute("book", notNullValue()))
                .andExpect(model().attribute("stats", hasKey("ratingDistribution")));

        verifyNoInteractions(bookshelfService);
    }

    // Wyświetlanie szczegółów (Zalogowany)

    @Test
    @DisplayName("GET /book/{id} - Wyświetlanie szczegółów zalogowanym użytkownikom")
    void shouldShowBookDetailsWithShelves_WhenLoggedIn() throws Exception {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BookDTO bookDto = new BookDTO(
                bookId, "Test Book", "1234567890", "Opis", 2024,
                UUID.randomUUID(), "Jan Kowalski", 4.5, List.of("Fantasy")
        );

        CustomUserDetailsService.BookarooUserDetails mockPrincipal = mock(CustomUserDetailsService.BookarooUserDetails.class);
        when(mockPrincipal.getId()).thenReturn(userId);
        when(mockPrincipal.getUsername()).thenReturn("user");
        when(mockPrincipal.getPassword()).thenReturn("pass");
        when(mockPrincipal.getAuthorities()).thenReturn(Collections.emptyList());

        when(bookService.getBookDetails(bookId)).thenReturn(bookDto);
        when(reviewService.getReviewsForBook(bookId)).thenReturn(Collections.emptyList());
        when(bookService.getBookStatistics(bookId)).thenReturn(createMockStats());

        when(bookshelfService.getUserShelves(userId)).thenReturn(List.of(new Bookshelf()));
        when(bookshelfService.getShelfNameForBook(userId, bookId)).thenReturn("Do przeczytania");

        mockMvc.perform(get("/book/{id}", bookId)
                        .with(user(mockPrincipal)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("userShelves"))
                .andExpect(model().attribute("currentShelfName", is("Do przeczytania")));
    }

    @Test
    @DisplayName("GET /book/{id} - Książka nieznaleziona (404)")
    void shouldReturnError_WhenBookNotFound() throws Exception {
        UUID bookId = UUID.randomUUID();

        when(bookService.getBookDetails(bookId))
                .thenThrow(new ResourceNotFoundException("Book", "id", bookId));

        mockMvc.perform(get("/book/{id}", bookId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /review/add - Dodanie recenzji")
    void shouldAddReview_WhenLoggedIn() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        // Mockowanie użytkownika
        CustomUserDetailsService.BookarooUserDetails mockPrincipal = mock(CustomUserDetailsService.BookarooUserDetails.class);
        when(mockPrincipal.getId()).thenReturn(userId);
        when(mockPrincipal.getUsername()).thenReturn("user");
        when(mockPrincipal.getPassword()).thenReturn("pass");
        when(mockPrincipal.getAuthorities()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/review/add")
                        .param("bookId", bookId.toString())
                        .param("rating", "5")
                        .param("content", "Super!")
                        .with(user(mockPrincipal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book/" + bookId));

        verify(reviewService).addReview(eq(userId), any(ReviewDTO.class));
    }

    @Test
    @DisplayName("POST /review/add - Nie powinno dodać recenzji, gdy zły user")
    void shouldNotAddReview_WhenGenericUser() throws Exception {
        UUID bookId = UUID.randomUUID();
        UserDetails genericUser = org.springframework.security.core.userdetails.User
                .withUsername("generic").password("pass").roles("USER").build();

        mockMvc.perform(post("/review/add")
                        .param("bookId", bookId.toString())
                        .param("rating", "1")
                        .param("content", "Bad")
                        .with(user(genericUser))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book/" + bookId));

        verifyNoInteractions(reviewService);
    }

    // m. pomocnicza
    private Map<String, Object> createMockStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("avgRating", 4.5);
        stats.put("ratingCount", 10);

        Map<Integer, Integer> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 2); // po 2 głosy na każdą gwiazdkę
        }
        stats.put("ratingDistribution", distribution);

        return stats;
    }
}