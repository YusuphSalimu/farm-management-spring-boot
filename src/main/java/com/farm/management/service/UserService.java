package com.farm.management.service;

import com.farm.management.entity.Farm;
import com.farm.management.entity.Role;
import com.farm.management.entity.User;
import com.farm.management.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.farm.management.repository.SaleRepository;
import com.farm.management.repository.ExpenseRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SupabaseStorageService storageService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CropRepository cropRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private HarvestRecordRepository harvestRecordRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private FarmRepository farmRepository;

    // Register new user
    public User registerUser(String fullName, String email, String password) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        user.setProfilePicture("default-avatar.png");

        Role role = roleRepository.findByName("ROLE_FARMER")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(role);

        return userRepository.save(user);
    }

    // Find user by email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Check if email exists
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Save user
    public User save(User user) {
        return userRepository.save(user);
    }

    // Get all users (admin)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    // Get all users except the currently logged in admin
    public List<User> findAllUsersExcept(String loggedInEmail) {
        return userRepository.findAll().stream()
                .filter(user -> !user.getEmail().equalsIgnoreCase(loggedInEmail))
                .toList();
    }

    // Find user by id
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // Update profile
    public User updateProfile(User user, String fullName,
                              MultipartFile profilePicture) throws Exception {

        user.setFullName(fullName);

        if (profilePicture != null && !profilePicture.isEmpty()) {
            String imageUrl = storageService
                    .uploadProfilePicture(profilePicture, user.getId());
            user.setProfilePicture(imageUrl);
        }

        return userRepository.save(user);
    }

    // Change password
    public boolean changePassword(User user, String currentPassword,
                                  String newPassword) {
        if (passwordEncoder.matches(currentPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    // Delete account permanently across Storage and DB
    @Transactional
    public void deleteAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // 1. Delete profile picture from Supabase Storage
        if (user.getProfilePicture() != null &&
                user.getProfilePicture().startsWith("http")) {
            try {
                storageService.deleteFile(user.getProfilePicture());
            } catch (Exception e) {
                System.err.println("Could not delete profile pic: "
                        + e.getMessage());
            }
        }

        // 2. Delete sales first (references crops)
        List<com.farm.management.entity.Sale> sales =
                saleRepository.findByUser(user);
        saleRepository.deleteAll(sales);

        // 3. Delete harvest records (references crops)
        harvestRecordRepository.deleteAll(
                harvestRecordRepository.findByUser(user));

        // 4. Delete expenses
        expenseRepository.deleteAll(
                expenseRepository.findByUser(user));

        // 5. Delete crops
        cropRepository.deleteAll(
                cropRepository.findByUser(user));

        // 6. Delete inventory
        inventoryRepository.deleteAll(
                inventoryRepository.findByUser(user));

        // 7. Delete farms
        farmRepository.deleteAll(
                farmRepository.findByUser(user));

        // 8. Delete user row completely from the repository
        userRepository.delete(user);

        // 9. Force immediate sync to clear down the database state instantly
        userRepository.flush();
    }

    // Toggle user enabled (admin)
    @Transactional
    public void toggleUserEnabled(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(!user.getEnabled());
        userRepository.save(user);
    }

    public void ensureDefaultFarm(User user) {
        long farmCount = farmRepository.countByUser(user);
        if (farmCount == 0) {
            Farm defaultFarm = new Farm();
            defaultFarm.setFarmName("General Farm");
            defaultFarm.setLocation("Tanzania");
            defaultFarm.setDescription("Default farm — update with your actual farm details");
            defaultFarm.setUser(user);
            farmRepository.save(defaultFarm);
        }
    }
}