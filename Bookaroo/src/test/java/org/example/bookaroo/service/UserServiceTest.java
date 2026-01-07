package org.example.bookaroo.service;

import org.example.bookaroo.dto.CreateUserDTO;
import org.example.bookaroo.dto.UpdateUserDTO;
import org.example.bookaroo.dto.UserDTO;
import org.example.bookaroo.entity.User;
import org.example.bookaroo.exception.ResourceNotFoundException;
import org.example.bookaroo.exception.UserAlreadyExistsException;
import org.example.bookaroo.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private BookshelfService bookshelfService;
    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("should return non-empty page when users exist")
    void shouldReturnNonEmptyPage_whenUsersExist() {
        // Given
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user));

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // When
        Page<UserDTO> result = userService.getAllUsers(pageable);

        // Then
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("should map user username correctly in getAllUsers")
    void shouldMapUsernameCorrectly_inGetAllUsers() {
        // Given
        User user = new User();
        user.setUsername("testuser");
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(user)));

        // When
        Page<UserDTO> result = userService.getAllUsers(pageable);

        // Then
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("testuser");
    }

    // --- GET USER BY ID ---

    @Test
    @DisplayName("should return correct user ID when user exists")
    void shouldReturnCorrectId_whenUserExists() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        UserDTO result = userService.getUserById(userId);

        // Then
        assertThat(result.getId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when user does not exist")
    void shouldThrowException_whenUserNotFound() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("should return created user username when data is valid")
    void shouldReturnUsername_whenUserCreated() {
        // Given
        CreateUserDTO dto = new CreateUserDTO("newUser", "email@test.com", "password", null, null, null);

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPass");
        when(bookshelfService.generateDefaultShelves(any())).thenReturn(Collections.emptyList());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        // When
        UserDTO result = userService.createUser(dto);

        // Then
        assertThat(result.getUsername()).isEqualTo("newUser");
    }

    @Test
    @DisplayName("should call repository save when creating user")
    void shouldCallSave_whenCreatingUser() {
        // Given
        CreateUserDTO dto = new CreateUserDTO("newUser", "email@test.com", "password", null, null, null);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        userService.createUser(dto);

        // Then
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("should throw exception when email is already taken")
    void shouldThrowException_whenEmailTaken() {
        // Given
        CreateUserDTO dto = new CreateUserDTO("user", "taken@email.com", "pass", null, null, null);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    @DisplayName("should not save user when email is taken")
    void shouldNotSave_whenEmailTaken() {
        // Given
        CreateUserDTO dto = new CreateUserDTO("user", "taken@email.com", "pass", null, null, null);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        // When
        try { userService.createUser(dto); } catch (Exception e) {}

        // Then
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw exception when username is already taken")
    void shouldThrowException_whenUsernameTaken() {
        // Given
        CreateUserDTO dto = new CreateUserDTO("takenUser", "email@test.com", "pass", null, null, null);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    @DisplayName("should update email in DTO result")
    void shouldReturnUpdatedEmail_whenUpdateSuccessful() {
        // Given
        UUID userId = UUID.randomUUID();
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("old@email.com");

        UpdateUserDTO updateDto = new UpdateUserDTO("new@email.com", null, null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("new@email.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserDTO result = userService.updateUser(userId, updateDto);

        // Then
        assertThat(result.getEmail()).isEqualTo("new@email.com");
    }

    @Test
    @DisplayName("should encode password when password is updated")
    void shouldEncodePassword_whenUpdatingPassword() {
        // Given
        UUID userId = UUID.randomUUID();
        User existingUser = new User();
        existingUser.setId(userId);
        UpdateUserDTO updateDto = new UpdateUserDTO(null, "newPass", null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        userService.updateUser(userId, updateDto);

        // Then
        verify(passwordEncoder).encode("newPass");
    }

    @Test
    @DisplayName("should throw exception when updating to occupied email")
    void shouldThrowException_whenEmailOccupiedByOther() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        User currentUser = new User();
        currentUser.setId(userId);

        User otherUser = new User();
        otherUser.setId(otherUserId);

        UpdateUserDTO updateDto = new UpdateUserDTO("taken@email.com", null, null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByEmail("taken@email.com")).thenReturn(Optional.of(otherUser));

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(userId, updateDto))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    @DisplayName("should call delete on repository when user exists")
    void shouldDeleteUser_whenExists() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("should update bio when provided")
    void shouldUpdateBio_whenProvided() {
        // Given
        String username = "user1";
        User user = new User();
        user.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        userService.updateUserProfile(username, "New Bio", null);

        // Then
        assertThat(user.getBio()).isEqualTo("New Bio");
    }

    @Test
    @DisplayName("should save file when file is provided")
    void shouldSaveFile_whenFileProvided() {
        // Given
        String username = "user1";
        User user = new User();
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        userService.updateUserProfile(username, "Bio", file);

        // Then
        verify(fileStorageService).saveFile(file);
    }

    @Test
    @DisplayName("should set locked to true when locking user")
    void shouldLockUser_whenToggled() {
        // Given
        UUID targetId = UUID.randomUUID();
        User targetUser = new User();
        targetUser.setLocked(false);
        targetUser.setUsername("target");

        when(userRepository.findById(targetId)).thenReturn(Optional.of(targetUser));

        // When
        userService.toggleUserLock(targetId, "admin");

        // Then
        assertThat(targetUser.isLocked()).isTrue();
    }

    @Test
    @DisplayName("should throw exception on self ban attempt")
    void shouldThrowException_whenSelfBan() {
        // Given
        UUID adminId = UUID.randomUUID();
        User admin = new User();
        admin.setUsername("admin");
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

        // When & Then
        assertThatThrownBy(() -> userService.toggleUserLock(adminId, "admin"))
                .isInstanceOf(IllegalArgumentException.class);
    }

}