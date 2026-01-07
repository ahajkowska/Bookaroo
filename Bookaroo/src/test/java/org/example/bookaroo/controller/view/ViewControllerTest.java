package org.example.bookaroo.controller.view;

import org.example.bookaroo.config.SecurityConfig;
import org.example.bookaroo.dto.BookDTO;
import org.example.bookaroo.dto.BookshelfDTO;
import org.example.bookaroo.service.BookService;
import org.example.bookaroo.service.BookshelfService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.example.bookaroo.testutils.WithMockCustomUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ViewController.class)
@Import(SecurityConfig.class)
class ViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private BookshelfService bookshelfService;


    @Test
    @DisplayName("GET / - Niezalogowani widzą listę książek")
    void shouldShowIndexPage_WhenAnonymous() throws Exception {
        List<BookDTO> books = List.of(createMockBookDto(), createMockBookDto());
        when(bookService.findAllList()).thenReturn(books);
        when(bookService.getAllBookAverageRatings()).thenReturn(Collections.emptyMap());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk()) // 200 OK -> bo w SecurityConfig "/" jest permitAll
                .andExpect(view().name("index"))
                .andExpect(model().attribute("books", hasSize(2)))
                .andExpect(model().attributeExists("ratings"))
                .andExpect(model().attributeDoesNotExist("userShelves"));

        verify(bookshelfService, never()).getUserShelvesByUsername(anyString());
    }

    @Test
    @DisplayName("GET / - Zalogowany użytkownik widzi listę książek oraz odpowiednie półki")
    @WithMockCustomUser(username = "magda_gessler")
    void shouldShowIndexPageWithShelves_WhenLoggedIn() throws Exception {
        List<BookDTO> books = List.of(createMockBookDto());
        List<BookshelfDTO> shelves = List.of(new BookshelfDTO(UUID.randomUUID(), "Do przeczytania", true, null), new BookshelfDTO(UUID.randomUUID(), "Ulubione", false, null));

        when(bookService.findAllList()).thenReturn(books);
        when(bookService.getAllBookAverageRatings()).thenReturn(Collections.emptyMap());
        when(bookshelfService.getUserShelvesByUsername("magda_gessler")).thenReturn(shelves);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("books", hasSize(1)))
                .andExpect(model().attribute("userShelves", hasSize(2)));

        verify(bookshelfService).getUserShelvesByUsername("magda_gessler");
    }

    @Test
    @DisplayName("GET /?search=Harry - Filtrowanie po wpisanych wynikach")
    @WithMockCustomUser
    void shouldFilterBooks_WhenSearchParamPresent() throws Exception {
        String query = "Harry";
        List<BookDTO> searchResults = List.of(createMockBookDto());

        when(bookService.searchBooksList(query)).thenReturn(searchResults);
        when(bookService.getAllBookAverageRatings()).thenReturn(Collections.emptyMap());

        mockMvc.perform(get("/")
                        .param("search", query))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("books", hasSize(1)))
                .andExpect(model().attribute("searchQuery", query));

        verify(bookService).searchBooksList(query);
        verify(bookService, never()).findAllList();
    }

    @Test
    @DisplayName("GET /?search= - Zwracanie całej listy książęk, jeśli pole wyszukiwania jest puste")
    void shouldReturnAllBooks_WhenSearchParamIsEmpty() throws Exception {
        when(bookService.findAllList()).thenReturn(List.of(createMockBookDto()));
        when(bookService.getAllBookAverageRatings()).thenReturn(Collections.emptyMap());

        mockMvc.perform(get("/")
                        .param("search", ""))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("searchQuery"));

        verify(bookService).findAllList();
        verify(bookService, never()).searchBooksList(anyString());
    }

    // m. pomocnicza
    private BookDTO createMockBookDto() {
        return new BookDTO(UUID.randomUUID(), "Tytuł", "ISBN", "Opis", 2023, UUID.randomUUID(), "Autor", 0.0, List.of());
    }
}