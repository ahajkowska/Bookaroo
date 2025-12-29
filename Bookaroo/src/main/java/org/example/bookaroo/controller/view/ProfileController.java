package org.example.bookaroo.controller.view;

import org.example.bookaroo.dto.UserBackupDTO;
import org.example.bookaroo.entity.Bookshelf;
import org.example.bookaroo.entity.User;
import org.example.bookaroo.repository.*;
import org.example.bookaroo.service.BookshelfService;
import org.example.bookaroo.service.CustomUserDetailsService;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class ProfileController {

    private final UserService userService;
    private final BookshelfService bookshelfService;

    public ProfileController(UserService userService, BookshelfService bookshelfService) {
        this.userService = userService;
        this.bookshelfService = bookshelfService;
    }

    // profil użytkownika
    @GetMapping("/profile/{userId}")
    public String showProfile(@PathVariable UUID userId, Model model, @AuthenticationPrincipal UserDetails currentUser) {
        User user = userService.findById(userId);

        List<Bookshelf> shelves = bookshelfService.getUserShelves(userId);

        Map<String, Object> stats = userService.getUserStats(userId);

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
        User user = userService.findByUsername(currentUser.getUsername());

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
        userService.updateUserProfile(currentUser.getUsername(), bio, avatarFile);

        User user = userService.findByUsername(currentUser.getUsername());

        return "redirect:/profile/" + user.getId();
    }

    @GetMapping("/profile/export")
    public ResponseEntity<UserBackupDTO> exportProfile(@AuthenticationPrincipal UserDetails currentUser) {
        UserBackupDTO backupDto = userService.exportUserData(currentUser.getUsername());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=backup_" + currentUser.getUsername() + ".json")
                .body(backupDto);
    }

    @PostMapping("/profile/import")
    public String importProfile(@AuthenticationPrincipal UserDetails currentUser,
                                @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "redirect:/";
        }

        try {
            // Delegacja całej logiki do serwisu
            userService.importUserData(currentUser.getUsername(), file);

            // Pobranie ID tylko do przekierowania
            User user = userService.findByUsername(currentUser.getUsername());
            return "redirect:/profile/" + user.getId() + "?success=restored";

        } catch (Exception e) {
            e.printStackTrace(); // Warto zalogować błąd (log.error) zamiast printStackTrace
            return "redirect:/?error=import_failed";
        }
    }

    @PostMapping("/profile/shelves/create")
    public String createShelf(@AuthenticationPrincipal UserDetails currentUser,
                              @RequestParam("name") String name) {
        if (currentUser instanceof CustomUserDetailsService.BookarooUserDetails userDetails) {
            UUID userId = userDetails.getId();

            if (name != null && !name.trim().isEmpty()) {
                bookshelfService.createCustomShelf(userId, name.trim());
            }

            return "redirect:/profile/" + userId;
        }

        return "redirect:/login";
    }

    @PostMapping("/profile/challenge/update")
    public String updateChallenge(@AuthenticationPrincipal UserDetails currentUser,
                                  @RequestParam(name = "target", required = false) Integer target) {

        userService.updateReadingChallenge(currentUser.getUsername(), target);

        User user = userService.findByUsername(currentUser.getUsername());
        return "redirect:/profile/" + user.getId();
    }
}