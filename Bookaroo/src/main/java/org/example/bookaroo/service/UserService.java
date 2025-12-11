package org.example.bookaroo.service;

import org.example.bookaroo.dto.CreateUserDTO;
import org.example.bookaroo.dto. UpdateUserDTO;
import org. example.bookaroo.dto.UserDTO;
import org. example.bookaroo.entity.User;
import org.example.bookaroo.exception.ResourceNotFoundException;
import org.example.bookaroo. exception.UserAlreadyExistsException;
import org.example.bookaroo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain. Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java. util.List;
import java. util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // GET ALL - z paginacją
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    // GET ALL - bez paginacji
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository. findAll(). stream()
                .map(this::convertToDTO)
                . collect(Collectors.toList());
    }

    // GET BY ID
    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return convertToDTO(user);
    }

    // GET BY USERNAME
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return convertToDTO(user);
    }

    // GET BY EMAIL
    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return convertToDTO(user);
    }

    // CREATE USER
    @Transactional
    public UserDTO createUser(CreateUserDTO createUserDTO) {
        // Sprawdź, czy email już istnieje
        if (userRepository.existsByEmail(createUserDTO.getEmail())) {
            throw new UserAlreadyExistsException("Email " + createUserDTO.getEmail() + " jest już zajęty");
        }

        // Sprawdź, czy username już istnieje
        if (userRepository.existsByUsername(createUserDTO.getUsername())) {
            throw new UserAlreadyExistsException("Username " + createUserDTO.getUsername() + " jest już zajęty");
        }

        // Stwórz nowego użytkownika
        User user = new User();
        user.setUsername(createUserDTO.getUsername());
        user.setEmail(createUserDTO.getEmail());
        user.setPassword(passwordEncoder.encode(createUserDTO.getPassword())); // Zaszyfruj hasło
        user.setRole(createUserDTO.getRole());
        user.setAvatar(createUserDTO.getAvatar());
        user.setBio(createUserDTO.getBio());

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    // UPDATE USER
    @Transactional
    public UserDTO updateUser(UUID id, UpdateUserDTO updateUserDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Aktualizuj tylko pola, które przyszły (nie są null)
        if (updateUserDTO.getEmail() != null) {
            // Sprawdź, czy nowy email nie jest już zajęty przez innego użytkownika
            userRepository.findByEmail(updateUserDTO.getEmail())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(id)) {
                            throw new UserAlreadyExistsException("Email " + updateUserDTO.getEmail() + " jest już zajęty");
                        }
                    });
            user.setEmail(updateUserDTO.getEmail());
        }

        if (updateUserDTO.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(updateUserDTO.getPassword()));
        }

        if (updateUserDTO.getAvatar() != null) {
            user.setAvatar(updateUserDTO.getAvatar());
        }

        if (updateUserDTO.getBio() != null) {
            user.setBio(updateUserDTO.getBio());
        }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    // DELETE USER
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        userRepository.delete(user);
    }

    // GET USERS BY REGISTRATION DATE (newest first)
    @Transactional(readOnly = true)
    public List<UserDTO> getNewestUsers(int limit) {
        return userRepository.findAllByOrderByCreatedAtDesc().stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // GET USERS BY USERNAME (alphabetically)
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersAlphabetically() {
        return userRepository.findAllByOrderByUsernameAsc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // f. pomocnicza --- Konwersja Entity -> DTO
    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatar(),
                user.getBio(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}