package org.example.bookaroo.service;

import jakarta.validation.Valid;
import org.example.bookaroo.dto.CreateUserDTO;
import org.example.bookaroo.dto. UpdateUserDTO;
import org. example.bookaroo.dto.UserDTO;
import org.example.bookaroo.dto.UserStatisticsDTO;
import org.example.bookaroo.dto.mapper.UserMapper;
import org.example.bookaroo.entity.*;
import org.example.bookaroo.exception.ResourceNotFoundException;
import org.example.bookaroo. exception.UserAlreadyExistsException;
import org.example.bookaroo.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain. Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java. util.List;
import java. util.UUID;

@Service
@Validated
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BookshelfService bookshelfService;
    private final FileStorageService fileStorageService;
    private final StatisticsRepository statisticsRepository;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       BookshelfService bookshelfService, FileStorageService fileStorageService,
                       StatisticsRepository statisticsRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.bookshelfService = bookshelfService;
        this.fileStorageService = fileStorageService;
        this.statisticsRepository = statisticsRepository;

    }

    // GET ALL - z paginacją
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserMapper::toDto);
    }

    // GET BY ID
    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return UserMapper.toDto(user);
    }

    // CREATE USER
    @Transactional
    public UserDTO createUser(@Valid CreateUserDTO createUserDTO) {
        // czy email już istnieje
        if (userRepository.existsByEmail(createUserDTO.getEmail())) {
            throw new UserAlreadyExistsException("Email " + createUserDTO.getEmail() + " jest już zajęty");
        }

        // czy username już istnieje
        if (userRepository.existsByUsername(createUserDTO.getUsername())) {
            throw new UserAlreadyExistsException("Username " + createUserDTO.getUsername() + " jest już zajęty");
        }

        // stwórz nowego użytkownika
        User user = UserMapper.toEntity(createUserDTO);

        user.setPassword(passwordEncoder.encode(createUserDTO.getPassword()));

        // generowanie domyślnych półek
        user.setBookshelves(bookshelfService.generateDefaultShelves(user));

        User savedUser = userRepository.save(user);
        return UserMapper.toDto(savedUser);
    }

    // UPDATE USER
    @Transactional
    public UserDTO updateUser(UUID id, @Valid UpdateUserDTO updateUserDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (updateUserDTO.getEmail() != null) {
            // czy nowy email nie jest już zajęty przez innego użytkownika
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
        return UserMapper.toDto(updatedUser);
    }

    // DELETE USER
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        userRepository.delete(user);
    }

    // GET USERS BY USERNAME (alphabetically)
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersAlphabetically() {
        return userRepository.findAllByOrderByUsernameAsc().stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Transactional
    public void updateUserProfile(String username, String bio, MultipartFile avatarFile) {
        User user = findByUsername(username);
        user.setBio(bio);

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String avatarUrl = fileStorageService.saveFile(avatarFile);
            user.setAvatar(avatarUrl);
        }
        userRepository.save(user);
    }

    @Transactional
    public void updateReadingChallenge(String username, Integer target) {
        User user = findByUsername(username);
        if (target == null || target < 1) {
            user.setReadingChallengeTarget(null);
        } else {
            user.setReadingChallengeTarget(target);
        }
        userRepository.save(user);
    }

    // f. pomocnicze
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    public UserStatisticsDTO getUserStats(UUID userId) {
        int currentYear = java.time.LocalDate.now().getYear();

        String challengeShelfName = "przeczytane";

        int readCount = 0;

        try {
            Integer result = statisticsRepository.countBooksOnShelfInYear(userId, challengeShelfName, currentYear);
            if (result != null) {
                readCount = result;
            }
        } catch (Exception e) {
            System.err.println("Nie udało się pobrać statystyk czytania: " + e.getMessage());
            readCount = 0; // w razie błędu do pokazania 0
        }

        return new UserStatisticsDTO(readCount, currentYear);
    }

    @Transactional
    public void toggleUserLock(UUID userId, String currentAdminUsername) {
        User user = findById(userId);

        if (user.getUsername().equals(currentAdminUsername)) {
            throw new IllegalArgumentException("SELF_BAN");
        }

        user.setLocked(!user.isLocked());
        userRepository.save(user);
    }
}