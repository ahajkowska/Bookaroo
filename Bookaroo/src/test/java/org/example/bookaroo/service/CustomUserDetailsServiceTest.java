package org.example.bookaroo.service;

import org.example.bookaroo.entity.User;
import org.example.bookaroo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;


    @Test
    @DisplayName("should return UserDetails with correct ID when user exists")
    void shouldReturnUserDetailsWithId_whenUserExists() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setUsername("alice");
        user.setPassword("secret");
        user.setRole("USER");
        user.setLocked(false);

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        // When
        UserDetails result = userDetailsService.loadUserByUsername("alice");

        // Then
        assertThat(result).isInstanceOf(CustomUserDetailsService.BookarooUserDetails.class);
        assertThat(((CustomUserDetailsService.BookarooUserDetails) result).getId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("should return UserDetails with correct username and password")
    void shouldMapCredentialsCorrectly() {
        // Given
        User user = new User();
        user.setUsername("magda_gessler");
        user.setPassword("encrypted_pass");
        user.setRole("USER");
        user.setLocked(false);

        when(userRepository.findByUsername("magda_gessler")).thenReturn(Optional.of(user));

        // When
        UserDetails result = userDetailsService.loadUserByUsername("magda_gessler");

        // Then
        assertThat(result.getUsername()).isEqualTo("magda_gessler");
        assertThat(result.getPassword()).isEqualTo("encrypted_pass");
    }

    // ROLE MAPPING

    @Test
    @DisplayName("should add ROLE_ prefix to user role")
    void shouldAddRolePrefix_whenLoadingUser() {
        // Given
        User user = new User();
        user.setUsername("admin");
        user.setPassword("pass");
        user.setRole("ADMIN");
        user.setLocked(false);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        // When
        UserDetails result = userDetailsService.loadUserByUsername("admin");

        // Then
        assertThat(result.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    // LOCKING

    @Test
    @DisplayName("should set accountNonLocked to false when user is locked")
    void shouldReturnLockedAccount_whenUserIsLocked() {
        // Given
        User user = new User();
        user.setUsername("banned");
        user.setPassword("pass");
        user.setRole("USER");
        user.setLocked(true); // ZABLOKOWANY

        when(userRepository.findByUsername("banned")).thenReturn(Optional.of(user));

        // When
        UserDetails result = userDetailsService.loadUserByUsername("banned");

        // Then
        assertThat(result.isAccountNonLocked()).isFalse();
    }

    @Test
    @DisplayName("should set accountNonLocked to true when user is not locked")
    void shouldReturnUnlockedAccount_whenUserIsNotLocked() {
        // Given
        User user = new User();
        user.setUsername("active");
        user.setPassword("pass");
        user.setRole("USER");
        user.setLocked(false); // NIEZABLOKOWANY

        when(userRepository.findByUsername("active")).thenReturn(Optional.of(user));

        // When
        UserDetails result = userDetailsService.loadUserByUsername("active");

        // Then
        assertThat(result.isAccountNonLocked()).isTrue();
    }

    @Test
    @DisplayName("should throw UsernameNotFoundException when user does not exist")
    void shouldThrowException_whenUserNotFound() {
        // Given
        String nonExistentUser = "ghost";
        when(userRepository.findByUsername(nonExistentUser)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(nonExistentUser))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Użytkownik nieznaleziony");
    }
}