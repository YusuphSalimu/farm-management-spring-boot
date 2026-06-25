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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Admin dashboard
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentAdmin = getCurrentUser();

        model.addAttribute("user", currentAdmin);

        // CRITICAL SECURITY UPDATE: Hides the currently logged-in Admin from the list
        model.addAttribute("users", userService.findAllUsersExcept(auth.getName()));

        // Keeps the total statistics accurate across all accounts
        model.addAttribute("totalUsers", userService.findAllUsers().size());
        model.addAttribute("activePage", "admin");
        return "admin/dashboard";
    }

    // Toggle user enabled/disabled
    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        userService.toggleUserEnabled(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "User status updated successfully.");
        return "redirect:/admin/dashboard";
    }

    // Delete user
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        userService.deleteAccount(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "User deleted successfully.");
        return "redirect:/admin/dashboard";
    }
}