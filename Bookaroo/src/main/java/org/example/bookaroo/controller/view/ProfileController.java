package org.example.bookaroo.controller.view;

import org.example.bookaroo.dto.BookshelfDTO;
import org.example.bookaroo.entity.User;
import org.example.bookaroo.service.BackupService;
import org.example.bookaroo.service.BookshelfService;
import org.example.bookaroo.service.CustomUserDetailsService;
import org.example.bookaroo.service.UserService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    private final BackupService backupService;

    public ProfileController(UserService userService, BookshelfService bookshelfService, BackupService backupService) {
        this.userService = userService;
        this.bookshelfService = bookshelfService;
        this.backupService = backupService;
    }

    // profil użytkownika
    @GetMapping("/profile/{userId}")
    public String showProfile(@PathVariable UUID userId, Model model, @AuthenticationPrincipal UserDetails currentUser) {
        User user = userService.findById(userId);

        List<BookshelfDTO> shelvesDtos = bookshelfService.getUserShelvesWithDetails(userId);

        Map<String, Object> stats = userService.getUserStats(userId);

        // czy to właściciel? (edycja i backup)
        boolean isOwner = currentUser != null && currentUser.getUsername().equals(user.getUsername());

        model.addAttribute("user", user);
        model.addAttribute("shelves", shelvesDtos);
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
    // --- EKSPORT ---
    @GetMapping("/profile/export")
    public ResponseEntity<ByteArrayResource> exportProfile(@AuthenticationPrincipal UserDetails currentUser, @RequestParam(defaultValue = "json") String format) {

        try {
            byte[] fileData;
            String filename;
            MediaType mediaType;

            if ("csv".equalsIgnoreCase(format)) {
                fileData = backupService.exportUserReviewsToCsv(currentUser.getUsername());
                filename = "recenzje_" + currentUser.getUsername() + ".csv";
                mediaType = MediaType.parseMediaType("text/csv");
            } else if ("pdf".equalsIgnoreCase(format)) {
                fileData = backupService.exportUserReviewsToPdf(currentUser.getUsername());
                filename = "recenzje_" + currentUser.getUsername() + ".pdf";
                mediaType = MediaType.APPLICATION_PDF;
            } else {
                // JSON
                fileData = backupService.exportUserDataToJson(currentUser.getUsername());
                filename = "backup_" + currentUser.getUsername() + ".json";
                mediaType = MediaType.APPLICATION_JSON;
            }

            ByteArrayResource resource = new ByteArrayResource(fileData);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(mediaType)
                    .contentLength(fileData.length)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/profile/import")
    public String importProfile(@AuthenticationPrincipal UserDetails currentUser,
                                @RequestParam("file") MultipartFile file) {

        User user = userService.findByUsername(currentUser.getUsername());

        if (file.isEmpty()) {
            return "redirect:/profile/" + user.getId() + "?error=import_failed";
        }

        try {
            backupService.importUserData(currentUser.getUsername(), file);

            return "redirect:/profile/" + user.getId() + "?success=restored";

        } catch (Exception e) {
            e.printStackTrace();

            return "redirect:/profile/" + user.getId() + "?error=import_failed";
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