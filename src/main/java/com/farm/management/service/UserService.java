package com.farm.management.service;

import com.farm.management.entity.Farm;
import com.farm.management.entity.Role;
import com.farm.management.entity.User;
import com.farm.management.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    // Find user by id
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // Update profile
    public User updateProfile(User user, String fullName,
                              MultipartFile profilePicture) throws IOException {

        user.setFullName(fullName);

        if (profilePicture != null && !profilePicture.isEmpty()) {
            // Save to static/uploads/profiles/
            String uploadDir = System.getProperty("user.dir")
                    + "/src/main/resources/static/uploads/profiles/";
            String fileName = UUID.randomUUID().toString()
                    + "_" + profilePicture.getOriginalFilename()
                    .replaceAll("[^a-zA-Z0-9._-]", "_");
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);
            Files.write(uploadPath.resolve(fileName),
                    profilePicture.getBytes());
            user.setProfilePicture(fileName);
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

    // Delete account
    public void deleteAccount(Long userId) {
        // Delete related records first to avoid foreign key errors
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete harvest records
        harvestRecordRepository.deleteAll(
                harvestRecordRepository.findByUser(user));

        // Delete inventory
        inventoryRepository.deleteAll(
                inventoryRepository.findByUser(user));

        // Delete crops
        cropRepository.deleteAll(
                cropRepository.findByUser(user));

        // Delete user
        userRepository.deleteById(userId);
    }

    // Toggle user enabled (admin)
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