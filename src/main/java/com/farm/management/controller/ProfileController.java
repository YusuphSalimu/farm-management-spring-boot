package com.farm.management.controller;

import com.farm.management.entity.User;
import com.farm.management.repository.UserRepository;
import com.farm.management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    // Helper to get current logged in user
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Show profile page
    @GetMapping("/profile")
    public String showProfile(Model model) {
        model.addAttribute("user", getCurrentUser());
        return "profile";
    }

    // Update profile
    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String fullName,
            @RequestParam(required = false) MultipartFile profilePicture,
            RedirectAttributes redirectAttributes) {

        try {
            User user = getCurrentUser();
            userService.updateProfile(user, fullName, profilePicture);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error updating profile: " + e.getMessage());
        }
        return "redirect:/profile";
    }


    // Show settings page
    @GetMapping("/settings")
    public String showSettings(Model model) {
        model.addAttribute("user", getCurrentUser());
        return "settings";
    }

    // Change password
    @PostMapping("/settings/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmNewPassword,
            RedirectAttributes redirectAttributes) {

        if (!newPassword.equals(confirmNewPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "New passwords do not match.");
            return "redirect:/settings";
        }

        User user = getCurrentUser();
        boolean success = userService.changePassword(
                user, currentPassword, newPassword);

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Password changed successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Current password is incorrect.");
        }
        return "redirect:/settings";
    }

    // Delete account
    @PostMapping("/settings/delete-account")
    public String deleteAccount(
            HttpServletRequest request,
            HttpServletResponse response) {

        User user = getCurrentUser();
        Long userId = user.getId();

        // Logout first
        new SecurityContextLogoutHandler()
                .logout(request, response,
                        SecurityContextHolder.getContext().getAuthentication());

        // Delete user
        userService.deleteAccount(userId);

        return "redirect:/login?deleted=true";
    }
}