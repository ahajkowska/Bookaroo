package org.example.bookaroo.controller.view;

import jakarta.validation.Valid;
import org.example.bookaroo.dto.CreateUserDTO;
import org.example.bookaroo.exception.UserAlreadyExistsException;
import org.example.bookaroo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        CreateUserDTO userDTO = new CreateUserDTO();
        model.addAttribute("user", userDTO);
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") CreateUserDTO userDTO,
                               BindingResult result,
                               Model model) {

        if (result.hasErrors()) {
            return "register";
        }

        try {
            userDTO.setRole("USER");

            userService.createUser(userDTO);

            return "redirect:/login?registered";

        } catch (UserAlreadyExistsException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}