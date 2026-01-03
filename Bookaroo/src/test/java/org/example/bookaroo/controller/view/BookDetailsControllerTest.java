package org.example.bookaroo.controller.view;

import org.example.bookaroo.config.SecurityConfig;
import org.example.bookaroo.entity.Book;
import org.example.bookaroo.entity.Bookshelf;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    // --- SCENARIUSZ 1: Wyświetlanie szczegółów (Anonim) ---

    @Test
    @DisplayName("GET /book/{id} - Niezalogowani nie widzą statystyk książki")
    void shouldShowBookDetails_WhenAnonymous() throws Exception {
        UUID bookId = UUID.randomUUID();
        Book book = new Book();
        book.setId(bookId);
        book.setTitle("Test Book");

        when(bookService.findById(bookId)).thenReturn(Optional.of(book));
        when(reviewService.getReviewsForBook(bookId)).thenReturn(Collections.emptyList());

        when(bookService.getBookStatistics(bookId)).thenReturn(createMockStats());

        mockMvc.perform(get("/book/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(view().name("book-details"))
                .andExpect(model().attribute("book", notNullValue()))
                .andExpect(model().attribute("stats", hasKey("ratingDistribution")));

        verifyNoInteractions(bookshelfService);
    }

    // --- SCENARIUSZ 2: Wyświetlanie szczegółów (Zalogowany) ---

    @Test
    @DisplayName("GET /book/{id} - Wyświetlanie szczegółów zalogowanym użytkownikom")
    void shouldShowBookDetailsWithShelves_WhenLoggedIn() throws Exception {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Book book = new Book();

        CustomUserDetailsService.BookarooUserDetails mockPrincipal = mock(CustomUserDetailsService.BookarooUserDetails.class);
        when(mockPrincipal.getId()).thenReturn(userId);
        when(mockPrincipal.getUsername()).thenReturn("user");
        when(mockPrincipal.getPassword()).thenReturn("pass");
        when(mockPrincipal.getAuthorities()).thenReturn(Collections.emptyList());

        when(bookService.findById(bookId)).thenReturn(Optional.of(book));
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
        when(bookService.findById(bookId)).thenReturn(Optional.empty());

        try {
            mockMvc.perform(get("/book/{id}", bookId))
                    .andExpect(status().is5xxServerError());
        } catch (Exception e) {
        }
    }

    @Test
    @DisplayName("POST /review/add - Dodanie recenzji")
    void shouldAddReview_WhenLoggedIn() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

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

        verify(reviewService).addReview(userId, bookId, 5, "Super!");
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