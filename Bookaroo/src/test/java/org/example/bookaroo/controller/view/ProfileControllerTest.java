package org.example.bookaroo.controller.view;

import org.example.bookaroo.config.SecurityConfig;
import org.example.bookaroo.dto.UserBackupDTO;
import org.example.bookaroo.entity.Bookshelf;
import org.example.bookaroo.entity.User;
import org.example.bookaroo.service.BookshelfService;
import org.example.bookaroo.service.CustomUserDetailsService;
import org.example.bookaroo.service.UserService;
import org.example.bookaroo.testutils.WithMockCustomUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@Import(SecurityConfig.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private BookshelfService bookshelfService;

    // --- SCENARIUSZ 1: Wyświetlanie profilu (Właściciel) ---

    @Test
    @DisplayName("GET /profile/{id} - Wyświetlenie profilu")
    @WithMockCustomUser(username = "magdaGessler", id = "11111111-1111-1111-1111-111111111111")
    void shouldShowProfile_WhenOwner() throws Exception {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        User user = new User();
        user.setId(userId);
        user.setUsername("magdaGessler");

        when(userService.findById(userId)).thenReturn(user);
        when(bookshelfService.getUserShelves(userId)).thenReturn(List.of(new Bookshelf()));
        when(userService.getUserStats(userId)).thenReturn(Map.of("readCount", 10));

        mockMvc.perform(get("/profile/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attribute("user", notNullValue()))
                .andExpect(model().attribute("isOwner", true));
    }

    // --- SCENARIUSZ 2: Wyświetlanie profilu (Gość) ---

    @Test
    @DisplayName("GET /profile/{id} - Wyświetlenie profilu kogoś innego")
    @WithMockCustomUser(username = "adamMalysz")
    void shouldShowProfile_WhenVisitor() throws Exception {
        UUID magdaGesslerId = UUID.randomUUID();
        User magdaGessler = new User();
        magdaGessler.setId(magdaGesslerId);
        magdaGessler.setUsername("magdaGessler");

        when(userService.findById(magdaGesslerId)).thenReturn(magdaGessler);
        when(bookshelfService.getUserShelves(magdaGesslerId)).thenReturn(Collections.emptyList());
        when(userService.getUserStats(magdaGesslerId)).thenReturn(Collections.emptyMap());

        mockMvc.perform(get("/profile/{userId}", magdaGesslerId))
                .andExpect(status().isOk())
                .andExpect(model().attribute("user", hasProperty("username", is("magdaGessler"))))
                .andExpect(model().attribute("isOwner", false));
    }

    @Test
    @DisplayName("GET /profile/edit - Edycja profilu")
    @WithMockCustomUser(username = "magdaGessler")
    void shouldShowEditForm() throws Exception {
        User magdaGessler = new User();
        magdaGessler.setUsername("magdaGessler");
        when(userService.findByUsername("magdaGessler")).thenReturn(magdaGessler);

        mockMvc.perform(get("/profile/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile-edit"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @DisplayName("POST /profile/update - Aktualizacja porfilu (bio i avatar)")
    @WithMockCustomUser(username = "magdaGessler")
    void shouldUpdateProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        User magdaGessler = new User();
        magdaGessler.setId(userId);
        magdaGessler.setUsername("magdaGessler");

        MockMultipartFile avatar = new MockMultipartFile("avatar", "pic.jpg", "image/jpeg", "bytes".getBytes());

        when(userService.findByUsername("magdaGessler")).thenReturn(magdaGessler);

        mockMvc.perform(multipart("/profile/update")
                        .file(avatar)
                        .param("bio", "New Bio")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile/" + userId));

        verify(userService).updateUserProfile(eq("magdaGessler"), eq("New Bio"), any());
    }

    @Test
    @DisplayName("POST /profile/shelves/create - Tworzenie półki")
    void shouldCreateShelf_WhenAuthenticated() throws Exception {
        UUID userId = UUID.randomUUID();
        String shelfName = "Nowa Półka";

        CustomUserDetailsService.BookarooUserDetails mockPrincipal = mock(CustomUserDetailsService.BookarooUserDetails.class);
        when(mockPrincipal.getId()).thenReturn(userId);
        when(mockPrincipal.getUsername()).thenReturn("magdaGessler");
        when(mockPrincipal.getAuthorities()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/profile/shelves/create")
                        .param("name", shelfName)
                        .with(user(mockPrincipal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile/" + userId));

        verify(bookshelfService).createCustomShelf(userId, shelfName);
    }

    @Test
    @DisplayName("GET /profile/export - Robi backup (JSON)")
    @WithMockCustomUser(username = "magdaGessler")
    void shouldExportProfile() throws Exception {
        UserBackupDTO backup = new UserBackupDTO(
                "magdaGessler",
                "user@test.com",
                Collections.emptyList(),
                Collections.emptyList()
        );

        when(userService.exportUserData("magdaGessler")).thenReturn(backup);

        mockMvc.perform(get("/profile/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=backup_magdaGessler.json")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("POST /profile/import - Import danych")
    @WithMockCustomUser(username = "magdaGessler")
    void shouldImportProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        MockMultipartFile file = new MockMultipartFile("file", "backup.json", "application/json", "{}".getBytes());

        when(userService.findByUsername("magdaGessler")).thenReturn(user);

        mockMvc.perform(multipart("/profile/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile/" + userId + "?success=restored"));

        verify(userService).importUserData(eq("magdaGessler"), any());
    }

    @Test
    @DisplayName("POST /profile/challenge/update - Aktualizuje challenge czytelniczy")
    @WithMockCustomUser(username = "magdaGessler")
    void shouldUpdateChallenge() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        when(userService.findByUsername("magdaGessler")).thenReturn(user);

        mockMvc.perform(post("/profile/challenge/update")
                        .param("target", "50")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile/" + userId));

        verify(userService).updateReadingChallenge("magdaGessler", 50);
    }
}