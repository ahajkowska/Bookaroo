package org.example.bookaroo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.bookaroo.dto.CreateUserDTO;
import org.example.bookaroo.dto. UpdateUserDTO;
import org.example.bookaroo.dto.UserBackupDTO;
import org. example.bookaroo.dto.UserDTO;
import org.example.bookaroo.entity.*;
import org.example.bookaroo.exception.ResourceNotFoundException;
import org.example.bookaroo. exception.UserAlreadyExistsException;
import org.example.bookaroo.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain. Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java. util.List;
import java.util.Map;
import java. util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BookshelfService bookshelfService;
    private final FileStorageService fileStorageService;
    private final StatisticsRepository statisticsRepository;
    private final BookshelfRepository bookshelfRepository;
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       BookshelfService bookshelfService, FileStorageService fileStorageService,
                       StatisticsRepository statisticsRepository, BookshelfRepository bookshelfRepository,
                       BookRepository bookRepository, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.bookshelfService = bookshelfService;
        this.fileStorageService = fileStorageService;
        this.statisticsRepository = statisticsRepository;
        this.bookshelfRepository = bookshelfRepository;
        this.bookRepository = bookRepository;
        this.reviewRepository = reviewRepository;
    }

    // GET ALL - z paginacją
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    // GET BY ID
    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return convertToDTO(user);
    }

    // CREATE USER
    @Transactional
    public UserDTO createUser(CreateUserDTO createUserDTO) {
        // czy email już istnieje
        if (userRepository.existsByEmail(createUserDTO.getEmail())) {
            throw new UserAlreadyExistsException("Email " + createUserDTO.getEmail() + " jest już zajęty");
        }

        // czy username już istnieje
        if (userRepository.existsByUsername(createUserDTO.getUsername())) {
            throw new UserAlreadyExistsException("Username " + createUserDTO.getUsername() + " jest już zajęty");
        }

        // stwórz nowego użytkownika
        User user = new User();
        user.setUsername(createUserDTO.getUsername());
        user.setEmail(createUserDTO.getEmail());
        user.setPassword(passwordEncoder.encode(createUserDTO.getPassword())); // Zaszyfruj hasło
        user.setRole(createUserDTO.getRole());
        user.setAvatar(createUserDTO.getAvatar());
        user.setBio(createUserDTO.getBio());

        // generowanie domyślnych półek
        user.setBookshelves(bookshelfService.generateDefaultShelves(user));

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    // UPDATE USER
    @Transactional
    public UserDTO updateUser(UUID id, UpdateUserDTO updateUserDTO) {
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
        return convertToDTO(updatedUser);
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
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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

    @Transactional(readOnly = true)
    public UserBackupDTO exportUserData(String username) {
        User user = findByUsername(username);

        var shelfDtos = user.getBookshelves().stream().map(shelf -> new org.example.bookaroo.dto.ShelfBackupDTO(
                shelf.getName(),
                shelf.getBooks().stream()
                        .map(Book::getIsbn)
                        .toList()
        )).toList();

        var reviewDtos = user.getGivenReviews().stream().map(review -> new org.example.bookaroo.dto.ReviewBackupDTO(
                review.getBook().getIsbn(),
                review.getContent(),
                review.getRating()
        )).toList();

        return new UserBackupDTO(user.getBio(), user.getAvatar(), shelfDtos, reviewDtos);
    }


    @Transactional
    public void importUserData(String username, MultipartFile file) throws IOException {
        User user = findByUsername(username);

        UserBackupDTO backupDto = new ObjectMapper().readValue(file.getInputStream(), UserBackupDTO.class);

        if (backupDto.bio() != null) user.setBio(backupDto.bio());
        if (backupDto.avatar() != null) user.setAvatar(backupDto.avatar());

        // Import Półek
        if (backupDto.shelves() != null) {
            for (var shelfDto : backupDto.shelves()) {
                Bookshelf shelf = user.getBookshelves().stream()
                        .filter(s -> s.getName().equalsIgnoreCase(shelfDto.name()))
                        .findFirst()
                        .orElseGet(() -> {
                            Bookshelf newShelf = new Bookshelf();
                            newShelf.setName(shelfDto.name());
                            newShelf.setUser(user);
                            newShelf.setIsDefault(false);
                            return bookshelfRepository.save(newShelf);
                        });

                for (String isbn : shelfDto.bookIsbns()) {
                    bookRepository.findByIsbn(isbn).ifPresent(book -> {
                        boolean alreadyOnShelf = shelf.getBooks().stream()
                                .anyMatch(b -> b.getId().equals(book.getId()));

                        if (!alreadyOnShelf) {
                            shelf.addBook(book);
                        }
                    });
                }
                bookshelfRepository.save(shelf);
            }
        }

        if (backupDto.reviews() != null) {
            for (var reviewDto : backupDto.reviews()) {
                bookRepository.findByIsbn(reviewDto.bookIsbn()).ifPresent(book -> {
                    boolean reviewExists = user.getGivenReviews().stream()
                            .anyMatch(r -> r.getBook().getId().equals(book.getId()));

                    if (!reviewExists) {
                        Review review = new Review();
                        review.setUser(user);
                        review.setBook(book);
                        review.setContent(reviewDto.content());
                        review.setRating(reviewDto.rating());
                        reviewRepository.save(review);
                    }
                });
            }
        }

        userRepository.save(user);
    }

    // f. pomocnicze
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

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    public Map<String, Object> getUserStats(UUID userId) {
        return statisticsRepository.getUserStats(userId);
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