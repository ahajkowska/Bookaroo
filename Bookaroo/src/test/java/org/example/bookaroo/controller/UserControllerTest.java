package org.example.bookaroo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.bookaroo.config.SecurityConfig;
import org.example.bookaroo.dto.CreateUserDTO;
import org.example.bookaroo.dto.UpdateUserDTO;
import org.example.bookaroo.dto.UserDTO;
import org.example.bookaroo.exception.UserAlreadyExistsException;
import org.example.bookaroo.service.BookshelfService;
import org.example.bookaroo.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf; // <--- WAŻNE
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private BookshelfService bookshelfService;

    @Test
    @DisplayName("GET /api/v1/users - Powinno zwrócić listę użytkowników (zalogowany)")
    @WithMockUser
    void shouldReturnPageOfUsers() throws Exception {
        Page<UserDTO> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(userService.getAllUsers(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /users - niezalogowany może stworzyć konto")
    void shouldCreateUser_whenAnonymous() throws Exception {
        CreateUserDTO createDto = new CreateUserDTO("nowyUser", "new@test.com", "superHaslo123@", "USER", null, "Hi");
        UUID newId = UUID.randomUUID();
        UserDTO createdDto = new UserDTO(newId, "nowyUser", "new@test.com", null, "Hi", "USER", LocalDateTime.now(), false);

        when(userService.createUser(any(CreateUserDTO.class))).thenReturn(createdDto);

        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/users/" + newId)));
    }

    @Test
    @DisplayName("POST /users - Zwraca konflikt gdy powtarza się email")
    void shouldReturnConflict_whenUserAlreadyExists() throws Exception {
        CreateUserDTO createDto = new CreateUserDTO("existing", "exist@test.com", "superHaslo123@", "USER", null, null);

        when(userService.createUser(any(CreateUserDTO.class)))
                .thenThrow(new UserAlreadyExistsException("Email zajęty"));

        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("DELETE /users/{id} - ADMIN może usunąć użytkownika (204)")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteUser_whenAdmin() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/users/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(id);
    }

    @Test
    @DisplayName("DELETE /users/{id} - USER nie może usunąć użytkownika (403)")
    @WithMockUser(roles = "USER")
    void shouldForbidDelete_whenRegularUser() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/users/{id}", id)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(userService, never()).deleteUser(id);
    }

    @Test
    @DisplayName("PUT /users/{id} - USER nie może aktualizować użytkownika (403)")
    @WithMockUser(roles = "USER")
    void shouldForbidUpdate_whenRegularUser() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateUserDTO updateDto = new UpdateUserDTO("mail@mail.com", null, null, null);

        mockMvc.perform(put("/api/v1/users/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }
}