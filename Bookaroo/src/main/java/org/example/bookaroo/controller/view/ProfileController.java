package org.example.bookaroo.controller.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.example.bookaroo.dto.UserBackupDTO;
import org.example.bookaroo.entity.Book;
import org.example.bookaroo.entity.Bookshelf;
import org.example.bookaroo.entity.User;
import org.example.bookaroo.exception.ResourceNotFoundException;
import org.example.bookaroo.repository.*;
import org.example.bookaroo.service.FileStorageService;
import org.example.bookaroo.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final StatisticsRepository statisticsRepository;
    private final FileStorageService fileStorageService;

    // do backupu
    private final BookshelfRepository bookshelfRepository;
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;

    public ProfileController(UserRepository userRepository, UserService userService, StatisticsRepository statisticsRepository, FileStorageService fileStorageService, BookshelfRepository bookshelfRepository, BookRepository bookRepository, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.statisticsRepository = statisticsRepository;
        this.fileStorageService = fileStorageService;
        this.bookshelfRepository = bookshelfRepository;
        this.bookRepository = bookRepository;
        this.reviewRepository = reviewRepository;
    }

    // profil użytkownika
    @GetMapping("/profile/{userId}")
    public String showProfile(@PathVariable UUID userId, Model model, @AuthenticationPrincipal UserDetails currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<Bookshelf> shelves = userService.getUserShelves(userId);

        Map<String, Object> stats = statisticsRepository.getUserStats(userId);

        // czy to właściciel? (edycja i backup)
        boolean isOwner = currentUser != null && currentUser.getUsername().equals(user.getUsername());

        model.addAttribute("user", user);
        model.addAttribute("shelves", shelves);
        model.addAttribute("stats", stats);
        model.addAttribute("isOwner", isOwner);

        return "profile"; // templates/profile.html
    }

    @GetMapping("/profile/edit")
    public String showEditForm(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        return "profile-edit"; // profile-edit.html
    }

    // Zapis danych (BIO + AVATAR)
    @PostMapping("/profile/update")
    public String updateProfile(
            @AuthenticationPrincipal UserDetails currentUser,
            @RequestParam("bio") String bio,
            @RequestParam("avatar") MultipartFile avatarFile
    ) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Aktualizacja BIO
        user.setBio(bio);

        // Obsługa pliku (jeśli wgrano nowy)
        if (!avatarFile.isEmpty()) {
            String avatarUrl = fileStorageService.saveFile(avatarFile);
            user.setAvatar(avatarUrl);
        }

        userRepository.save(user);

        return "redirect:/profile/" + user.getId();
    }

    @GetMapping("/profile/export")
    public ResponseEntity<UserBackupDTO> exportProfile(@AuthenticationPrincipal UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername()).orElseThrow();

        var shelfDtos = user.getBookshelves().stream().map(shelf -> new org.example.bookaroo.dto.ShelfBackupDTO(
                shelf.getName(),
                shelf.getBooks().stream().map(Book::getIsbn).toList()
        )).toList();

        var reviewDtos = user.getGivenReviews().stream().map(review -> new org.example.bookaroo.dto.ReviewBackupDTO(
                review.getBook().getIsbn(),
                review.getContent(),
                review.getRating()
        )).toList();

        UserBackupDTO backupDto = new UserBackupDTO(
                user.getBio(),
                user.getAvatar(),
                shelfDtos,
                reviewDtos
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=backup_" + user.getUsername() + ".json")
                .body(backupDto);
    }

    @PostMapping("/profile/import")
    @Transactional
    public String importProfile(@AuthenticationPrincipal UserDetails currentUser, @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return "redirect:/";

        try {
            User user = userRepository.findByUsername(currentUser.getUsername()).orElseThrow();
            UserBackupDTO backupDto = new ObjectMapper().readValue(file.getInputStream(), UserBackupDTO.class);

            // bio / avatar
            if (backupDto.bio() != null) user.setBio(backupDto.bio());
            if (backupDto.avatar() != null) user.setAvatar(backupDto.avatar());

            // półki
            if (backupDto.shelves() != null) {
                for (var shelfDto : backupDto.shelves()) {
                    Bookshelf shelf = user.getBookshelves().stream()
                            .filter(s -> s.getName().equalsIgnoreCase(shelfDto.name()))
                            .findFirst()
                            .orElseGet(() -> {
                                Bookshelf newShelf = new Bookshelf();
                                newShelf.setName(shelfDto.name());
                                newShelf.setUser(user);
                                user.getBookshelves().add(newShelf);
                                return bookshelfRepository.save(newShelf);
                            });

                    for (String isbn : shelfDto.bookIsbns()) {
                        bookRepository.findByIsbn(isbn).ifPresent(book -> {
                            boolean alreadyOnShelf = shelf.getBooks().stream()
                                    .anyMatch(b -> b.getId().equals(book.getId()));

                            if (!alreadyOnShelf) {
                                shelf.getBooks().add(book);
                            }
                        });
                    }
                    bookshelfRepository.save(shelf);
                }
            }

            // recenzje
            if (backupDto.reviews() != null) {
                for (var reviewDto : backupDto.reviews()) {
                    bookRepository.findByIsbn(reviewDto.bookIsbn()).ifPresent(book -> {
                        boolean reviewExists = user.getGivenReviews().stream()
                                .anyMatch(r -> r.getBook().getId().equals(book.getId()));

                        if (!reviewExists) {
                            org.example.bookaroo.entity.Review review = new org.example.bookaroo.entity.Review();
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
            return "redirect:/profile/" + user.getId() + "?success=restored";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/?error=import_failed";
        }
    }
}