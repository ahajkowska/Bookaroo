package org.example.bookaroo.controller.view;

import org.example.bookaroo.entity.User;
import org.example.bookaroo.repository.UserRepository;
import org.example.bookaroo.service.FileStorageService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public ProfileController(UserRepository userRepository, FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    // 1. Wyświetl formularz edycji
    @GetMapping("/profile/edit")
    public String showEditForm(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        return "profile-edit"; // Zwraca widok profile-edit.html
    }

    // 2. Obsłuż zapis danych (BIO + AVATAR)
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

        return "redirect:/profile/" + user.getId(); // Przekieruj z powrotem na profil
    }
}