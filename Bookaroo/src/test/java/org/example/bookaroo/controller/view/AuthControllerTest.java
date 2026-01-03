package org.example.bookaroo.controller.view;

import org.example.bookaroo.config.SecurityConfig;
import org.example.bookaroo.dto.CreateUserDTO;
import org.example.bookaroo.dto.UserDTO;
import org.example.bookaroo.exception.UserAlreadyExistsException;
import org.example.bookaroo.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("GET /register - Wyświetlenie formularza rejestracji")
    void shouldShowRegistrationForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @DisplayName("POST /register - Pomyślne zarejestrowanie usera")
    void shouldRegisterUser_WhenDataIsValid() throws Exception {
        when(userService.createUser(any(CreateUserDTO.class))).thenReturn(new UserDTO());

        // When & Then
        mockMvc.perform(post("/register")
                        .param("username", "newUser")
                        .param("email", "new@test.com")
                        .param("password", "superHaslo123@")
                        .param("role", "USER")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        verify(userService).createUser(any(CreateUserDTO.class));
    }

    @Test
    @DisplayName("POST /register - Powrót do formularza przy błędzie walidacji w rejestracji")
    void shouldReturnForm_WhenValidationFails() throws Exception {

        mockMvc.perform(post("/register")
                        .param("username", "")
                        .param("email", "zly-format-email")
                        .param("password", "")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("user", "username"))
                .andExpect(model().attributeHasFieldErrors("user", "email"));

        // serwis nie powinien być wołany przy błędzie walidacji
        verify(userService, never()).createUser(any());
    }

    @Test
    @DisplayName("POST /register - Użytkownik już istnieje (Wyjątek z serwisu)")
    void shouldReturnForm_WhenUserAlreadyExists() throws Exception {
        doThrow(new UserAlreadyExistsException("Taki użytkownik już istnieje"))
                .when(userService).createUser(any(CreateUserDTO.class));

        mockMvc.perform(post("/register")
                        .param("username", "existingUser")
                        .param("email", "exist@test.com")
                        .param("password", "superHaslo123@")
                        .param("role", "USER")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("errorMessage", "Taki użytkownik już istnieje"));
    }

    @Test
    @DisplayName("GET /login - Wyświetla stronę logowania")
    void shouldShowLoginForm() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }
}