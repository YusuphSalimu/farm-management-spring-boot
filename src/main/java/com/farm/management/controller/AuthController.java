package com.farm.management.controller;

import com.farm.management.entity.User;
import com.farm.management.service.UserService;
import com.farm.management.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/")
    public String landingPage() {
        return "landing";
    }

    // Show login page
    @GetMapping("/login")
    public String showLoginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("errorMessage",
                    "Invalid email or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage",
                    "You have been logged out successfully.");
        }
        return "auth/login";
    }

    // Show registration page
    @GetMapping("/register")
    public String showRegisterPage() {
        return "auth/register";
    }

    // Handle registration form submission
    @PostMapping("/register")
    public String registerUser(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage",
                    "Passwords do not match.");
            return "auth/register";
        }

        // Check if email already exists
        if (userService.existsByEmail(email)) {
            model.addAttribute("errorMessage",
                    "Email already registered. Please use a different email.");
            return "auth/register";
        }

        // Register the user
        userService.registerUser(fullName, email, password);

        // After userService.registerUser(...)
        User newUser = userService.findByEmail(email).orElse(null);
        if (newUser != null) {
            userService.ensureDefaultFarm(newUser);
        }

        // Send welcome email
        emailService.sendWelcomeEmail(email, fullName);

        return "redirect:/login?registered=true";
    }
}